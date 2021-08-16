package org.peerjs.rtc;

import org.peerjs.rtc.sdp.RtcSessionDescription;

public interface ISdpSetListener {
    void onSetSuccess();
    void onSetFailure(String error);
}

