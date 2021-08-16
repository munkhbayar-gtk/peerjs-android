package org.peerjs.event;

public enum PeerEventType {

    CONNECTION("connection"),
    DISCONNECTED("disconnected"),
    STREAM("stream"),
    OPEN("open"),
    CALL("call"),

    CLOSE("close"),
    ERROR("error");

    private final String value;
    private PeerEventType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
