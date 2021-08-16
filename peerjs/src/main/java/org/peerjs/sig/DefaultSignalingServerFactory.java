package org.peerjs.sig;

import org.peerjs.configuration.PeerOptions;
import org.peerjs.message.PeerJsMessage;

public class DefaultSignalingServerFactory implements ISignallingServerFactory{
    @Override
    public AbstractSignallingServer createServer(PeerOptions peerOption, IWebSocketClientListener<PeerJsMessage> listener){
        return new DefaultSignallingServer(peerOption, listener);
    }
}
