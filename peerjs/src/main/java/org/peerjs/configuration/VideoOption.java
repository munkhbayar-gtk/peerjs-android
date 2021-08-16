package org.peerjs.configuration;

public class VideoOption {
    public final int width;
    public final int height;

    public final int frameRate;
    public final int maxBitrate;
    public final String codec;
    public final boolean flexfecEnabled;
    public final boolean codecHwAcceleration;

    private VideoOption(int width, int height, int videoFrameRate,
                       int videoMaxBitrate,
                       String videoCodec,
                       boolean videoFlexfecEnabled, boolean videoCodecHwAcceleration) {
        this.width = width;
        this.height = height;
        this.frameRate = videoFrameRate;
        this.maxBitrate = videoMaxBitrate;
        this.codec = videoCodec;
        this.flexfecEnabled = videoFlexfecEnabled;
        this.codecHwAcceleration = videoCodecHwAcceleration;
    }

    /**
     *  wigth x height = 1280 x 720
     * @return
     */
    public static VideoOption HD() {
        return build(1280, 720);
    }

    /**
     * wigth x height = 1920 x 1080
     * @return
     */
    public static VideoOption FULL_HD() {
        return build(1920, 1080);
    }
    /**
     * wigth x height = 3840 x 2160
     * @return
     */
    public static VideoOption ULTRA_4K() {
        return build(3840, 2160);
    }

    /**
     * wigth x height = 640 x 480
     * @return
     */
    public static VideoOption VGA() {
        return build(640, 480);
    }
    /**
     * wigth x height = 320 x 240
     * @return
     */
    public static VideoOption QVGA() {
        return build(320, 240);
    }
    private static VideoOption build(int width, int height) {
        return new Builder().build(width, height);
    }
    public static class Builder {
        private final String DEF_VIDEO_CODEC = "VP8";
        private final int DEF_VIDEO_FRAME_RATE=30;
        private final int DEF_MAX_BITRATE = 1700;
        private final boolean DEF_FLEX_FEC = true;
        private final boolean DEF_HW_ACCELERATION = true;

        private String videoCodec = DEF_VIDEO_CODEC;
        private int frameRate = DEF_VIDEO_FRAME_RATE;
        private int maxBitrate = DEF_MAX_BITRATE;
        private boolean flexFec = DEF_FLEX_FEC;
        private boolean hwAcceration = DEF_HW_ACCELERATION;

        private int width = 1280;
        private int height = 720;

        public VideoOption build(int width, int height) {
            return width(width).height(height).build();
        }
        public Builder width(int width) {
            this.width = width;
            return this;
        }
        public Builder height(int height) {
            this.frameRate = frameRate;
            return this;
        }
        public Builder frameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }
        public Builder maxBitrate(int maxBitrate) {
            this.maxBitrate = maxBitrate;
            return this;
        }
        public Builder flexFec(boolean flexFec) {
            this.flexFec = flexFec;
            return this;
        }
        public Builder hwAcceration(boolean hwAcceration) {
            this.hwAcceration = hwAcceration;
            return this;
        }
        public VideoOption build(){
            return new VideoOption(width, height, frameRate, maxBitrate, videoCodec, flexFec, hwAcceration);
        }
    }


}
