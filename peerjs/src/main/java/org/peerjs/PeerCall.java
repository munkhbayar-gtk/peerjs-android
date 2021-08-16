package org.peerjs;

import org.peerjs.configuration.PeerOptions;
import org.peerjs.event.PeerEventType;
import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;
import org.peerjs.message.PeerJsMessage;
import org.peerjs.message.PeerJsMessagePayload;
import org.peerjs.message.PeerJsMessageType;
import org.peerjs.message.SdpMessage;
import org.peerjs.rtc.AbstractRtcPeerConnection;
import org.peerjs.rtc.ISdpCreateListener;
import org.peerjs.rtc.ISdpSetListener;
import org.peerjs.rtc.ISignalling;
import org.peerjs.rtc.MediaConstraints;
import org.peerjs.rtc.RtcIceConnectionState;
import org.peerjs.rtc.sdp.RtcIceCandidate;
import org.peerjs.rtc.sdp.RtcSessionDescription;

public abstract class PeerCall extends MediaConnection {
    private static final PLog log = PLogFactory.getLogger(PeerCall.class);
    protected PeerJsMessage offerMessage;
    protected PeerOptions peerOptions;
    private AbstractRtcPeerConnection rtcPeerConnection;
    private ISignalling signalSender;

    protected PeerCall(PeerOptions peerOptions, PeerJsMessage offerMessage,
                       AbstractRtcPeerConnection rtcPeerConnection, ISignalling signalSender) {
        this(peerOptions, rtcPeerConnection, signalSender);
        this.offerMessage = offerMessage;
    }
    protected PeerCall(PeerOptions peerOptions, AbstractRtcPeerConnection rtcPeerConnection, ISignalling signalSender) {
        this.peerOptions = peerOptions;
        this.rtcPeerConnection = rtcPeerConnection;
        this.signalSender = signalSender;
        initPeerConnectionListeners();
    }

    private String getDstId(){
        String myId = peerOptions.getId();
        String src = offerMessage.getSrc();
        String dst = offerMessage.getDst();
        if(myId.equals(src)) return dst;
        return src;
    }

    private String getConId() {
        String conId = offerMessage.getPayload().getConnectionId();
        return conId;
    }

    private void initPeerConnectionListeners() {
        rtcPeerConnection.onIceCandidate((iceCandidate)->{
            String destId = getDstId();
            signalSender.sendIceCandidate(createCandidate(iceCandidate), destId, getConId());
        });
        rtcPeerConnection.onIceConnectionChange((connectionState)->{
            if(Utils.in(connectionState, RtcIceConnectionState.DISCONNECTED)) {
                fire(PeerEventType.CLOSE, null);
            }
        });
        rtcPeerConnection.onConnectionState((connectionState)->{
            if(Utils.in(connectionState, RtcIceConnectionState.CLOSED, RtcIceConnectionState.DISCONNECTED)) {
                fire(PeerEventType.CLOSE, null);
            }
            else if(Utils.in(connectionState, RtcIceConnectionState.FAILED)) {
                PeerError err = new PeerError(PeerErrorType.RTC_ERROR);
                err.setCause(new Exception(connectionState.name()));
                fire(PeerEventType.ERROR, err);
            }
        });
        //rtcPeerConnection.on
    }

    public final String getInitiatorId() {
        return offerMessage.getSrc();
    }
    public final String getReceiverId() {
        return offerMessage.getDst();
    }
    public PeerJsMessage getOfferMessage() {
        return offerMessage;
    }

    protected void establish(IPeerInputStream stream) {
        //create answer
        log.d("bind answer local stream to rtc connection");
        rtcPeerConnection.createPeerConnection(stream);
        createAnswer();
    }
    private void createAnswer() {
        log.d("Creating-1 answer ...");
        RtcSessionDescription offerSdp = createSdp(offerMessage.getPayload().getSdp());
        rtcPeerConnection.setRemoteDescription(new ISdpSetListener() {
            @Override
            public void onSetSuccess() {
                log.d("Created-2 answer ...");
                rtcPeerConnection.createAnswer(new ISdpCreateListener() {
                    @Override
                    public void onCreateSuccess(RtcSessionDescription sdp) {
                        log.d("createAnswer/onCreateSuccess");
                        onAnswerCreated(sdp);
                    }

                    @Override
                    public void onCreateFailure(String error) {
                        log.e("handleOfferMessage/setRemoteDescription/createAnswer/onCreateFailure: " + error);
                        fireError(error);
                    }
                });
            }

            @Override
            public void onSetFailure(String error) {
                log.e("handleOfferMessage/setRemoteDescription/onSetFailure: " + error);
                fireError(error);
            }
        }, offerSdp);
    }
    @Override
    void connect(String peerId, IPeerInputStream stream) {
        //super.connect(stream);
        // 1. create an offer message and send it to signalling server
        log.d("bind local stream to rtcConnection");
        // 1. create physical rtc connection
        rtcPeerConnection.createPeerConnection(stream);
        createOfferMessage(peerId, stream);
    }

