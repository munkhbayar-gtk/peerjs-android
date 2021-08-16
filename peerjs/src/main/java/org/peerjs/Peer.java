package org.peerjs;

import org.peerjs.configuration.PeerOptions;
import org.peerjs.event.CloseEvent;
import org.peerjs.event.DefaultEventRunContext;
import org.peerjs.event.AbstractEventRunContext;
import org.peerjs.event.IPeerEventListener;
import org.peerjs.event.PeerEventType;
import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;
import org.peerjs.message.PeerJsMessage;
import org.peerjs.message.PeerJsMessageType;
import org.peerjs.rtc.IRtcPeerConnectionFactory;
import org.peerjs.sig.AbstractSignallingServer;
import org.peerjs.sig.DefaultSignalingServerFactory;
import org.peerjs.sig.ISignallingServerFactory;
import org.peerjs.sig.IWebSocketClientListener;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

public final class Peer {
    private static PLog log = PLogFactory.getLogger(Peer.class);

    private PeerState state = PeerState.INITIAL;

    private String id;
    private Map<PeerEventType, IPeerEventListener<?>> eventListeners = new ConcurrentHashMap<>();
    private Map<String, PeerCall> mediaConnections = new ConcurrentHashMap<>();

    private PeerOptions peerOptions;
    private AbstractSignallingServer serverConnection;
    private ISignallingServerFactory serverFactory;
    private PeerCallFactory peerCallFactory = new PeerCallFactory();
    private AbstractEventRunContext eventRunContext;

    public Peer(String id, PeerOptions options){
        this(id, options, new DefaultSignalingServerFactory(), new DefaultEventRunContext());
    }
    public Peer(String id, PeerOptions options, ISignallingServerFactory serverFactory) {
        this(id, options, serverFactory, new DefaultEventRunContext());
    }
    public Peer(String id, PeerOptions options, AbstractEventRunContext eventRunContext) {
        this(id, options, new DefaultSignalingServerFactory(), eventRunContext);
    }

    public Peer(String id, PeerOptions options, ISignallingServerFactory serverFactory, AbstractEventRunContext eventRunContext) {
        if(serverFactory == null){
            throw new NullPointerException("Signalling Server must not be NULL");
        }
        if(eventRunContext == null) {
            throw new NullPointerException("eventRunContext must not be NULL");
        }
        this.peerOptions = options;
        setId(id);
        this.serverFactory = serverFactory;
        this.eventRunContext = eventRunContext;

        if(peerOptions.getThreadPoolExecutor() != null) {
            this.eventRunContext.setThreadPoolExecutor(peerOptions.getThreadPoolExecutor());
        }

        createRtcConnectionFactoryInstance();
        initPeerJsMessageHandlers();

    }
    private void createRtcConnectionFactoryInstance() {
        IRtcPeerConnectionFactory factory = null;
        try{
            factory = peerOptions.getRtcConnectionFactoryClass().getConstructor().newInstance();
        }catch(Exception ex){
            String logMsg = "Error Creating RtcPeerConnectionFactory instance";
            log.e(logMsg);
            throw new RuntimeException(logMsg, ex);
        }
        this.peerCallFactory.rtcConnectionFactory = factory;
    }
    private void setId(String id){
        //options.setId(id);
        this.id = id;
        try{
            Method setIdMethod = PeerOptions.class.getDeclaredMethod("setId", String.class);
            setIdMethod.setAccessible(true);
            setIdMethod.invoke(peerOptions, id);
            //Field field = PeerOptions.class.getF
        }catch (Exception e) {
            throw new RuntimeException("Error On Setting ID to options", e);
        }
    }
    public boolean isDestroyed() {
        return in(state, PeerState.DESTROYED, PeerState.DESTROYING);
    }
    public boolean isDisconnected(){
        return in(state, PeerState.DISCONNECTED, PeerState.DISCONNECTING);
    }
    public boolean isOpen() {
        return in(state, PeerState.OPEN) && serverConnection != null;
    }
    public boolean isOpenning() {
        return in(state, PeerState.OPENNING);
    }
    public final void establish(){
        log.d("establishing ...");
        if(isDestroyed()) {
            throw new IllegalStateException("this peer is already destroyed. so cannot reuse");
        }
        if(eventListeners.isEmpty()) {
            throw new IllegalStateException("No event listener declared");
        }
        state = PeerState.OPENNING;
        //TODO:
        serverConnection = serverFactory.createServer(peerOptions, webSocketClientListener);

        String tmpId = this.id;
        try{
            if(id == null || id.length() == 0) {
                log.d("Requesting new id");
                tmpId = serverConnection.getId();
            }
            setId(tmpId);
            log.d("peer-id: " + tmpId);
            // connecting to websocket server
            serverConnection.connect();
        }catch (Exception e) {
            fireException(e);
        }
    }

