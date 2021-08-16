package org.peerjs.google.webrtc;

import android.content.Context;

import org.peerjs.IPeerInputStream;
import org.peerjs.rtc.AbstractRtcPeerConnection;
import org.peerjs.rtc.RtcBinding;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSink;

import java.util.List;

public abstract class CapturerStreamSource implements IPeerInputStream {

    @Override
    public Object getStreamSource() {
        return this;
    }
    protected abstract void onBind(RtcBinding binding);

    //VideoSink localRender, final List<VideoSink> remoteSinks,
    //      final VideoCapturer videoCapturer
    protected abstract VideoSink getLocalVideoRender();
    protected abstract List<VideoSink> getRemoteVideoRenderers();
    protected abstract VideoCapturer createVideoCapturer();
    protected abstract EglBase getEglBase();

    protected abstract PeerConnectionFactory.Options getPeerConnectionFactoryOptions();

    protected abstract AudioHardwareOption getAudioHardwareOption();
    protected abstract boolean isVideoEnabled();

    protected abstract Context getAppContext();

}
