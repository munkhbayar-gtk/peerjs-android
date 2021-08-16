package org.peerjs.sig;

import java.io.IOException;
import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;

public class DefaultWebSocketClient extends AbstractWebSocketClient {
    private static PLog log = PLogFactory.getLogger(DefaultWebSocketClient.class);
    private WebSocketClient wsSocket;

    public DefaultWebSocketClient( IWebSocketClientListener<String> listener) {
        super (listener);
    }

    @Override
    public void connect(String wsUrl) throws IOException {
        try{
            log.d("ws-url: " + wsUrl);
            wsSocket = new WebSocketClient(new URI(wsUrl)){

                @Override
                public void onClose(int code, String reason, boolean arg2) {
                    listener.onClose(code, reason);
                }
    
                @Override
                public void onError(Exception error) {
                    listener.onError(error);
                }
    
                @Override
                public void onMessage(String message) {
                    log.d("ws-message : " + message);
                    listener.onMessage(message);
                }
    
                @Override
                public void onOpen(ServerHandshake arg0) {
                    listener.onOpen();
                }
                
            };
            wsSocket.connect();
        }catch(Exception e){
            throw new IOException("websocket connection error", e);
        }
        
    }

    @Override
    public void send(byte[] bytes) {
        wsSocket.send(bytes);
    }

    @Override
    public void send(String message) {
        wsSocket.send(message);
    }

    @Override
    public void close() {
        log.d("Closing WebSocket client");
        try{
            wsSocket.closeBlocking();
        }catch (Exception e) {
            log.e("ERR-WebSocket close: ", e);
        }

    }

    @Override
    public void reconnect() {
        wsSocket.reconnect();
    }
}
