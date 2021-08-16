package org.peerjs.sig;

import org.peerjs.message.PeerJsMessage;

public interface IMessageParser {
    PeerJsMessage parse(String json);
    String toString(PeerJsMessage message);
}
