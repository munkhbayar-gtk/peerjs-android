package org.peerjs.sig;

import org.peerjs.Utils;
import org.peerjs.configuration.PeerOptions;
import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;
import org.peerjs.message.PeerJsMessage;
import org.peerjs.message.PeerJsMessagePayload;
import org.peerjs.message.PeerJsMessageType;
import org.peerjs.message.SdpMessage;
import org.peerjs.rtc.ISignalling;

import java.io.IOException;
import java.util.Random;

public abstract class AbstractSignallingServer implements ISignalling {
    private static final PLog log = PLogFactory.getLogger(AbstractSignallingServer.class);
    protected final PeerOptions peerOptions;
    protected final IMessageParser messageParser;
    protected final AbstractWebSocketClient wsClient;
    protected final IHttpClient httpClient;
    protected final IWebSocketClientListener<PeerJsMessage> webSocketClientListener;
    private final Thread heartBeatSchedulerThread;
    private boolean running = true;
    AbstractSignallingServer(PeerOptions peerOptions, IWebSocketClientListener<PeerJsMessage> webSocketClientListener) {
        this.peerOptions = peerOptions;
        this.webSocketClientListener = webSocketClientListener;
        messageParser = createMessageParser();
        httpClient = createHttpClient();
        wsClient = createWebSocketClient();
        heartBeatSchedulerThread = new Thread(()->{
            while(running) {
                try{
                    sendHeartBeat();
                    Thread.sleep(peerOptions.getPingInterval());
                }catch (InterruptedException e) {
                    if(running) {
                        log.d("Signalling Server HeartBeat Sender Thread shutdown");
                    }else{
                        log.e("Signalling Server HeartBeat Sender Thread interrupted unexpectedly", e);
                    }
                }
            }
        });
    }

    private void sendHeartBeat(){
        PeerJsMessage heartbeat = new PeerJsMessage();
        heartbeat.setType(PeerJsMessageType.HEARTBEAT);
        this.send(heartbeat);
    }

    protected String getIdUrl() {
        PeerOptions opt = peerOptions;
        StringBuilder sb = getContextPath(opt);
        sb.append(opt.getKey()).append("/").append("id?ts=");
        String ts = generateTs();
        sb.append(ts);

        return (opt.isSecure() ? "https://" : "http://") + sb.toString();
    }
    protected String getWebSocketUrl() {
        PeerOptions opt = peerOptions;
        StringBuilder sb = getContextPath(opt);
        sb.append("peerjs?key=").append(opt.getKey()).append("&id=").append(opt.getId()).append("&token=").append(generateToken());
        return (opt.isSecure() ? "wss://" : "ws://") + sb.toString();
    }
    private StringBuilder getContextPath(PeerOptions opt) {
        StringBuilder sb = new StringBuilder();
        sb
            .append(opt.getHost())
            .append(":")
            .append(opt.getPort()).append("/");
        String path = opt.getPath();
        int idx = 0;
        for(; idx < path.length() ; idx ++ ){
            if(path.charAt(idx) != '/') {
                break;
            }
        }
        if(idx == path.length()) {
            path="";
        }else{
            path = path.substring(idx);
        }
        sb.append(path).append("/");
        return sb;
    }
    public final String getId() throws IOException {
        String idUrl = getIdUrl();
        log.d("ID-URL: " + idUrl);
        return httpClient.get(getIdUrl());
    };
    public final void send(PeerJsMessage message){
        String str = messageParser.toString(message);
        wsClient.send(str);
        if(log.isDebugEnabled()){
            log.d("WebSocket: SENT: " + str);
        }
    }

    public final void close(){
        wsClient.close();
        running = false;
        heartBeatSchedulerThread.interrupt();
        Ids.revoke(this);
    }
    public final void connect() throws IOException {
        String wsUrl = getWebSocketUrl();
        log.d("WS-URL: " + wsUrl);
        wsClient.connect(getWebSocketUrl());
    };

    abstract IHttpClient createHttpClient();
    abstract AbstractWebSocketClient createWebSocketClient();
    protected IMessageParser createMessageParser(){
        return new DefaultMessageParser();
    }
    private static final long RANGE = 7854201L;
    private String generateTs() {
        Random rnd = new Random();
        long a = rnd.nextInt(100) * RANGE;
        long b = rnd.nextInt(100) * RANGE;
        return a + "." + b;
    }
    private String generateToken() {
        //2ab e2y 6erl h
        return Ids.newId(this, 10, ""); //Utils.randomAlphaNumeric(10);

    }

    public void startHeartBeating() {
        heartBeatSchedulerThread.start();
    }

    private PeerJsMessage create(PeerJsMessageType type, SdpMessage msg, String dst) {
        PeerJsMessage message = new PeerJsMessage();
        message.setType(type);
        message.setSrc(peerOptions.getId());
        message.setDst(dst);

        PeerJsMessagePayload payload = new PeerJsMessagePayload();
        payload.setSdp(msg);
        payload.setBrowser(peerOptions.getPlatform());

        //String connectionId = Ids.newId(this, 10, "mc_");
        //payload.setConnectionId(connectionId);
        payload.setType("media");

        message.setPayload(payload);

        return message;
    }
    @Override
    public PeerJsMessage sendOfferSdp(SdpMessage message, String destPeerId) {
        PeerJsMessage peerJsMessage = create(PeerJsMessageType.OFFER, message, destPeerId);
        peerJsMessage.setSrc(null);
        String connectionId = Ids.newId(this, 10, "mc_");
        peerJsMessage.getPayload().setConnectionId(connectionId);
        send(peerJsMessage);
        return peerJsMessage;
    }

    @Override
    public void sendAnswerSdp(SdpMessage message, String destPeerId, String conId) {
        PeerJsMessage peerJsMessage = create(PeerJsMessageType.ANSWER, message, destPeerId);
        peerJsMessage.getPayload().setConnectionId(conId);
        send(peerJsMessage);
    }

    @Override
    public void sendIceCandidate(SdpMessage candidateMessage, String destPeerId, String conId) {
        PeerJsMessage peerJsMessage = create(PeerJsMessageType.CANDIDATE, null, destPeerId);
        peerJsMessage.getPayload().setCandidate(candidateMessage);
        peerJsMessage.getPayload().setConnectionId(conId);
        send(peerJsMessage);
    }

    @Override
    public void sendLocalIceCandidateRemovals(SdpMessage[] candidates, String destPeerId, String conId) {
        throw new RuntimeException("sendLocalIceCandidateRemovals is not implemented in this version");
    }
}
