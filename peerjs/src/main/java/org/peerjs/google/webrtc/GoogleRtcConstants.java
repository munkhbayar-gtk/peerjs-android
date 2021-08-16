package org.peerjs.google.webrtc;

import org.peerjs.configuration.VideoOption;
import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GoogleRtcConstants {
    private static PLog log = PLogFactory.getLogger(GoogleRtcConstants.class);

    static final String VIDEO_TRACK_ID = "ARDAMSv0";
    static final String AUDIO_TRACK_ID = "ARDAMSa0";
    static final String VIDEO_TRACK_TYPE = "video";
    static final String TAG = "PCRTCClient";
    static final String VIDEO_CODEC_VP8 = "VP8";
    static final String VIDEO_CODEC_VP9 = "VP9";
    static final String VIDEO_CODEC_H264 = "H264";
    static final String VIDEO_CODEC_H264_BASELINE = "H264 Baseline";
    static final String VIDEO_CODEC_H264_HIGH = "H264 High";
    static final String VIDEO_CODEC_AV1 = "AV1";
    static final String VIDEO_CODEC_AV1_SDP_CODEC_NAME = "AV1X";
    static final String AUDIO_CODEC_OPUS = "opus";
    static final String AUDIO_CODEC_ISAC = "ISAC";
    static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    static final String VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    static final String VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/";
    static final String DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";

    static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    static final int HD_VIDEO_WIDTH = 1280;
    static final int HD_VIDEO_HEIGHT = 720;

    static final int BPS_IN_KBPS = 1000;
    static final String RTCEVENTLOG_OUTPUT_DIR_NAME = "rtc_event_log";

    private static String joinString(
            Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }

    static String preferCodec(String sdp, String codec, boolean isAudio) {
        final String[] lines = sdp.split("\r\n");
        final int mLineIndex = findMediaDescriptionLine(isAudio, lines);
        if (mLineIndex == -1) {
            //Log.w(TAG, "No mediaDescription line, so can't prefer " + codec);
            log.w("No mediaDescription line, so can't prefer " + codec);
            return sdp;
        }
        // A list with all the payload types with name |codec|. The payload types are integers in the
        // range 96-127, but they are stored as strings here.
        final List<String> codecPayloadTypes = new ArrayList<>();
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        final Pattern codecPattern = Pattern.compile("^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$");
        for (String line : lines) {
            Matcher codecMatcher = codecPattern.matcher(line);
            if (codecMatcher.matches()) {
                codecPayloadTypes.add(codecMatcher.group(1));
            }
        }
        if (codecPayloadTypes.isEmpty()) {
            //Log.w(TAG, "No payload types with name " + codec);
            log.w("No payload types with name " + codec);
            return sdp;
        }

        final String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
        if (newMLine == null) {
            return sdp;
        }
        //Log.d(TAG, "Change media description from: " + lines[mLineIndex] + " to " + newMLine);
        log.d("Change media description from: " + lines[mLineIndex] + " to " + newMLine);
        lines[mLineIndex] = newMLine;
        return joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */);
    }
    /** Returns the line number containing "m=audio|video", or -1 if no such line exists. */
    static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        final String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length; ++i) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }
    static String movePayloadTypesToFront(
            List<String> preferredPayloadTypes, String mLine) {
        // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
        final List<String> origLineParts = Arrays.asList(mLine.split(" "));
        if (origLineParts.size() <= 3) {
            log.e(  "Wrong SDP media description format: " + mLine);
            return null;
        }
        final List<String> header = origLineParts.subList(0, 3);
        final List<String> unpreferredPayloadTypes =
                new ArrayList<>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
        // types.
        final List<String> newLineParts = new ArrayList<>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, " ", false /* delimiterAtEnd */);
    }
    static String getFieldTrials(VideoOption video, AudioHardwareOption audioHardwareOption) {
        String fieldTrials = "";
        if (video.flexfecEnabled) {
            fieldTrials += GoogleRtcConstants.VIDEO_FLEXFEC_FIELDTRIAL;
            log.d("Enable FlexFEC field trial.");
        }
        fieldTrials += GoogleRtcConstants.VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL;
        if (audioHardwareOption.disableWebRtcAGCAndHPF) {
            fieldTrials += GoogleRtcConstants.DISABLE_WEBRTC_AGC_FIELDTRIAL;
            log.d("Disable WebRTC AGC field trial.");
        }
        return fieldTrials;
    }
    static String setStartBitrate(
            String codec, boolean isVideoCodec, String sdp, int bitrateKbps) {
        String[] lines = sdp.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        // Search for codec rtpmap in format
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }
        if (codecRtpMap == null) {
            log.w( "No rtpmap for " + codec + " codec");
            return sdp;
        }
        log.w(  "Found " + codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]);

        // Check if a=fmtp string already exist in remote SDP for this codec and
        // update it with new bitrate parameter.
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                log.w(  "Found " + codec + " " + lines[i]);
                if (isVideoCodec) {
                    lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                }
                log.w(  "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            // Append new a=fmtp line if no such line exist for a codec.
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                if (isVideoCodec) {
                    bitrateSet =
                            "a=fmtp:" + codecRtpMap + " " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " " + AUDIO_CODEC_PARAM_BITRATE + "="
                            + (bitrateKbps * 1000);
                }
                log.w(  "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }
        }
        return newSdpDescription.toString();
    }

    static String getSdpVideoCodecName(String videoCodec) {
        log.e("ERR: " + videoCodec);

        switch (videoCodec) {
            case VIDEO_CODEC_VP8:
                return VIDEO_CODEC_VP8;
            case VIDEO_CODEC_VP9:
                return VIDEO_CODEC_VP9;
            case VIDEO_CODEC_AV1:
                return VIDEO_CODEC_AV1_SDP_CODEC_NAME;
            case VIDEO_CODEC_H264_HIGH:
            case VIDEO_CODEC_H264_BASELINE:
                return VIDEO_CODEC_H264;
            default:
                return VIDEO_CODEC_VP8;
        }
    }
}
