package org.peerjs.event;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbstractEventRunContext {
    private static final int DEFAULT_POOLSIZE = 10;
    private ThreadPoolExecutor executor = null;
    protected AbstractEventRunContext(int poolSize){
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
    }
    protected AbstractEventRunContext(){
        this(DEFAULT_POOLSIZE);
    }
    public void execute(Runnable eventExecutor){
        executor.execute(eventExecutor);
    }
    public void setThreadPoolExecutor(ThreadPoolExecutor newExecutor){
        synchronized (this){
            List<Runnable> tasks = executor.shutdownNow();
            executor = newExecutor;
            tasks.forEach((task)->{executor.execute(task);});
        }
    }

    public final void finish(){
        executor.shutdownNow();
    }
}
