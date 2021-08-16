package org.peerjs.message;

public class PeerJsMessage {
    private PeerJsMessageType type;
    private String dst;
    private String src;
    private PeerJsMessagePayload payload;

    

    /**
     * @return the src
     */
    public String getSrc() {
        return src;
    }

    /**
     * @param src the src to set
     */
    public void setSrc(String src) {
        this.src = src;
    }

    public PeerJsMessageType getType() {
        return type;
    }

    public void setType(PeerJsMessageType type) {
        this.type = type;
    }

    public PeerJsMessagePayload getPayload() {
        return payload;
    }

    public void setPayload(PeerJsMessagePayload payload) {
        this.payload = payload;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }
}
