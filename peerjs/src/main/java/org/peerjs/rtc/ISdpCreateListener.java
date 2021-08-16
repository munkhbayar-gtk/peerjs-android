package org.peerjs.rtc;

import org.peerjs.rtc.sdp.RtcSessionDescription;

public interface ISdpCreateListener {
    void onCreateSuccess(RtcSessionDescription sdp);
    void onCreateFailure(String error);
}
