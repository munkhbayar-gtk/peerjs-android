package org.peerjs.message;

import java.util.HashMap;
import java.util.Map;

public class SdpMessage extends HashMap<String, String> {

    public int getAsInt(String key) {
        String vl = get(key);
        try{
            return Integer.parseInt(vl);
        }catch (Exception e) {
            return 0;
        }
    }
}
