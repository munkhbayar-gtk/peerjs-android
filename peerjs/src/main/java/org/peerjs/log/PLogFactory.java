package org.peerjs.log;

public abstract class PLogFactory {
    private static PLogFactory instance;
    public static PLog getLogger(Class<?> clzz){
        PLog log = instance.create(clzz);
        if(instance.isWrappable()) {
            return new LogWrapper(log);
        }
        return log;
    }
    public static PLog getLogger(String tag){
        PLog log = instance.create(tag);
        if(instance.isWrappable()) {
            return new LogWrapper(log);
        }
        return log;
    }

    protected abstract PLog create(Class<?> clzz);
    protected abstract PLog create(String tag);
    protected boolean isWrappable() {
        return true;
    }
    public static void initLogFactory(PLogFactory factory) {
        instance = factory;
    }

    private static class LogWrapper extends PLog {
        private PLog logger;

        public LogWrapper(PLog logger) {
            this.logger = logger;
        }

        @Override
        public void e(Object msg) {
            if(logger.isErrorEnabled()){
                logger.e(msg);
            }
        }

        @Override
        public void e(String msg, Throwable e) {
            if(logger.isErrorEnabled()){
                logger.e(msg, e);
            }
        }

        @Override
        public void w(Object msg) {
            if(logger.isWarnEnabled()){
                logger.w(msg);
            }
        }

        @Override
        public void w(String msg, Throwable e) {
            if(logger.isWarnEnabled()){
                logger.w(msg, e);
            }
        }

        @Override
        public void d(Object msg) {
            if(logger.isDebugEnabled()){
                logger.d(msg);
            }
        }

        @Override
        public void d(String msg, Throwable e) {
            if(logger.isDebugEnabled()){
                logger.d(msg, e);
            }
        }

        @Override
        public void i(Object msg) {
            if(logger.isInfoEnabled()){
                logger.i(msg);
            }
        }

        @Override
        public void i(String msg, Throwable e) {
            if(logger.isInfoEnabled()){
                logger.i(msg, e);
            }
        }

        @Override
        public void t(Object msg) {
            if(logger.isTraceEnabled()){
                logger.t(msg);
            }
        }

        @Override
        public void t(String msg, Throwable e) {
            if(logger.isTraceEnabled()){
                logger.t(msg, e);
            }
        }

        @Override
        public void v(Object msg) {
            if(logger.isVerboseEnabled()){
                logger.v(msg);
            }
        }

        @Override
        public void v(String msg, Throwable e) {
            if(logger.isVerboseEnabled()){
                logger.v(msg, e);
            }
        }
    }

}
