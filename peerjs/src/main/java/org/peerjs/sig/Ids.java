package org.peerjs.sig;

import org.peerjs.Utils;

import java.util.*;

class Ids {
    private static Ids instance;

    static {
        instance = new Ids();
    }

    private Set<String> ids = new HashSet<>();
    private Map<Object, List<String>> allocates = new HashMap<>();
    private Ids(){

    }
    //mc_of7xvm592u//
    //private String mc_of7xvm592u
    private String generateId(Object owner, int len, String prefix){
        String id = null;
        synchronized (ids) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            String token = Utils.randomAlphaNumeric(len);
            sb.append(token);
            id = sb.toString();
            ids.add(id);
        }
        if(!allocates.containsKey(id)){
            allocates.put(owner, new LinkedList<>());
        }
        List<String> ownerIds = allocates.get(owner);
        ownerIds.add(id);
        return id;
    }

    static String newId(Object owner, int len, String prefix){
        return instance.generateId(owner, len, prefix);
    }

    static void revoke(Object owner){
        List<String> ownerIds = instance.allocates.remove(owner);
        if(ownerIds != null){
            synchronized (instance.ids){
                ownerIds.forEach((id)->{
                    instance.ids.remove(id);
                });
            }
        }
    }

}
