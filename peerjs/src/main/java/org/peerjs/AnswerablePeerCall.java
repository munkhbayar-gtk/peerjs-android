package org.peerjs;

import org.peerjs.configuration.PeerOptions;
import org.peerjs.message.PeerJsMessage;
import org.peerjs.rtc.AbstractRtcPeerConnection;
import org.peerjs.rtc.ISignalling;

public final class AnswerablePeerCall extends PeerCall {
    public AnswerablePeerCall(PeerOptions peerOptions, PeerJsMessage offerMessage,
                              AbstractRtcPeerConnection rtcPeerConnection,
                              ISignalling signalSender) {
        super(peerOptions, offerMessage, rtcPeerConnection, signalSender);
    }

    public final void answer(IPeerInputStream stream){
        this.establish(stream);
    }
}
