package org.peerjs.event;

public class CloseEvent {
    public final int code;
    public final String reason;

    public CloseEvent(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }
}
