package org.peerjs;

import org.peerjs.AnswerablePeerCall;
import org.peerjs.PeerCall;
import org.peerjs.configuration.PeerOptions;
import org.peerjs.event.PeerEventType;
import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;
import org.peerjs.message.PeerJsMessage;
import org.peerjs.message.PeerJsMessagePayload;
import org.peerjs.message.PeerJsMessageType;
import org.peerjs.message.SdpMessage;
import org.peerjs.rtc.AbstractRtcPeerConnection;
import org.peerjs.rtc.IRtcPeerConnectionFactory;
import org.peerjs.rtc.ISdpCreateListener;
import org.peerjs.rtc.ISdpListener;
import org.peerjs.rtc.ISdpSetListener;
import org.peerjs.rtc.ISignalling;
import org.peerjs.rtc.MediaConstraints;
import org.peerjs.rtc.sdp.RtcIceCandidate;
import org.peerjs.rtc.sdp.RtcSessionDescription;

final class PeerCallFactory {
    private static final PLog log = PLogFactory.getLogger(PeerCallFactory.class);
    IRtcPeerConnectionFactory rtcConnectionFactory;

    final PeerCall createCallableCall(PeerOptions peerOptions,
                                ISignalling signalSender){
        AbstractRtcPeerConnection rtcPeerConnection = rtcConnectionFactory.create(peerOptions);
        return new PeerCall(peerOptions, rtcPeerConnection, signalSender) {};
    }

    final AnswerablePeerCall createAnswerableCall(PeerOptions peerOptions, PeerJsMessage offerMessage,
                                            ISignalling signalSender){
        AbstractRtcPeerConnection rtcPeerConnection = rtcConnectionFactory.create(peerOptions);
        return new AnswerablePeerCall(peerOptions, offerMessage, rtcPeerConnection, signalSender);
    }
}
