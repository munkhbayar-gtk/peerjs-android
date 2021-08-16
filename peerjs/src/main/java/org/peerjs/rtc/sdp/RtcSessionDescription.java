package org.peerjs.rtc.sdp;

public class RtcSessionDescription {
    public final String type;
    public final String sdp;

    public RtcSessionDescription(String type, String sdp) {
        this.type = type;
        this.sdp = sdp;
    }
}
