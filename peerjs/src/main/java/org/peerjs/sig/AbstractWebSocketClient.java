package org.peerjs.sig;

import org.peerjs.message.PeerJsMessage;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractWebSocketClient {
    protected IWebSocketClientListener<String> listener;
    protected AbstractWebSocketClient(IWebSocketClientListener<String> listener) {
        this.listener = listener;
    }
    public abstract void send(byte[] bytes);
    public abstract void send(String message);
    public abstract void close();
    public abstract void reconnect();
    public abstract void connect(String wsUrl) throws IOException;

}
