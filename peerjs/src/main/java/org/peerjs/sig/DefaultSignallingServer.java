package org.peerjs.sig;

import org.peerjs.configuration.PeerOptions;
import org.peerjs.message.PeerJsMessage;


public class DefaultSignallingServer extends AbstractSignallingServer{

    public DefaultSignallingServer(PeerOptions peerOptions,
                                   IWebSocketClientListener<PeerJsMessage> webSocketClientListener) {
        super(peerOptions, webSocketClientListener);
    }

    @Override
    protected IHttpClient createHttpClient() {
        return new DefaultHttpClient();
    }

    @Override
    protected AbstractWebSocketClient createWebSocketClient() {
        return new DefaultWebSocketClient(new IWebSocketClientListener<String>() {
            @Override
            public void onOpen() {
                webSocketClientListener.onOpen();
            }

            @Override
            public void onClose(int code, String reason) {
                webSocketClientListener.onClose(code, reason);
            }

            @Override
            public void onMessage(String message) {
                PeerJsMessage peerJsMessage = messageParser.parse(message);
                webSocketClientListener.onMessage(peerJsMessage);
            }

            @Override
            public void onMessage(byte[] bytes) {
                webSocketClientListener.onMessage(bytes);
            }

            @Override
            public void onError(Throwable error) {
                webSocketClientListener.onError(error);
            }
        });
    }
}
