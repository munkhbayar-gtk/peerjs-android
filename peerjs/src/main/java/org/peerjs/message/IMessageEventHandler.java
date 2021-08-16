package org.peerjs.message;

public interface IMessageEventHandler<Data> {
    void onEvent(Data data);
}
