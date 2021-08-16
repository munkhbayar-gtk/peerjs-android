package org.peerjs.sig;

import com.google.gson.Gson;

import org.peerjs.message.PeerJsMessage;

public class DefaultMessageParser implements IMessageParser{
    private Gson gson = new Gson();
    @Override
    public PeerJsMessage parse(String json) {
        return gson.fromJson(json, PeerJsMessage.class);
    }

    @Override
    public String toString(PeerJsMessage message) {
        return gson.toJson(message);
    }
}
