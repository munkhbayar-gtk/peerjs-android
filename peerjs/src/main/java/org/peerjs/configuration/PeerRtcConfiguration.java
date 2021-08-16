package org.peerjs.configuration;

public class PeerRtcConfiguration {
    /*
            TODO:
                implement other configuration fields
                bundlePolicy,
                rtcpMuxPolicy,
                certificate,
                tcpCandidatePolicy,

                ...
             */
    public static Builder newBuilder() {
        return new Builder(null);
    }
    private PeerRtcConfiguration(IceServerOption[] iceServers){
        this.iceServers = iceServers;

    }
    public final IceServerOption[] iceServers;

    public static class Builder {
        private IceServerOption[] iceServers;
        private final PeerOptions.Builder peerOptionBuilder;
        Builder(PeerOptions.Builder peerOptionBuilder) {
            this.peerOptionBuilder = peerOptionBuilder;
        }
        public Builder iceServers(IceServerOption ... iceServerOptions) {
            this.iceServers = iceServerOptions;
            return this;
        }

        public PeerOptions.Builder peerOptionBuilder() {
            this.peerOptionBuilder.rtcConfiguration = new PeerRtcConfiguration(iceServers);
            return this.peerOptionBuilder;
        }
    }
}
