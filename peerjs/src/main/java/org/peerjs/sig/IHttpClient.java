package org.peerjs.sig;

import java.io.IOException;

public interface IHttpClient {
    String get(String url) throws IOException;
}
