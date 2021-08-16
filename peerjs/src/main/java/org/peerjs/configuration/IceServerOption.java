package org.peerjs.configuration;

public class IceServerOption {

    public static Builder newBuilder() {
        return new Builder();
    }

    public final String [] urls;
    public final String username;
    public final String password;

    private IceServerOption(String[] urls, String username, String password) {
        this.urls = urls;
        this.username = username;
        this.password = password;
    }

    public static class Builder{
        private String [] urls;
        private String username;
        private String password;
        private Builder(){
        }
        public Builder urls(String... urls){
            this.urls = urls;
            return this;
        }
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        public IceServerOption build(){
            return new IceServerOption(urls, username, password);
        }
    }
}
