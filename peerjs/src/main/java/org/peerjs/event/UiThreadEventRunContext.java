package org.peerjs.event;

import android.os.Handler;
import android.os.Looper;

public class UiThreadEventRunContext extends  AbstractEventRunContext{

    private Handler hdlr = new Handler(Looper.getMainLooper());

    @Override
    public void execute(Runnable eventExecutor) {
        hdlr.post(eventExecutor);
    }
}
