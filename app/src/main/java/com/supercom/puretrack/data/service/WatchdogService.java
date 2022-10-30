package com.supercom.puretrack.data.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.supercom.puretrack.data.source.local.local_managers.business_logic.WatchdogManager;

public class WatchdogService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WatchdogManager.sharedInstance().initWatchdogReceiverAlaram(getApplicationContext());
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        WatchdogManager.sharedInstance().cancelWatchdogAlaram(getApplicationContext());
        super.onDestroy();
    }

}
