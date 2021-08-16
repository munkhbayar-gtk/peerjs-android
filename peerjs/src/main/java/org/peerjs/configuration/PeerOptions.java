package org.peerjs.configuration;

import android.util.Log;

import org.peerjs.google.webrtc.GoogleWebRtcPeerConnectionFactory;
import org.peerjs.rtc.IRtcPeerConnectionFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class PeerOptions {
    private String id;
    private final String key;
    private final String host;
    private final int port;
    private final String path ;
    private final boolean secure;
    
    private final VideoOption video;
    private final AudioOption audio;

    private final long pingInterval;

    private final PeerRtcConfiguration rtcConfiguration;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final Class<? extends IRtcPeerConnectionFactory> rtcConnectionFactoryClass;

    private final String platform;
    public PeerOptions(String key, String host, int port, String path,
                       AudioOption audio, VideoOption video,
                       boolean secure, 
                       long pingInterval,
                       PeerRtcConfiguration rtcConfiguration,
                       ThreadPoolExecutor threadPoolExecutor,
                       Class<? extends IRtcPeerConnectionFactory> rtcConnectionFactoryClass,
                       String platform
    ) {
        this.key = key;
        this.host = host;
        this.port = port;
        this.path = path;
        this.audio = audio;
        this.video = video;
        
        this.secure = secure;
        this.pingInterval = pingInterval;

        this.rtcConfiguration = rtcConfiguration;
        this.threadPoolExecutor = threadPoolExecutor;
        this.rtcConnectionFactoryClass = rtcConnectionFactoryClass;
        this.platform = platform;
    }

    public Class<? extends IRtcPeerConnectionFactory> getRtcConnectionFactoryClass() {
        return rtcConnectionFactoryClass;
    }

    void setId(String id) {
        this.id = id;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public long getPingInterval() {
        return pingInterval;
    }

    public String getKey() {
        return key;
    }

    public boolean isSecure() {
        return secure;
    }

    public PeerRtcConfiguration getRtcConfiguration() {
        return rtcConfiguration;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public boolean isAudio() {
        return audio != null;
    }

    public AudioOption getAudio() {
        return audio;
    }

    public boolean isVideo() {
        return video != null;
    }
    public VideoOption getVideo() {
        return video;
    }

    public String getPlatform() {
        return platform;
    }

    public static class Builder{
        private String key = "peerjs";
        private String host = "/";
        private int port = 9000;
        private String path = "/";
        private VideoOption videoOption = VideoOption.HD();
        private AudioOption audioOption = AudioOption.getDefault();

        private boolean secure = false;
        private long pingInterval = 5000l;

        private ThreadPoolExecutor eventPoolThreadExecutor;
        private Class<? extends IRtcPeerConnectionFactory> rtcConnectionFactoryClass;

        private String platform = "android";

        PeerRtcConfiguration rtcConfiguration;

        public Builder threadPool(ThreadPoolExecutor eventPoolThreadExecutor) {
            this.eventPoolThreadExecutor = eventPoolThreadExecutor;
            return this;
        }

        public Builder pingInterval(long pingInterval) {
            this.pingInterval = pingInterval;
            return this;
        }
        public Builder key(String key) {
            this.key = key;
            return this;
        }
        public Builder host(String host) {
            this.host = host;
            return this;
        }
        public Builder secure() {
            secure = true;
            return this;
        }
        public Builder port(int port) {
            this.port = port;
            return this;
        }
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        public Builder media(Object audio, Object video){
            //this.audio = audio;

            boolean _audio = false;
            if(audio instanceof Boolean) {
                _audio = ((Boolean)audio).booleanValue();
            }else if(audio instanceof AudioOption) {
                _audio = true;
                this.audioOption = (AudioOption)audio;
            }else{
                throw new RuntimeException("'audio' option should be boolean or AudioOption");
            }

            if(!_audio) {
                this.audioOption = null;
            }

            boolean _video = false;
            if(video instanceof Boolean) {
                _video = ((Boolean)video).booleanValue();
            }else if (video instanceof VideoOption) {
                _video = true;
                this.videoOption = (VideoOption)video;
            }else {
                throw new RuntimeException("'video' option should be boolean or VideoOption");
            }
            if(!_video) {
                this.videoOption = null;
            }
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public PeerRtcConfiguration.Builder rtcConfiguration(){
            return new PeerRtcConfiguration.Builder(this);
        }

        public PeerOptions build() {
            if(this.rtcConnectionFactoryClass == null) {
                if(platform.equals("android")) {
                    this.rtcConnectionFactoryClass = GoogleWebRtcPeerConnectionFactory.class;
                }else{
                    throw new RuntimeException("Set RtcConnectionFactotyClass");
                }


                /*
                String callFactoryClassName = System.getProperty("call.factory.class");
                if(callFactoryClassName == null || callFactoryClassName.length() == 0) {
                    throw new NullPointerException("No call factory call was configured!. use system property: [call.factory.class]");
                }
                try{
                    Class<?> clzz = Class.forName(callFactoryClassName);
                    this.callFactoryClass = (Class<? extends IPeerCallFactory>) clzz;
                }catch (Exception e){
                    throw new ClassCastException( callFactoryClassName + " cannot be cast to " + IPeerCallFactory.class.getCanonicalName());
                }
                */
            }
            try{
                rtcConnectionFactoryClass.getConstructor().newInstance();
            }catch (Exception e) {
                throw new IllegalStateException("no default constructor found from : " + this.rtcConnectionFactoryClass.getCanonicalName());
            }
            return new PeerOptions(this.key, this.host, this.port, this.path, this.audioOption, this.videoOption, this.secure,
                    this.pingInterval,
                    this.rtcConfiguration, eventPoolThreadExecutor, this.rtcConnectionFactoryClass, this.platform);
        }
    }
}
