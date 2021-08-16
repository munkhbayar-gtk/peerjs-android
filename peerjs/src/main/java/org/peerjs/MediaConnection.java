package org.peerjs;

import org.peerjs.IPeerInputStream;
import org.peerjs.IPeerOutputStream;
import org.peerjs.PeerError;
import org.peerjs.event.IPeerEventListener;
import org.peerjs.event.PeerEventType;

import java.util.HashMap;
import java.util.Map;

abstract class MediaConnection {
    private Map<PeerEventType, IPeerEventListener<?>> eventListeners = new HashMap<>();
    private Map<PeerEventType, IPeerEventListener<?>> internalEventListeners = new HashMap<>();

    public void onStream(IPeerEventListener<IPeerOutputStream> listener){
        eventListeners.put(PeerEventType.STREAM, listener);
    }

    void on(PeerEventType type, IPeerEventListener<?> listener) {
        internalEventListeners.put(type, listener);
    }

    protected void fire(PeerEventType type, Object data){
        IPeerEventListener<Object> iEventListener = (IPeerEventListener<Object>) internalEventListeners.get(type);
        if(iEventListener != null) {
            iEventListener.onEvent(data);
        }
        IPeerEventListener<Object> oEventListener = (IPeerEventListener<Object>)eventListeners.get(type);
        if(oEventListener != null) {
            oEventListener.onEvent(data);
        }
    }


    public void onClose(IPeerEventListener<Void> listener){
        eventListeners.put(PeerEventType.CLOSE, listener);
    }

    public void onError(IPeerEventListener<PeerError> listener){
        eventListeners.put(PeerEventType.ERROR, listener);
    }

    abstract void connect(String peerId, IPeerInputStream stream);

    public void close() {
        IPeerEventListener<?> internalEventListener = internalEventListeners.remove(PeerEventType.CLOSE);
        if(internalEventListener != null) {
            internalEventListener.onEvent(null);
        }
        IPeerEventListener<?> eventListener = eventListeners.remove(PeerEventType.CLOSE);
        if(eventListener != null) {
            eventListener.onEvent(null);
        }
    }
}
