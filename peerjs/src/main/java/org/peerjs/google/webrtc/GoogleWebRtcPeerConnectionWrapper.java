package org.peerjs.google.webrtc;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.peerjs.IPeerInputStream;
import org.peerjs.Peer;
import org.peerjs.PeerState;
import org.peerjs.Utils;
import org.peerjs.configuration.AudioOption;
import org.peerjs.configuration.IceServerOption;
import org.peerjs.configuration.PeerOptions;
import org.peerjs.configuration.PeerRtcConfiguration;
import org.peerjs.configuration.VideoOption;
import org.peerjs.event.PeerEventType;
import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;
import org.peerjs.rtc.AbstractRtcPeerConnection;
import org.peerjs.rtc.ISdpCreateListener;
import org.peerjs.rtc.ISdpSetListener;
import org.peerjs.rtc.RtcBinding;
import org.peerjs.rtc.RtcIceConnectionState;
import org.peerjs.rtc.RtcSignalState;
import org.peerjs.rtc.sdp.RtcIceCandidate;
import org.peerjs.rtc.sdp.RtcSessionDescription;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpParameters;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GoogleWebRtcPeerConnectionWrapper extends AbstractRtcPeerConnection {
    private static final PLog log = PLogFactory.getLogger(GoogleWebRtcPeerConnectionWrapper.class);

    //private static final String AUDIO_CODEC_ISAC = "ISAC";

    private PeerOptions peerOptions;
    private PeerConnection peerConnection;
    private PeerConnectionFactory factory;
    private CapturerStreamSource streamSource;

    GoogleWebRtcPeerConnectionWrapper(PeerOptions peerOptions) {
        this.peerOptions = peerOptions;
        queuedRemoteCandidates = new ArrayList<>();
    }

    private RtcBinding rtcBinding = new RtcBinding() {
        @Override
        public boolean isAudioEnabled() {
            return localAudioTrack.enabled();
        }

        @Override
        public boolean isVideoEnabled() {
            return localVideoTrack.enabled();
        }

        @Override
        public void setVideoEnabled(boolean enabled) {
            GoogleWebRtcPeerConnectionWrapper.this.setLocalVideoEnabled(enabled);
        }

        @Override
        public void setAudioEnabled(boolean enabled) {
            GoogleWebRtcPeerConnectionWrapper.this.setAudioEnabled(enabled);
        }

        @Override
        public double getAudioVolume() {
            return vol;
        }
        private double vol;
        @Override
        public void setAudioVolume(double volume) {
            vol = volume;
            GoogleWebRtcPeerConnectionWrapper.this.setAudioVolume(volume);
        }
    };

    private PeerConnection.Observer pcConnectionListener = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            log.d("onSignalingChange: " + signalingState);
            fire(RTC_EVENT_TYPE.ON_SIGNALING_CHANGE, RtcSignalState.valueOf(signalingState.name()));
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            fire(RTC_EVENT_TYPE.ON_CONNECTION_STATE, RtcIceConnectionState.valueOf(iceConnectionState.name()));
            log.d("OnPeerConnectionState: " + iceConnectionState);
            if(Utils.in(iceConnectionState, PeerConnection.IceConnectionState.CONNECTED)) {
                //streamSource.onBind(GoogleWebRtcPeerConnectionWrapper.this);
            }
            if(Utils.in(iceConnectionState, PeerConnection.IceConnectionState.DISCONNECTED)) {

            }
            /*
            if(iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
                state = PeerState.OPEN;
            }else {
                if(Utils.in(iceConnectionState,
                        PeerConnection.IceConnectionState.CLOSED,
                        PeerConnection.IceConnectionState.DISCONNECTED,
                        PeerConnection.IceConnectionState.FAILED
                        )){
                    state = PeerState.DISCONNECTED;
                }else{
                    state = PeerState.INITIAL;
                }
            }
             */
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            RtcIceCandidate candidate = convert(iceCandidate);
            fire(RTC_EVENT_TYPE.ON_ICE_CANDIDATE, candidate);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            fire(RTC_EVENT_TYPE.ON_ICECANDIDATES_REMOVED, convert(iceCandidates));
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {
            fire(RTC_EVENT_TYPE.ON_RENEGOTIATION_NEEDED, null);
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        }
    };
    private void initializePeerConnectionFactory(){
        Context appContext = streamSource.getAppContext();
        AudioHardwareOption audioHardwareOption = streamSource.getAudioHardwareOption();
        String fieldTrials = GoogleRtcConstants.getFieldTrials(peerOptions.getVideo(), audioHardwareOption);
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                        .setFieldTrials(fieldTrials)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
    }
    @Override
    public void createPeerConnection(IPeerInputStream stream) {
        streamSource = (CapturerStreamSource) stream.getStreamSource();
        streamSource.onBind(rtcBinding);
        execute(()->{
            // 1. initialize peer connection factory
            initializePeerConnectionFactory();
            // 2. create PeerConnectionFactory
            initPeerConnectionFactory();
            // 3. init media constraints
            createMediaConstraint();
            // 4. init physical peer connection
            createPhysicalPeerConnection();

            startStreaming();
        });
    }
    private void startStreaming(){
        if(this.videoCapturer != null) {
            VideoOption video = peerOptions.getVideo();
            try{
                videoCapturer.startCapture(video.width, video.height, video.frameRate);
                log.d("Video capturer started ...");
            }catch (Exception e) {
                log.e("ERR", e);
            }

        }
    }
    private void createPhysicalPeerConnection() {
        log.d("Creating physical peer connection ...");
        // 1. create
        PeerConnection.RTCConfiguration rtcConfig = createRtcConfig(peerOptions.getRtcConfiguration());
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = true;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        Log.d("rtcConfig,", new Gson().toJson(rtcConfig));
        peerConnection = factory.createPeerConnection(rtcConfig, this.pcConnectionListener);
        log.d("Created physical peer connection.");
        /*
        TODO: data channel initialize here
        * */
        List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
        boolean videoEnabled = localVideoRender && peerOptions.isVideo(); //isVideoEnabled();
        log.d("Adding video track. : " + videoEnabled);
        if(videoEnabled) {
            peerConnection.addTrack(createLocalVideoTrack(streamSource), mediaStreamLabels);

            // We can add the renderers right away because we don't need to wait for an
            // answer to get the remote track.
            List<VideoSink> remoteSinks = streamSource.getRemoteVideoRenderers();
            remoteVideoTrack = getRemoteVideoTrack();
            remoteVideoTrack.setEnabled(localVideoRender);
            for (VideoSink remoteSink : remoteSinks) {
                remoteVideoTrack.addSink(remoteSink);
            }
        }
        log.d("Adding audio track.");
        peerConnection.addTrack(createAudioTrack(), mediaStreamLabels);

        if(videoEnabled) {
            findVideoSender();
        }
        //
    }
    private VideoTrack getRemoteVideoTrack() {
        for (RtpTransceiver transceiver : peerConnection.getTransceivers()) {
            MediaStreamTrack track = transceiver.getReceiver().track();
            if (track instanceof VideoTrack) {
                return (VideoTrack) track;
            }
        }
        return null;
    }

    private boolean localVideoRender = true;
    private VideoCapturer videoCapturer;
    private EglBase eglBase;
    private RtpSender localVideoSender;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    private SurfaceTextureHelper surfaceTextureHelper;
    private VideoSource videoSource;

    private AudioSource audioSource;
    private AudioTrack localAudioTrack;
    private boolean audioState;

    private void _setVideoMaxBitRate(Integer maxBitrateKbps){
        if(peerConnection == null || localVideoSender == null) {
            log.d("LocalVideoSender is not ready");
            return;
        }
        RtpParameters parameters = localVideoSender.getParameters();
        if(parameters.encodings.isEmpty()){
            log.d("LocalVideoSender's parameters are not ready");
            return;
        }
        for (RtpParameters.Encoding encoding : parameters.encodings) {
            // Null value means no limit.
            encoding.maxBitrateBps = maxBitrateKbps == null ? null : maxBitrateKbps * GoogleRtcConstants.BPS_IN_KBPS;
        }
        if(!localVideoSender.setParameters(parameters)){
            log.e("LocalVideoSender's parameter setting failed");
        }else{
            //Log.d(TAG, "Configured max video bitrate to: " + maxBitrateKbps);
            log.d("Configured max video bitrate to: " + maxBitrateKbps);
        }
    }
    public void setVideoMaxBitRate(final Integer maxBitrateKbps){
        execute(()->{
            _setVideoMaxBitRate(maxBitrateKbps);
        });
    }
    public void setAudioEnabled(boolean enabled) {
        audioState = enabled;
        execute(
                ()->{
                    if(localAudioTrack != null){
                        localAudioTrack.setEnabled(audioState);
                    }
                }
        );
    }
    public boolean isAudioEnabled() {
        return audioState;
    }
    public void setAudioVolume(double volume) {
        execute(()->{
            if(localAudioTrack != null) {
                localAudioTrack.setVolume(volume);
            }
        });
    }
    public void setLocalVideoEnabled(boolean enabled) {
        localVideoRender = enabled;
        execute(()->{
            if(localVideoTrack != null){
                localVideoTrack.setEnabled(localVideoRender);
            }
        });
    }
    private AudioTrack createAudioTrack() {
        audioState = peerOptions.isAudio();

        audioSource = factory.createAudioSource(audioConstaints);
        localAudioTrack = factory.createAudioTrack(GoogleRtcConstants.AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(audioState);
        log.d("AUDIO-STATE: " + audioState);
        return localAudioTrack;
    }

    private VideoTrack createLocalVideoTrack(
            CapturerStreamSource streamSource
            ) {
        videoCapturer = streamSource.createVideoCapturer();
        VideoSink localRender = streamSource.getLocalVideoRender();
        eglBase = streamSource.getEglBase();
        Context appContext = streamSource.getAppContext();

        surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        videoSource = factory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, appContext, videoSource.getCapturerObserver());

        //VideoOption video = peerOptions.getVideo();
        //videoCapturer.startCapture(video.width, video.height, video.frameRate);

        localVideoTrack = factory.createVideoTrack(GoogleRtcConstants.VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(localVideoRender);
        localVideoTrack.addSink(localRender);
        return localVideoTrack;
    }

    private boolean isVideoEnabled() {
        return localVideoRender && videoCapturer != null;
    }
    private boolean preferIsac;
    private void initPeerConnectionFactory(){
        AudioOption audio = peerOptions.getAudio();
        VideoOption video = peerOptions.getVideo();

        //CapturerStreamSource streamSource = (CapturerStreamSource) stream.getStreamSource();
        EglBase eglBase = streamSource.getEglBase();


        // 1. init PeerConnectionFactory
        preferIsac = audio.codec != null && audio.codec.equals(GoogleRtcConstants.AUDIO_CODEC_ISAC);
        //NODE: can save mic input to a file, setup here


        final boolean enableH264HighProfile =
                GoogleRtcConstants.VIDEO_CODEC_H264_HIGH.equals(video.codec);
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        if(video.codecHwAcceleration) {
            encoderFactory = new DefaultVideoEncoderFactory(
                    eglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, enableH264HighProfile);
            decoderFactory = new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());
        }else{
            encoderFactory = new SoftwareVideoEncoderFactory();
            decoderFactory = new SoftwareVideoDecoderFactory();
        }

        PeerConnectionFactory.Options factoryOptions = streamSource.getPeerConnectionFactoryOptions();
        factory = PeerConnectionFactory.builder()
                .setOptions(factoryOptions)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }
    private MediaConstraints createAudioConstaint() {
        AudioOption audio = peerOptions.getAudio();
        MediaConstraints constraints = new MediaConstraints();
        if(!audio.audioProcessing) {
            constraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(GoogleRtcConstants.AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            constraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(GoogleRtcConstants.AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            constraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(GoogleRtcConstants.AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            constraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(GoogleRtcConstants.AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }
        return constraints;
    }

    private MediaConstraints audioConstaints;
    private MediaConstraints sdpConstraints;
    private void createMediaConstraint() {

        audioConstaints = createAudioConstaint();

        sdpConstraints = new MediaConstraints();

        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "" + peerOptions.isAudio()));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", Boolean.toString(isVideoEnabled())));
        //sdpConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    }

    private RtcSessionDescription convert(SessionDescription sessionDescription){
        String type = sessionDescription.type.canonicalForm();
        String desc = sessionDescription.description;
        return new RtcSessionDescription(type, desc);
    }

    private SessionDescription convert(RtcSessionDescription rtcSdp){
        String type = rtcSdp.type;
        String sdp = rtcSdp.sdp;
        return new SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp);
    }
    @Override
    public void createOffer(ISdpCreateListener listener){//, MediaConstraints mediaConstraints) {
        createMediaConstraint();//convert(mediaConstraints);
        execute(()->{
            peerConnection.createOffer(getCreateSdpObserver("createOffer", listener), sdpConstraints);
        });
    }

    private SdpObserver getCreateSdpObserver(String name, ISdpCreateListener listener) {
        return new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription desc) {
                String sdp = desc.description;
                if (preferIsac) {
                    sdp = GoogleRtcConstants.preferCodec(sdp, GoogleRtcConstants.AUDIO_CODEC_ISAC, true);
                }
                if (isVideoEnabled()) {
                    sdp = GoogleRtcConstants.preferCodec(sdp, GoogleRtcConstants.getSdpVideoCodecName(peerOptions.getVideo().codec), false);
                }
                final SessionDescription newDesc = new SessionDescription(desc.type, sdp);
                listener.onCreateSuccess(convert(newDesc));
            }

            @Override
            public void onSetSuccess() {
                log.e(name + "/onSetSuccess: SET success: NODE: it is not supposed to be called!");
            }

            @Override
            public void onCreateFailure(String s) {
                listener.onCreateFailure(s);
            }

            @Override
            public void onSetFailure(String s) {
                log.e(name + "/onSetFailure: " +s+": NODE: It is not supposed to be called!");
            }
        };
    }
    private SdpObserver getSetSdpObserver(String name, ISdpSetListener listener, Runnable onSuccess) {
        return new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                log.d(name + "/onCreateSuccess: NODE: It is not supposed to be called!");
            }

            @Override
            public void onSetSuccess() {
                listener.onSetSuccess();
                onSuccess.run();
            }

            @Override
            public void onCreateFailure(String s) {
                log.d(name + "/onCreateFailure: "+s+": NODE: it is not supposed to be called!");
            }

            @Override
            public void onSetFailure(String s) {
                listener.onSetFailure(s);
            }
        };
    }
    @Override
    public void createAnswer(ISdpCreateListener listener){//, MediaConstraints mediaConstraints) {
        //MediaConstraints mContraint = createMediaConstraint();//convert(mediaConstraints);
        execute(()->{

            peerConnection.createAnswer(getCreateSdpObserver("createAnswer", listener), sdpConstraints);
        });
    }

    @Override
    public void setLocalDescription(ISdpSetListener listener, RtcSessionDescription rtcSdp) {
        execute(()->{
            String sdp = rtcSdp.sdp;
            if (preferIsac) {
                sdp = GoogleRtcConstants.preferCodec(sdp, GoogleRtcConstants.AUDIO_CODEC_ISAC, true);
            }
            if (isVideoEnabled()) {
                sdp = GoogleRtcConstants.preferCodec(sdp, GoogleRtcConstants.getSdpVideoCodecName(peerOptions.getVideo().codec), false);
            }
            SessionDescription desc = convert(rtcSdp);

            final SessionDescription newDesc = new SessionDescription(desc.type, sdp);
            log.d("Seting LocalDescription ...");
            peerConnection.setLocalDescription(getSetSdpObserver("setLocalDescription", listener, ()->{
                log.d("Done Seting LocalDescription");
                drainCandidates();
                int maxBitRate = peerOptions.getVideo().maxBitrate;
                if(maxBitRate > 0) {
                    setVideoMaxBitRate(maxBitRate);
                }
            }), newDesc);
        });
    }

    @Override
    public void setRemoteDescription(ISdpSetListener listener, RtcSessionDescription sdp) {
        execute(()->{
            _setRemoteDescription(listener, sdp);
        });

    }
    private void _setRemoteDescription(ISdpSetListener listener, RtcSessionDescription rtcSdp) {
        if(peerConnection == null ){
            return;
        }
        SessionDescription desc = convert(rtcSdp);
        String sdp = desc.description;
        if (preferIsac) {
            sdp = GoogleRtcConstants.preferCodec(sdp, GoogleRtcConstants.AUDIO_CODEC_ISAC, true);
        }
        if (isVideoEnabled()) {
            sdp = GoogleRtcConstants.preferCodec(sdp, GoogleRtcConstants.getSdpVideoCodecName(peerOptions.getVideo().codec), false);
        }
        int audioBitRate = peerOptions.getAudio().startBitrate;
        if( audioBitRate > 0){
            sdp = GoogleRtcConstants.setStartBitrate(
                    GoogleRtcConstants.AUDIO_CODEC_OPUS, false, sdp, audioBitRate);
        }
        SessionDescription newDesc = new SessionDescription(desc.type, sdp);
        log.d("Setting RemoteDescription1 ...");
        //log.d("DESC: " + new Gson().toJson(desc));

        peerConnection.setRemoteDescription(getSetSdpObserver("setRemoteDescription", listener, ()->{
            log.d("Done Setting RemoteDescription");
            drainCandidates();
        }), newDesc);
    }
    @Override
    public void setConfiguration(PeerRtcConfiguration configuration) {
        PeerConnection.RTCConfiguration rtcConfig = createRtcConfig(configuration);
        peerConnection.setConfiguration(rtcConfig);
    }


    private List<IceCandidate> queuedRemoteCandidates = null;
    private void drainCandidates() {
        if (queuedRemoteCandidates != null) {
            log.d("Add " + queuedRemoteCandidates.size() + " remote candidates");
            for (IceCandidate candidate : queuedRemoteCandidates) {
                peerConnection.addIceCandidate(candidate);
            }

        }
        queuedRemoteCandidates = null;
    }
    @Override
    public boolean addIceCandidate(RtcIceCandidate iceCandidate) {
        log.d("STATE:" + (peerConnection != null) + " " + (queuedRemoteCandidates != null));
        IceCandidate candidate = convert(iceCandidate);
        execute(()->{
            if(queuedRemoteCandidates != null) {
                queuedRemoteCandidates.add(candidate);
            }else{
                peerConnection.addIceCandidate(candidate);
            }
        });
        return true;
    }

    @Override
    public boolean removeIceCandidates(RtcIceCandidate[] iceCandidates) {
        execute(()->{
            peerConnection.removeIceCandidates(convert(iceCandidates));
        });
        return true;
    }

    @Override
    public RtcSessionDescription getLocalDescription() {
        SessionDescription sdp = peerConnection.getLocalDescription();
        return convert(sdp);
    }

    @Override
    public RtcSessionDescription getRemoteDescription() {
        SessionDescription sdp = peerConnection.getRemoteDescription();
        return convert(sdp);
    }

    // ############################### Helper Methods.#########################
    private PeerConnection.RTCConfiguration createRtcConfig(PeerRtcConfiguration peerRtcConfig) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(createIceServers(peerRtcConfig));
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = true;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

        return rtcConfig;
    };
    private List<PeerConnection.IceServer> createIceServers(PeerRtcConfiguration peerRtcConfig){
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        for(IceServerOption iceServer : peerRtcConfig.iceServers) {
            iceServers.add(convert(iceServer));
        }
        //Arrays.stream(peerRtcConfig.iceServers).filter()
        return iceServers;
    }
    private PeerConnection.IceServer convert(IceServerOption iceServer) {
        List<String> urls = Utils.toList(iceServer.urls);
        String username = iceServer.username != null ? iceServer.username : "";
        String password = iceServer.password != null ? iceServer.password : "";

        return PeerConnection.IceServer.builder(urls)
                .setPassword(password)
                .setUsername(username)
                .createIceServer();
    }
    private RtcIceCandidate convert(IceCandidate iceCandidate) {
            /*
            public final String sdpMid;
            public final int sdpMLineIndex;
            public final String sdp;
             */
        //String sdpMid, int sdpMLineIndex, String candidate
        return new RtcIceCandidate(iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp);
    }
    private IceCandidate convert(RtcIceCandidate rtcIceCandidate) {
        return new IceCandidate(rtcIceCandidate.sdpMid, rtcIceCandidate.sdpMLineIndex, rtcIceCandidate.candidate);
    }
    private IceCandidate[] convert(RtcIceCandidate[] rtcIceCandidates) {
        IceCandidate[] ret = new IceCandidate[rtcIceCandidates.length];
        for(int i = 0 ; i < rtcIceCandidates.length ; i ++){
            ret[i] = convert(rtcIceCandidates[i]);
        }
        return ret;
    }
    private RtcIceCandidate[] convert(IceCandidate[] iceCandidates) {
        RtcIceCandidate[] rtcIceCandidates = null;
        if(iceCandidates != null) {
            rtcIceCandidates = new RtcIceCandidate[iceCandidates.length];
            for(int i = 0 ; i < iceCandidates.length ; i ++){
                rtcIceCandidates[i] = convert(iceCandidates[i]);
            }
        }
        return rtcIceCandidates;
    }
    private void findVideoSender() {
        for (RtpSender sender : peerConnection.getSenders()) {
            if (sender.track() != null) {
                String trackType = sender.track().kind();
                if (trackType.equals(GoogleRtcConstants.VIDEO_TRACK_TYPE)) {
                    //Log.d(TAG, "Found video sender.");
                    log.d("Found video sender");
                    localVideoSender = sender;
                }
            }
        }
    }


    @Override
    public void close() {
        execute(this::closeInternal);
        super.close();

    }
    private void closeInternal() {
        if(peerConnection != null) {
            peerConnection.close();
        }
        if(audioSource != null){
            audioSource.dispose();
        }
        try{
            if(videoCapturer != null) {
                videoCapturer.stopCapture();
                videoCapturer.dispose();
            }
        }catch (Exception e){
            log.e("videocapturer stopping: ", e);
        }
        if(surfaceTextureHelper != null ){
            surfaceTextureHelper.dispose();
        }
        if(factory != null) {
            factory.dispose();
        }
        if(eglBase != null) {
            eglBase.release();
        }
    }
    public final PeerConnection getNativeRtcConnectionObject(){
        return this.peerConnection;
    }
}
