package org.peerjs;

import java.io.PrintWriter;
import java.io.StringWriter;

public class PeerError {
    private PeerErrorType type;

    private Throwable cause;
    
    /**
     * @param type
     */
    public PeerError(PeerErrorType type) {
        this.type = type;
    }


    public PeerErrorType getType() {
        return type;
    }
 

    /**
     * @return the cause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * @param cause the cause to set
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[type = ").append(type).append(", trace: ").append(toString(cause)).append("]");
        return sb.toString();
    }
    
    public static String toString(Throwable cause) {
        StringWriter wr = new StringWriter();
        PrintWriter pwr = new PrintWriter(wr);
        cause.printStackTrace(pwr);
        return wr.toString();
    }
}
