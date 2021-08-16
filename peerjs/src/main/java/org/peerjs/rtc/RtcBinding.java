package org.peerjs.rtc;

public interface RtcBinding {
    boolean isAudioEnabled();
    boolean isVideoEnabled();
    void setVideoEnabled(boolean enabled);
    void setAudioEnabled(boolean enabled);

    double getAudioVolume();
    void setAudioVolume(double volume);
}
