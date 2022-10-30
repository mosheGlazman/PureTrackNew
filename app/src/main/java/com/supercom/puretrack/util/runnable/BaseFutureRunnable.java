package com.supercom.puretrack.util.runnable;

import android.os.Handler;

public class BaseFutureRunnable implements Runnable {

    public void scheduleFutureRun(Handler handler, long timeToMills) {
        handler.removeCallbacks(this);
        handler.postDelayed(this, timeToMills);
    }

    @Override
    public void run() {
    }

}
