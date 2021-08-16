package org.peerjs.event;

public interface IPeerEventListener<Data> {
    void onEvent(Data data);
}
