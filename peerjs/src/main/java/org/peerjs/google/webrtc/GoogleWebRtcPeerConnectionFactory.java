package org.peerjs.google.webrtc;

import org.peerjs.configuration.PeerOptions;
import org.peerjs.rtc.AbstractRtcPeerConnection;
import org.peerjs.rtc.IRtcPeerConnectionFactory;

public class GoogleWebRtcPeerConnectionFactory implements IRtcPeerConnectionFactory {
    @Override
    public AbstractRtcPeerConnection create(PeerOptions peerOptions) {
        return new GoogleWebRtcPeerConnectionWrapper(peerOptions);
    }
}
