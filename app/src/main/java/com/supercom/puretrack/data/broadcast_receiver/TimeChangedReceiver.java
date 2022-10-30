package com.supercom.puretrack.data.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.repositories.NetworkRepository;

public class TimeChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        long currentTime = System.currentTimeMillis();
        long lastDeviceTime = NetworkRepository.getInstance().getLastCycleStartTime();

        if (currentTime < lastDeviceTime) {
            App.writeToNetworkLogsAndDebugInfo(TimeChangedReceiver.class.getCanonicalName(), "Time from device was changed to earlier time", DebugInfoModuleId.Receivers);

        }
    }

}
