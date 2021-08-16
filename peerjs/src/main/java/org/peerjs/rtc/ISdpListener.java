package org.peerjs.rtc;

import org.peerjs.rtc.sdp.RtcSessionDescription;

public interface ISdpListener {
    void onCreateSuccess(RtcSessionDescription sdp);

    void onCreateFailure(String error);
    void onSetFailure(String error);
    void onSetSuccess();
}
