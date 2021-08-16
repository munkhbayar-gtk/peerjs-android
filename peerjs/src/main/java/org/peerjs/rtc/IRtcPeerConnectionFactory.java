package org.peerjs.rtc;

import org.peerjs.configuration.PeerOptions;

public interface IRtcPeerConnectionFactory {
    AbstractRtcPeerConnection create(PeerOptions peerOptions);
}
