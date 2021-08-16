package org.peerjs.sig;

public interface IWebSocketClientListener<MessageType> {
    void onOpen();
    void onClose(int code, String reason);
    void onMessage(MessageType message);
    void onMessage(byte[] bytes);
    void onError(Throwable error);
}
