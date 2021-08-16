package org.peerjs;

import org.peerjs.configuration.IceServerOption;
import org.peerjs.configuration.PeerOptions;
import org.peerjs.configuration.VideoOption;
import org.peerjs.log.ConsolePLogFactory;
import org.peerjs.log.PLogFactory;

public class Test {
    void test() {
                     
    }
    public static void main(String[] args) {

        PLogFactory.initLogFactory(new ConsolePLogFactory());

        Peer peer = new Peer(null,
                new PeerOptions.Builder()
                        .host("127.0.0.1")
                        .port(8080)
                        .path("/myapp")
                        .key("peerjs1")
                        .media(true, VideoOption.HD())
                        //.secure()
                        .rtcConfiguration()
                            .iceServers(
                                    IceServerOption.newBuilder().urls("stun:turn2.l.google.com").build()
                                    //IceServerOption.newBuilder().urls("").password("").username("").build()
                            )
                        .peerOptionBuilder()
                        .build());
        //peer.onCall();
        peer.onOpen((id)->{
            System.out.println(id);
            /*try{
                Thread.sleep(10000);
            }catch (Exception e){}
            */
            //peer.destroy();
        });
        peer.onError((error)->{
            System.out.println("Error: " + error);
        });
        peer.onClose((closeEvent)->{
            System.out.println("OnClose: " + closeEvent);
        });
        peer.establish();
    }
}
