package org.peerjs.rtc;

import android.util.Pair;

import org.peerjs.IPeerInputStream;
import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;
import org.peerjs.message.SdpMessage;
import org.peerjs.rtc.sdp.RtcIceCandidate;
import org.peerjs.rtc.sdp.RtcSessionDescription;
import org.webrtc.PeerConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbstractRtcPeerConnection implements IRtcPeerConnection{
    private static PLog log = PLogFactory.getLogger(AbstractRtcPeerConnection.class);

    private ExecutorService executor = null;
    private Map<RTC_EVENT_TYPE, IPeerConnectionListener<?>> eventListeners = new HashMap<>();
    protected AbstractRtcPeerConnection() {
        executor = Executors.newSingleThreadExecutor();
    }

    public abstract void createPeerConnection(IPeerInputStream stream);
    protected void execute(Runnable task) {
        executor.execute(task);
    }
    @Override
    public void close() {
        executor.shutdownNow();
    }

    protected <Data> void fire(RTC_EVENT_TYPE type, Data data) {
        executor.execute(()->{
            IPeerConnectionListener<Object> listener = (IPeerConnectionListener<Object>) eventListeners.get(type);
            log.d("event: " + type + " has handler " + (listener != null) + " data: " + data);
            if(listener == null) {
                //log.d("type: " + type + " has no event listener registered");
                return;
            }
            listener.on(data);
        });
    }
    protected void put(RTC_EVENT_TYPE type, IPeerConnectionListener<?> handler) {
        eventListeners.put(type, handler);
    }
    @Override
    public void onIceCandidate(IPeerConnectionListener<RtcIceCandidate> handler) {
        put(RTC_EVENT_TYPE.ON_ICE_CANDIDATE, handler);
    }

    @Override
    public void onIceCandidatesRemoved(IPeerConnectionListener<RtcIceCandidate[]> handler) {
        put(RTC_EVENT_TYPE.ON_ICECANDIDATES_REMOVED, handler);
    }

    @Override
    public void onSignalingChange(IPeerConnectionListener<RtcSignalState> handler) {
        put(RTC_EVENT_TYPE.ON_SIGNALING_CHANGE, handler);
    }

    @Override
    public void onIceConnectionChange(IPeerConnectionListener<RtcIceConnectionState> handler) {
        put(RTC_EVENT_TYPE.ON_ICE_CONNECTION_CHANGE, handler);
    }

   /* @Override
    public void onStandardizedIceConnectionChange(IPeerConnectionListener<RtcSignalState> handler) {
        put(RTC_EVENT_TYPE.ON_STANDARDIZED_ICE_CONNECTION_CHANGE, handler);
    }*/

    @Override
    public void onConnectionState(IPeerConnectionListener<RtcConnectionState> handler) {
        put(RTC_EVENT_TYPE.ON_CONNECTION_STATE, handler);
    }

    @Override
    public void onRenegotiationNeeded(IPeerConnectionListener<Void> handler) {
        put(RTC_EVENT_TYPE.ON_RENEGOTIATION_NEEDED, handler);
    }



    protected enum RTC_EVENT_TYPE {
        ON_ICE_CANDIDATE,
        ON_ICECANDIDATES_REMOVED,
        ON_SIGNALING_CHANGE,
        ON_ICE_CONNECTION_CHANGE,
        ON_STANDARDIZED_ICE_CONNECTION_CHANGE,
        ON_CONNECTION_STATE,
        ON_RENEGOTIATION_NEEDED,
    }
}
