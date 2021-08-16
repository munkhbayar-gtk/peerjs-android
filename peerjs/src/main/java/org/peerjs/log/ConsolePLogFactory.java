package org.peerjs.log;

import java.io.PrintStream;

public class ConsolePLogFactory extends PLogFactory{

    @Override
    protected PLog create(Class<?> clzz) {
        return create(clzz.getCanonicalName());
    }

    @Override
    protected PLog create(String tag) {
        return new PLog(){

            private void flush(String msg, PrintStream out){
                out.println(logMessage(msg));
            }
            private String logMessage(String msg) {
                StringBuilder sb = new StringBuilder();
                sb.append("[").append(getLevel() + "] ").append("[").append(tag).append("] ").append(msg);
                return sb.toString();
            }
            private void flush(String msg, Throwable e, PrintStream out){
                String log = toString(logMessage(msg), e);
                out.println(log);
            }
            @Override
            public void e(Object msg) {
                flush(msg.toString(), System.err);
            }

            @Override
            public void e(String msg, Throwable e) {
                flush(msg, System.err);
            }

            @Override
            public void w(Object msg) {
                flush(msg.toString(), System.out);
                
            }

            @Override
            public void w(String msg, Throwable e) {
                flush(msg.toString(), e, System.out);
                
            }

            @Override
            public void d(Object msg) {
                flush(msg.toString(), System.out);
                
            }

            @Override
            public void d(String msg, Throwable e) {
                flush(msg.toString(), e, System.out);
                
            }

            @Override
            public void i(Object msg) {
                flush(msg.toString(), System.out);
                
            }

            @Override
            public void i(String msg, Throwable e) {
                flush(msg.toString(), e, System.out);
                
            }

            @Override
            public void t(Object msg) {
                flush(msg.toString(), System.out);
                
            }

            @Override
            public void t(String msg, Throwable e) {
                flush(msg.toString(), e, System.out);
                
            }

            @Override
            public void v(Object msg) {
                flush(msg.toString(), System.out);
                
            }

            @Override
            public void v(String msg, Throwable e) {
                flush(msg.toString(), e, System.out);
            }
            
        };
    }
    
}
