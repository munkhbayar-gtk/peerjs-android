package org.peerjs.message;

import java.util.HashMap;
import java.util.Map;

public class PeerJsMessagePayload {
    private String type;
    private String connectionId;
    private String browser;
    private SdpMessage sdp;
    private SdpMessage candidate;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SdpMessage getCandidate() {
        return candidate;
    }

    public void setCandidate(SdpMessage candidate) {
        this.candidate = candidate;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public SdpMessage getSdp() {
        return sdp;
    }

    public void setSdp(SdpMessage sdp) {
        this.sdp = sdp;
    }
}
