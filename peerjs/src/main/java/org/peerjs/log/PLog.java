package org.peerjs.log;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

public abstract class PLog {
    private PLogLevel level = PLogLevel.DEBUG;

    public PLogLevel getLevel() {
        return level;
    }
    public void setLevel(PLogLevel level) {
        this.level = level;
    }

    private boolean isLevelEnabled(PLogLevel level) {
        return level.value <= this.level.value;
    }
    protected String toString(String msg, Throwable e) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(msg).append("\n");

        StringWriter wr = new StringWriter();
        PrintWriter pwr = new PrintWriter(wr);
        e.printStackTrace(pwr);
        sb.append(wr.toString());
        return sb.toString();
    }
    protected String format(String msg, Object ...args) {
        return MessageFormat.format(msg, args);
    }
    public abstract void e(Object msg);
    public final void e(String msg, Object ... args){
        e(format(msg, args));
    };
    public abstract void e(String msg, Throwable e);
    public final boolean isErrorEnabled(){
        return isLevelEnabled(PLogLevel.ERROR);
    }

    public abstract void w(Object msg);
    public final void w(String msg, Object ... args){
        w(format(msg, args));
    }
    public abstract void w(String msg, Throwable e);
    public final boolean isWarnEnabled(){
        return isLevelEnabled(PLogLevel.ERROR);
    }

    public abstract void d(Object msg);
    public final void d(String msg, Object ... args){
        d(format(msg, args));
    }
    public abstract void d(String msg, Throwable e);
    public final boolean isDebugEnabled(){
        return isLevelEnabled(PLogLevel.DEBUG);
    }

    public abstract void i(Object msg);
    public final void i(String msg, Object ... args){
        i(format(msg, args));
    }
    public abstract void i(String msg, Throwable e);
    public final boolean isInfoEnabled(){
        return isLevelEnabled(PLogLevel.INFO);
    }

    public abstract void t(Object msg);
    public final void t(String msg, Object ... args){
        t(format(msg, args));
    }
    public abstract void t(String msg, Throwable e);
    public final boolean isTraceEnabled(){
        return isLevelEnabled(PLogLevel.TRACE);
    }

    public abstract void v(Object msg);
    public final void v(String msg, Object ... args){
        v(format(msg, args));
    }
    public abstract void v(String msg, Throwable e);
    public final boolean isVerboseEnabled(){
        return isLevelEnabled(PLogLevel.VERBOSE);
    }

}