    private void createOfferMessage(String peerId, IPeerInputStream stream){
        rtcPeerConnection.createOffer(new ISdpCreateListener() {
            @Override
            public void onCreateSuccess(RtcSessionDescription sdp) {
                log.d("createOfferMessage: createOffer: create success, Setting localDescription");
                onOfferCreated(sdp, peerId);
            }

            @Override
            public void onCreateFailure(String error) {
                log.e("createOfferMessage: createOffer: failure: " + error);
                fireError(error);
            }
        });
    }
    void createRtcPeerConnection(){
        //this.rtcPeerConnection.createPeerConnection();
    }
    private void onOfferCreated(RtcSessionDescription offerSdp, String peerId) {
        rtcPeerConnection.setLocalDescription(new ISdpSetListener() {
            @Override
            public void onSetSuccess() {
                log.d("onOfferCreated/setLocalDescription/onSetSuccess");
                RtcSessionDescription localSdp = rtcPeerConnection.getLocalDescription();
                offerMessage = signalSender.sendOfferSdp(createSdp(localSdp), peerId);
            }

            @Override
            public void onSetFailure(String error) {
                log.e("onOfferCreated/setLocalDescription/onSetFailure: " + error);
            }
        }, offerSdp);
    }
    private void onAnswerCreated(RtcSessionDescription answerSdp) {
        rtcPeerConnection.setLocalDescription(new ISdpSetListener() {
            @Override
            public void onSetSuccess() {
                log.d("onAnswerCreated/setLocalDescription/onSetSuccess");
                RtcSessionDescription localSdp = rtcPeerConnection.getLocalDescription();
                signalSender.sendAnswerSdp(createSdp(localSdp), getDstId(), getConId());
            }

            @Override
            public void onSetFailure(String error) {
                log.e("onAnswerCreated/setLocalDescription/onSetFailure: " + error);
                fireError(error);
            }
        }, answerSdp);
    }

    void handleOfferMessage(PeerJsMessage offerMessage){
        this.offerMessage = offerMessage;
    }
    void handleAnswerMessage(PeerJsMessage answerMessage){
        rtcPeerConnection.setRemoteDescription(new ISdpSetListener() {
            @Override
            public void onSetSuccess() {
                log.d("handleAnswerMessage/setRemoteDescription/onSetSuccess: set successfull");
            }
            @Override
            public void onSetFailure(String error) {
                log.e("handleAnswerMessage/setRemoteDescription/onSetFailure: " + error);
            }
        }, createSdp(answerMessage.getPayload().getSdp()));
    }

    void handleIceCandidateMessage(PeerJsMessage iceCandidateMessage) {
        RtcIceCandidate candidate = createCandidate(iceCandidateMessage.getPayload().getCandidate());
        rtcPeerConnection.addIceCandidate(candidate);
    }

    private SdpMessage createSdp(RtcSessionDescription rtcSessionDescription) {
        SdpMessage sdp = new SdpMessage();
        sdp.put("type", rtcSessionDescription.type);
        sdp.put("sdp", rtcSessionDescription.sdp);
        return sdp;
    }
    private RtcSessionDescription createSdp(SdpMessage sdpMessage) {
        return new RtcSessionDescription(sdpMessage.get("type"), sdpMessage.get("sdp"));
    }
    private SdpMessage createCandidate(RtcIceCandidate candidate) {
        SdpMessage sdp = new SdpMessage();
        sdp.put("sdpMid", candidate.sdpMid);
        sdp.put("sdpMLineIndex", candidate.sdpMLineIndex + "");
        sdp.put("candidate", candidate.candidate);
        return sdp;
    }
    private RtcIceCandidate createCandidate(SdpMessage sdpMessage) {
        //String sdpMid, int sdpMLineIndex, String candidate
        return new RtcIceCandidate(sdpMessage.get("sdpMid"),
                sdpMessage.getAsInt("sdpMLineIndex"),
                sdpMessage.get("candidate"));
    }
    private void fireError(String error) {
        PeerError err = new PeerError(PeerErrorType.RTC_ERROR);
        err.setCause(new Exception(error));
        fire(PeerEventType.ERROR, err);
    }
}
