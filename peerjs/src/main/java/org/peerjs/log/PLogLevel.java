package org.peerjs.log;

public enum  PLogLevel {
    VERBOSE(0),
    TRACE(1),
    DEBUG(2),
    WARN(3),
    INFO(4),
    ERROR(5);

    public final int value;
    PLogLevel(int value){
        this.value = value;
    }
}
