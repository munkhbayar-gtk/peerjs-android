package org.peerjs.event;

public interface IEventDispatcher {
    void dispatche(PeerEventType type, Object data);
    default void dispatche(String type, Object data){}
}