    private void fireException(Throwable e) {
        PeerError error = new PeerError(PeerErrorType.WS_ERROR);
        error.setCause(e);
        fire(PeerEventType.ERROR, error);
    }

    private <Data> void fire(PeerEventType type, Data data) {
        if(isDestroyed()) {
            log.d("peer instance is already dead. No event firing will occur");
            return;
        }
        IPeerEventListener<Object> listener = (IPeerEventListener<Object>) eventListeners.get(type);
        if(listener != null){
            eventRunContext.execute(()->{
                if(log.isDebugEnabled()) {
                    log.d("Firing event: [" + type + ", data="+data+"]");
                    listener.onEvent(data);
                    log.d("Fired event: " + type);
                }else{
                    listener.onEvent(data);
                }
            });
        }else{
            // debug log print: no event listener set for the event type
            if(log.isDebugEnabled()){
                log.d("no event listener set for the event: " + type);
            }
        }
    }
    public PeerOptions getPeerOptions() {
        return peerOptions;
    }

    public PeerCall call(String peerId, IPeerInputStream stream) {
        boolean exists = false;
        synchronized (mediaConnections) {
            if(mediaConnections.containsKey(peerId)) {
                exists = true;
            }else{
                mediaConnections.put(peerId, peerCallFactory.createCallableCall(peerOptions, serverConnection));//new PeerCall(peerOptions));
            }
        }
        if(exists) {
            throw new RuntimeException("peerId already exists: " + peerId);
        }

        PeerCall call = mediaConnections.get(peerId);
        call.on(PeerEventType.CLOSE, (IPeerEventListener<?>) data->{
            removeCall(peerId);
        });
        call.on(PeerEventType.ERROR, (IPeerEventListener<?>) data->{
            removeCall(peerId);
        });
        call.connect(peerId, stream);
        return call;
    }

    public void onOpen(IPeerEventListener<String> listener) {
        eventListeners.put(PeerEventType.OPEN, (IPeerEventListener<String>) id -> {
            state = PeerState.OPEN;
            this.id = id;
            listener.onEvent(id);
        });
    }

    public void onCall(IPeerEventListener<AnswerablePeerCall> listener) {
        eventListeners.put(PeerEventType.CALL, listener);
    }
    public void onClose(IPeerEventListener<CloseEvent> listener){
        eventListeners.put(PeerEventType.CLOSE, listener);
    }
    public void onConnection(IPeerEventListener<PeerDataConnection> listener) {
        eventListeners.put(PeerEventType.CONNECTION, listener);
    }
    public void onDisconnected(IPeerEventListener<Void> listener) {
        eventListeners.put(PeerEventType.DISCONNECTED, listener);
    }
    public void onError(IPeerEventListener<PeerError> listener) {
        eventListeners.put(PeerEventType.ERROR, listener);
    }
    public String getId(){
        return id;
    }
    public void connectToDataChannel() {
        throw new IllegalStateException("Data Channels not supported in this build");
    }
    public void disconnect(){
        state = PeerState.DISCONNECTING;
        if(serverConnection != null) {
            serverConnection.close();
        }
        
    }
    public void reconnect(){

    }
    public void destroy(){
        log.d("destroying ...");

        state = PeerState.DESTROYING;
        eventListeners.clear();
        eventRunContext.finish();
        if(serverConnection != null) {
            serverConnection.close();
        }
        for(String callId : mediaConnections.keySet()){
            PeerCall call = mediaConnections.get(callId);
            call.close();
        }
        mediaConnections.clear();
        disconnect();
        state = PeerState.DESTROYED;
        log.d("destroyed");
    }
    
