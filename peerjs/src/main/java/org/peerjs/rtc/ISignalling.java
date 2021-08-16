package org.peerjs.rtc;

import org.peerjs.message.PeerJsMessage;
import org.peerjs.message.SdpMessage;

public interface ISignalling {
    PeerJsMessage sendOfferSdp(SdpMessage message, String destPeerId);
    void sendAnswerSdp(SdpMessage message, String destPeerId, String connectionId);
    void sendIceCandidate(SdpMessage candidateMessage, String destPeerId, String connectionId);
    void sendLocalIceCandidateRemovals(SdpMessage[] candidates, String destPeerId, String connectionId);
}
