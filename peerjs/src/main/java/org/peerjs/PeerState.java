package org.peerjs;

public enum PeerState {
    INITIAL,

    OPENNING,
    OPEN,
    DESTROYING,
    DESTROYED,
    DISCONNECTING,
    DISCONNECTED,
    ERROR;
}
