package org.peerjs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {
    public static <T> boolean in(T vl, T ... values) {
        for(T v : values) {
            if(vl == v) return true;
        }
        return false;
    }

    public static String randomAlphaNumeric(int len){
        char [] token = new char[len];
        for(int i = 0 ; i < token.length ; i ++) {
            int num = new Random().nextInt(35);
            if(num < 26) {
                token[i] = (char)(num + 'a');
            }else{
                token[i] = (char)(35-num+ 48);
            }
        }
        return new String(token);
    }

    public static <T> List<T> toList(T ... arr){
        List<T> list = new ArrayList<>(arr.length);
        for(T el : arr) {
            list.add(el);
        }
        return list;
    }
}
