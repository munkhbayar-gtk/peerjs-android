package org.peerjs.log;

import android.util.Log;

public class AndroidLogFactory extends PLogFactory{
    @Override
    protected boolean isWrappable() {
        return false;
    }

    @Override
    protected PLog create(Class<?> clzz) {
        return this.create(clzz.getCanonicalName());
    }

    private static String toStr(Object obj) {
        return obj != null ? obj.toString() : null;
    }
    @Override
    protected PLog create(String tag) {
        return new PLog() {
            @Override
            public void e(Object msg) {
                Log.e(tag, toStr(msg));
            }

            @Override
            public void e(String msg, Throwable e) {
                Log.e(tag, msg, e);
            }

            @Override
            public void w(Object msg) {
                Log.w(tag, toStr(msg));
            }

            @Override
            public void w(String msg, Throwable e) {
                Log.w(tag, msg, e);
            }

            @Override
            public void d(Object msg) {
                Log.d(tag, toStr(msg));
            }

            @Override
            public void d(String msg, Throwable e) {
                Log.d(tag, msg, e);
            }

            @Override
            public void i(Object msg) {
                Log.i(tag, toStr(msg));
            }

            @Override
            public void i(String msg, Throwable e) {
                Log.i(tag, msg, e);
            }

            @Override
            public void t(Object msg) {
                Log.wtf(tag, toStr(msg));
            }

            @Override
            public void t(String msg, Throwable e) {
                Log.wtf(tag, msg, e);
            }

            @Override
            public void v(Object msg) {
                Log.v(tag, toStr(msg));
            }

            @Override
            public void v(String msg, Throwable e) {
                Log.v(tag, msg, e);
            }
        };
    }
}
