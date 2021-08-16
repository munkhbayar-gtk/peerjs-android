package org.peerjs.configuration;

public class AudioOption {
    public final int startBitrate;
    public final String codec;
    public final boolean audioProcessing;

    private AudioOption(int startBitrate, String codec, boolean audioProcessing) {
        this.startBitrate = startBitrate;
        this.codec = codec;
        this.audioProcessing = audioProcessing;
    }

    public static AudioOption getDefault() {
        return new Builder().build();
    }

    public static class Builder {
        private static final int DEF_BITRATE = 32;
        private static final String DEF_CODEC = "OPUS";
        private static final boolean DEF_AUDIO_PROCESSING=true;

        private int audioStartBitrate = DEF_BITRATE;
        private String codec = DEF_CODEC;
        private boolean audioProcessing = DEF_AUDIO_PROCESSING;

        public Builder startBitrate(int startBitrate){
            audioStartBitrate = startBitrate;
            return this;
        }
        public Builder codec(String codec){
            this.codec = codec;
            return this;
        }

        public Builder audioProcessing(boolean audioProcessing){
            this.audioProcessing = audioProcessing;
            return this;
        }

        public AudioOption build() {
            return new AudioOption(audioStartBitrate, codec, audioProcessing);
        }

    }

}
