package org.peerjs.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.peerjs.IPeerInputStream;
import org.peerjs.Peer;
import org.peerjs.configuration.IceServerOption;
import org.peerjs.configuration.PeerOptions;
import org.peerjs.configuration.VideoOption;
import org.peerjs.event.UiThreadEventRunContext;
import org.peerjs.google.webrtc.AudioHardwareOption;
import org.peerjs.google.webrtc.CapturerStreamSource;
import org.peerjs.log.AndroidLogFactory;
import org.peerjs.log.PLogFactory;
import org.peerjs.rtc.AbstractRtcPeerConnection;
import org.peerjs.rtc.RtcBinding;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainPeerActivity extends AppCompatActivity {

    private SurfaceViewRenderer pipVideo;
    private SurfaceViewRenderer localVideo;;
    private TextView txtId;
    private TextView txtStatus;
    private TextView txtFriendId;
    private Peer peer;
    private EglBase eglBase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_peer);
        txtId = findViewById(R.id.txt_id);
        txtStatus = findViewById(R.id.txt_status);
        txtFriendId = findViewById(R.id.txt_friend_id);
        txtStatus.setTextColor(Color.GRAY);

        pipVideo = findViewById(R.id.pip_video_view);
        localVideo = findViewById(R.id.local_video_view);

        eglBase = EglBase.create();

        pipVideo.init(eglBase.getEglBaseContext(), null);
        pipVideo.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        localVideo.init(eglBase.getEglBaseContext(), null);
        localVideo.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        pipVideo.setZOrderMediaOverlay(true);
        pipVideo.setEnableHardwareScaler(true /* enabled */);

        remoteProxyRenderer.setTarget(pipVideo);
        localProxyVideoSink.setTarget(localVideo);
        remoteSinks.add(remoteProxyRenderer);

        findViewById(R.id.btn_call).setOnClickListener((v)->{
            call();
        });
        findViewById(R.id.btn_audio).setOnClickListener((v)->{
            toggleAudio();
        });
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e)->{
            Log.e("Unhandled-Exception", "Error", e);
            defaultHandler.uncaughtException(t, e);
        });
    }

    private void call(){
        String dstId = txtFriendId.getText().toString();
        peer.call(dstId, this::createStream);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "onDestroy: " + (peer == null));
        if(peer != null) {
            Log.e("onDestroy", "onDestroy: " + peer.isOpen());
            peer.destroy();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(peer != null && peer.isOpen()) {
            return;
        }
        PLogFactory.initLogFactory(new AndroidLogFactory());
        String idStr = new Random().nextInt(3) + "";
        peer = new Peer(idStr,//"150b7441-b8bc-49a4-b9aa-ca2bec6b513a",
                new PeerOptions.Builder()
                .host("192.168.1.10")
                .port(9000)
                .key("peerjs1")
                .path("myapp")
                .media(true, new VideoOption.Builder().hwAcceration(false).frameRate(15).build())
                .platform("android")
                .rtcConfiguration()
                    .iceServers(IceServerOption.newBuilder().urls("stun:stun.l.google.com:19302").username("").password("").build())
                    .peerOptionBuilder()
                        .build(), new UiThreadEventRunContext());
        peer.onOpen((id)->{
            txtStatus.setTextColor(Color.GREEN);
            txtStatus.setText("Connected");
            txtId.setText(id);
        });
        peer.onClose((evt)->{
            if(peer.isOpen()){
                txtStatus.setTextColor(Color.GRAY);
                txtStatus.setText("Not connected");
            }
        });
        peer.onError((error)->{
            Log.e("PEER", error.getType().toString(), error.getCause());
        });
        peer.onCall((call)->{
            Log.e("CALL", "Call received: " + call.getInitiatorId());
            call.answer(createStream());
        });
        new Thread(()->{
            peer.establish();
        }).start();

        startScreenCapture();
    }

    private final int CAPTURE_PERMISSION_REQUEST_CODE = 10;
    private static Intent mediaProjectionPermissionResultData = null;
    private static int mediaProjectionPermissionResultCode = 0;
    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
    }

    //@TargetApi(21)
    private @Nullable VideoCapturer createScreenCapturer() {
        Log.e("Capturer", "Creating capturer");
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "User didn't give permission to capture the screen.", Toast.LENGTH_LONG).show();
            //reportError("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Toast.makeText(MainPeerActivity.this, "User revoked permission to capture the screen.", Toast.LENGTH_LONG).show();
            }
        });
    }
    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Log.d("activity", "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }
    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private final List<VideoSink> remoteSinks = new ArrayList<>();

    private void toggleAudio() {
        //rtc.setAudioEnabled(!rtc.isAudioEnabled());
        rtcBinding.setAudioEnabled(!rtcBinding.isAudioEnabled());
    }
    private RtcBinding rtcBinding;
    private IPeerInputStream createStream() {
        return new CapturerStreamSource() {
            @Override
            protected void onBind(RtcBinding _rtcBinging) {
                rtcBinding = _rtcBinging;
                //rtcPeerConnection.on
                remoteSinks.add(remoteProxyRenderer);
            }

            @Override
            protected VideoSink getLocalVideoRender() {
                return localProxyVideoSink;
            }

            @Override
            protected List<VideoSink> getRemoteVideoRenderers() {
                return remoteSinks;
            }

            @Override
            protected VideoCapturer createVideoCapturer() {
                return createScreenCapturer();
            }

            @Override
            protected EglBase getEglBase() {
                return eglBase;
            }

            @Override
            protected PeerConnectionFactory.Options getPeerConnectionFactoryOptions() {
                return new PeerConnectionFactory.Options();
            }

            @Override
            protected AudioHardwareOption getAudioHardwareOption() {
                return new AudioHardwareOption.Builder().build();
            }

            @Override
            protected boolean isVideoEnabled() {
                return true;
            }

            @Override
            protected Context getAppContext() {
                return MainPeerActivity.this.getApplicationContext();
            }
        };
    }
}