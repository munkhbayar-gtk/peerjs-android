package org.peerjs.rtc;

import org.peerjs.configuration.PeerRtcConfiguration;
import org.peerjs.message.SdpMessage;
import org.peerjs.rtc.sdp.RtcIceCandidate;
import org.peerjs.rtc.sdp.RtcSessionDescription;

interface IRtcPeerConnection {
    void createOffer(ISdpCreateListener listener);//, MediaConstraints mediaConstraints);
    void createAnswer(ISdpCreateListener listener);//, MediaConstraints mediaConstraints);
    void setLocalDescription(ISdpSetListener listener, RtcSessionDescription sdp);
    RtcSessionDescription getLocalDescription();
    void setRemoteDescription(ISdpSetListener listener, RtcSessionDescription sdp);
    RtcSessionDescription getRemoteDescription();
    void setConfiguration(PeerRtcConfiguration configuration);

    boolean addIceCandidate(RtcIceCandidate iceCandidate);
    boolean removeIceCandidates(RtcIceCandidate[] iceCandidates);
    void close();


    void onIceCandidate(IPeerConnectionListener<RtcIceCandidate> handler);
    void onIceCandidatesRemoved(IPeerConnectionListener<RtcIceCandidate[]> handler);
    void onSignalingChange(IPeerConnectionListener<RtcSignalState> handler);
    void onIceConnectionChange(IPeerConnectionListener<RtcIceConnectionState> handler);


    //void onStandardizedIceConnectionChange(IPeerConnectionListener<RtcSignalState> handler);
    void onConnectionState(IPeerConnectionListener<RtcConnectionState> handler);
    void onRenegotiationNeeded(IPeerConnectionListener<Void> handler);
}
