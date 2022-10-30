package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import com.supercom.puretrack.data.broadcast_receiver.WatchdogReceiver;
import com.supercom.puretrack.data.service.WatchdogService;
import com.supercom.puretrack.util.application.App;

import java.util.concurrent.TimeUnit;

public class WatchdogManager {

    private static final long WATCHDOG_INTERVAL = TimeUnit.MINUTES.toMillis(1);
    public final int WATCHDOG_REQUEST_CODE = 0;

    WatchdogReceiver watchdogReceiver;

    private static final WatchdogManager INSTANCE = new WatchdogManager();

    private WatchdogManager() {
    }

    public static WatchdogManager sharedInstance() {
        return INSTANCE;
    }

    public void initWatchdogReceiverAlaram(Context context) {
        if (watchdogReceiver == null) {
            watchdogReceiver = new WatchdogReceiver();
        }
        watchdogReceiver.setRepeatingAlarm(context, System.currentTimeMillis(), WATCHDOG_INTERVAL, WatchdogReceiver.class, WATCHDOG_REQUEST_CODE);
    }

    public void cancelWatchdogAlaram(Context context) {
        if (watchdogReceiver != null) {
            watchdogReceiver.CancelAlarm(context, WatchdogReceiver.class, WATCHDOG_REQUEST_CODE, null);
        }

    }

    public boolean isWatchdogeRunning() {
        ActivityManager manager = (ActivityManager) App.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WatchdogService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
