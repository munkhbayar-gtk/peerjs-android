package org.peerjs.google.webrtc;

public class AudioHardwareOption {

    public final boolean disableBuiltInAEC;
    public final boolean disableBuiltInAGC;
    public final boolean disableBuiltInNS;
    public final boolean disableWebRtcAGCAndHPF;

    private AudioHardwareOption(boolean disableBuiltInAEC, boolean disableBuiltInAGC, boolean disableBuiltInNS, boolean disableWebRtcAGCAndHPF) {
        this.disableBuiltInAEC = disableBuiltInAEC;
        this.disableBuiltInAGC = disableBuiltInAGC;
        this.disableBuiltInNS = disableBuiltInNS;
        this.disableWebRtcAGCAndHPF = disableWebRtcAGCAndHPF;
    }

    public static class Builder {
        private boolean disableBuiltInAEC = false;
        private boolean disableBuiltInAGC = false;
        private boolean disableBuiltInNS = false;
        private boolean disableWebRtcAGCAndHPF = false;

        public Builder disableBuiltInAEC(boolean disableBuiltInAEC) {
            this.disableBuiltInAEC = disableBuiltInAEC;
            return this;
        }
        public Builder disableBuiltInAGC(boolean disableBuiltInAGC) {
            this.disableBuiltInAGC = disableBuiltInAGC;
            return this;
        }
        public Builder disableBuiltInNS(boolean disableBuiltInNS) {
            this.disableBuiltInNS = disableBuiltInNS;
            return this;
        }
        public Builder disableWebRtcAGCAndHPF(boolean disableWebRtcAGCAndHPF) {
            this.disableWebRtcAGCAndHPF = disableWebRtcAGCAndHPF;
            return this;
        }

        public AudioHardwareOption build(){
            return new AudioHardwareOption(disableBuiltInAEC, disableBuiltInAGC, disableBuiltInNS, disableWebRtcAGCAndHPF);
        }
    }
}
