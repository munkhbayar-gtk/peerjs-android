package org.peerjs.rtc.sdp;

import org.webrtc.IceCandidate;

public class RtcIceCandidate {
    /*
        "candidate":"candidate:0 1 UDP 2122252543 192.168.43.189 52369 typ host",
        "sdpMid":"0",
        "sdpMLineIndex":0,
        "usernameFragment":"2b7a2e98"
    */
    public final String sdpMid;
    public final int sdpMLineIndex;
    public final String candidate;

    public RtcIceCandidate(String sdpMid, int sdpMLineIndex, String candidate) {
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
        this.candidate = candidate;
    }
}
