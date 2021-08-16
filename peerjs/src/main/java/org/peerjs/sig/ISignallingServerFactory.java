package org.peerjs.sig;

import org.peerjs.configuration.PeerOptions;
import org.peerjs.message.PeerJsMessage;

public interface ISignallingServerFactory {
    AbstractSignallingServer createServer(PeerOptions peerOptions, IWebSocketClientListener<PeerJsMessage> listener);
}
