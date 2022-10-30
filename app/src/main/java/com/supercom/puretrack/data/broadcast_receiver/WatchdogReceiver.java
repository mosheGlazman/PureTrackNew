package com.supercom.puretrack.data.broadcast_receiver;

import android.content.Context;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.ui.activity.BaseActivity;
import com.supercom.puretrack.ui.activity.MainActivity;

public class WatchdogReceiver extends BaseAlarmManagerBroadcastReciever {
    public static final String TAG = "WatchdogReceiver";

    @Override
    protected void handleOnReceive(Context context) {
        if (!App.isActivityAlreadyRunning(context, MainActivity.class.getName())) {
            BaseActivity.isCameFromLauncherActivityCode = true;
            openMainActivity();
        }
    }

    private void openMainActivity() {
        ((App) App.getContext()).launchPureTracActivity(MainActivity.IS_APPLICATION_STARTED_BY_WATCHDOG);
    }

}
