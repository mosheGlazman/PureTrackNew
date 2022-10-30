package com.supercom.puretrack.data.broadcast_receiver;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;

public class LocationPointReceiver extends BaseAlarmManagerBroadcastReciever {


    private static final String LOCATION_POINT_RECEIVER_CONST = "locationPointReceiverConst";

    public static String START_LOCATION_CONST = "startLocationConst";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String stringExtra = intent.getStringExtra(LOCATION_POINT_RECEIVER_CONST);

        if (stringExtra != null) {
            if (stringExtra.equals(START_LOCATION_CONST)) {
                Intent intentToPass = new Intent();
                intentToPass.putExtra(LocationManager.CHECK_BEST_LOCATION_EXTRA, LocationManager.CHECK_BEST_LOCATION_EXTRA);
                intentToPass.setAction(LocationManager.LOCATION_HANDLER_ACTION_EXTRA);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentToPass);
            }
        }
    }

    @Override
    protected Intent getIntent(Context context, Class<?> callbackReceiverClass, String type, String action) {
        Intent intent = super.getIntent(context, callbackReceiverClass, type, action);
        intent.putExtra(LOCATION_POINT_RECEIVER_CONST, type);
        return intent;
    }
}
