package org.peerjs.sig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.peerjs.log.PLog;
import org.peerjs.log.PLogFactory;

public class DefaultHttpClient implements IHttpClient{
    private static PLog log = PLogFactory.getLogger(DefaultHttpClient.class);
    private static final int HTTP_TIMEOUT_MS = 8000;
    
    @Override
    public String get(String url) throws IOException {
        HttpURLConnection connection =null;
        log.d("request http : " + url);
        try{
            connection = (HttpURLConnection)new URL(url).openConnection() ;
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setConnectTimeout(HTTP_TIMEOUT_MS);
            connection.setReadTimeout(HTTP_TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            InputStream responseStream = null;//connection.getInputStream();
            if(responseCode != 200) {
                responseStream = connection.getErrorStream();
            }else{
                responseStream = connection.getInputStream();
            }
            String resStr = toString(responseStream);
            if(responseCode != 200) {
                throw new IOException("response status: " + responseCode + ", error: " + resStr);
            }
            return resStr;
        }catch (IOException e){
            throw e;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

    }
    private String toString(InputStream input) throws IOException{
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[1024];
        int len = 0;
        while((len = input.read(buf)) != -1){
            byte[] bytes = new byte[len];
            System.arraycopy(buf,0, bytes, 0, len);
            sb.append(new String(bytes));
        }
        return sb.toString();
    }
}