    private void handleWsError(Throwable cause) {
        //cause.printStackTrace();
        log.d("websocket error");
        PeerError error = new PeerError(PeerErrorType.WS_ERROR);
        error.setCause(cause);
        fire(PeerEventType.ERROR, error);
    }
    private void handleWsClose(int code, String reason) {
        System.out.println("reason: " + reason);
        fire(PeerEventType.CLOSE, new CloseEvent(code, reason));
    }
    private void handleWsOpen(){
        //fire(PeerEventType.OPEN, this.id);
    }
    private void handlePeerJsMessage(PeerJsMessage message){
        String json = new Gson().toJson(message);
        log.d("[peerjs-msg] : " + json + " " + message.getType());
        IPeerEventListener<PeerJsMessage> eventListener = peerJsMessageEventHandler.get(message.getType());
        if(eventListener != null) {
            eventListener.onEvent(message);
        }
        
        //TODO:
    }
    private void handlePeerJsMessageOpen() {
        // Schedule HeartBeat Message To Signaling Server
        fire(PeerEventType.OPEN, this.id);
        serverConnection.startHeartBeating();
    }
    private Map<PeerJsMessageType, IPeerEventListener<PeerJsMessage>> peerJsMessageEventHandler = new HashMap<>();
    private void initPeerJsMessageHandlers() {
        peerJsMessageEventHandler.put(PeerJsMessageType.OPEN, (peerJsMessage)->handlePeerJsMessageOpen());
        peerJsMessageEventHandler.put(PeerJsMessageType.ANSWER, (peerJsMessage)->handleRtcAnswerMessage(peerJsMessage));
        peerJsMessageEventHandler.put(PeerJsMessageType.OFFER, (peerJsMessage)->handleRtcOfferMessage(peerJsMessage));
        peerJsMessageEventHandler.put(PeerJsMessageType.CANDIDATE, (peerJsMessage)->handleRtcCandidateMessage(peerJsMessage));
    }
    private void handleRtcOfferMessage(PeerJsMessage peerJsMessage) {
        // fire call event to the peer instance.
        log.d("OFFER FROM: " + peerJsMessage.getType() + " to " + peerJsMessage.getDst() + ", my-id: " + this.getId());
        //mediaConnections.put()
        PeerCall call = getOrCreateCall(peerJsMessage); //peerCallFactory.createAnswerableCall(peerOptions, peerJsMessage, serverConnection); //new PeerCall(peerOptions, peerJsMessage);

        String id = getCallId(peerJsMessage);
        mediaConnections.put(id, call);
        call.on(PeerEventType.CLOSE, (data)->{
            removeCall(id);
        });
        call.on(PeerEventType.ERROR, (data)->{
            PeerError error = (PeerError)data;
            log.e("Error: " + error);
            removeCall(id);
        });
        call.handleOfferMessage(peerJsMessage);
        fire(PeerEventType.CALL, call);
    }
    private void removeCall(String id){
        if(mediaConnections.containsKey(id)) {
            mediaConnections.remove(id);
        }
    }
    private synchronized PeerCall getOrCreateCall(PeerJsMessage peerJsMessage) {
        String id = getCallId(peerJsMessage);
        if(!mediaConnections.containsKey(id)){
            AnswerablePeerCall call = peerCallFactory.createAnswerableCall(peerOptions, peerJsMessage, serverConnection);
            mediaConnections.put(id, call);
            return call;
        }
        return mediaConnections.get(id);
    }
    private String getCallId(PeerJsMessage peerJsMessage) {
        String id = peerJsMessage.getSrc() + ":" + peerJsMessage.getPayload().getConnectionId();
        return id;
    }
    private void handleRtcAnswerMessage(PeerJsMessage peerJsMessage) {
        String src = peerJsMessage.getSrc(); //getCallId(peerJsMessage);
        PeerCall call = mediaConnections.get(src);

        call.handleAnswerMessage(peerJsMessage);
        //fire(PeerEventType.ANSWER, peerJsMessage);
    }
    private void handleRtcCandidateMessage(PeerJsMessage peerJsMessage) {
        String src = getCallId(peerJsMessage);
        PeerCall call = getOrCreateCall(peerJsMessage); //mediaConnections.get(src);
        log.d("ids: " + mediaConnections.keySet());
        call.handleIceCandidateMessage(peerJsMessage);
    }
    private IWebSocketClientListener<PeerJsMessage> webSocketClientListener =
            new IWebSocketClientListener<PeerJsMessage>() {
                @Override
                public void onOpen() {
                    handleWsOpen();
                }

                @Override
                public void onClose(int code, String reason) {
                    handleWsClose(code, reason);
                }

                @Override
                public void onMessage(PeerJsMessage message) {
                    handlePeerJsMessage(message);
                }

                @Override
                public void onMessage(byte[] bytes) {
                    //DEBUG handle binary message
                }

                @Override
                public void onError(Throwable cause) {
                    handleWsError(cause);
                }
            };

    
    private <T> boolean in(T vl, T ... values) {
        return Utils.in(vl, values);
    }
}
