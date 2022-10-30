package com.supercom.puretrack.data.service.heart_beat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.supercom.puretrack.ui.activity.TurnOnScreenActivity;

public class TurnOnReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(HeartBeatTaskJava.TAG,"BroadcastReceiver");
            Intent i=new Intent(context, TurnOnScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
}
