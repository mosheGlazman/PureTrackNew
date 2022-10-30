package com.supercom.puretrack.data.service.heart_beat;

import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.screenLockX;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.lockscreen.LockScreenActivity;
import com.supercom.puretrack.util.application.App;

import java.util.Date;

public class ServiceUtils {

    public static void setTimeout(int screenOffTimeout) {
        int time;
        switch (screenOffTimeout) {
            case 0:
                time = 15000;
                break;
            case 1:
                time = 30000;
                break;
            case 2:
                time = 60000;
                break;
            case 3:
                time = 120000;
                break;
            case 4:
                time = 600000;
                break;
            case 5:
                time = 1800000;
                break;
            default:
                time = -1;
        }

        try{
            Settings.System.putInt(App.getAppContext().getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, time);
        }catch (Exception e) {  }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification getNotificationChannel(Context context) {
        NotificationManager mNotific = null;
        CharSequence name = "Ragav";
        String desc = "this is notific";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        final String ChannelID = "my_channel_03";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotific = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(ChannelID, name,
                    importance);
            mChannel.setDescription(desc);
            mChannel.setLightColor(Color.CYAN);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            mNotific.createNotificationChannel(mChannel);
        }

        String Body = "Heart beat service Is Running";

        Notification n = new Notification.Builder(context, ChannelID)
                .setContentTitle("Heart Beat")
                .setContentText(Body)
                .setBadgeIconType(R.drawable.ic_baseline_heart_broken_240)
                .setNumber(5)
                .setSmallIcon(R.drawable.ic_baseline_heart_broken_24)
                .setAutoCancel(true)
                .build();

        return n;
    }

    public static void turnOnScreen() {
        if(BuildConfig.DEBUG){
            return;
        }

        LockScreenActivity.cancelNextLock =true;

        try {
            Context context = App.getAppContext();
            screenLockX = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLockX.acquire();
            screenLockX.release();

            Log.i("HeartBeatTaskF","turnOnScreen success" );
        } catch (Exception e) {
            Log.e("HeartBeatTaskF","turnOnScreen error",e );
        }
    }
}
