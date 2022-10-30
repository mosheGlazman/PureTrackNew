/**
 *
 */
package com.supercom.puretrack.data.broadcast_receiver;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class WakingAlarmBroadcastReceiver extends BaseAlarmManagerBroadcastReciever {
    public static String WAKING_ALARM_RECEIVER_CALLBACK_EXTRA_NAME = "WakingAlarmBoadcastReceiver_ExtraName";
    public static final String TAG = "WakingAlarmBoadcastReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void writeToLogs(long triggerlMillis, int requestAlaramCode) {

    }

    @Override
    protected Intent getIntent(Context context, Class<?> callbackReceiverClass, String extraValue, String action) {
        Intent intent = new Intent(context, callbackReceiverClass);
        intent.putExtra(WAKING_ALARM_RECEIVER_CALLBACK_EXTRA_NAME, extraValue);
        intent.setAction(action);

        return intent;
    }
}
