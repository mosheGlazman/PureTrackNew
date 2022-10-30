package com.supercom.puretrack.data.broadcast_receiver;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.model.database.entities.EntityDeviceDetails;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableDeviceDetails;
import com.supercom.puretrack.ui.activity.MainActivity;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewDayStartReceiver extends BaseAlarmManagerBroadcastReciever {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainActivityIntent = new Intent(MainActivity.MAIN_RECEIVER_EXTRA);
        mainActivityIntent.putExtra(MainActivity.MAIN_RECEIVER_EXTRA, MainActivity.NEW_DAY_START_RECEIVER_EXTRA);
        LocalBroadcastManager.getInstance(context).sendBroadcast(mainActivityIntent);
        addApplicationDataUsageToDB(context);
    }

    public void startAlarmManagerReceiver(long timeInMills, int hour, int minute, int second, int dayOfYear,
                                          Class<?> callbackReceiverClass, int requestAlaramCode) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.add(Calendar.DAY_OF_YEAR, dayOfYear);
        setRepeatingAlarm(App.getContext(), calendar.getTimeInMillis(), TimeUnit.DAYS.toMillis(1),
                callbackReceiverClass, requestAlaramCode);
    }

    private void addApplicationDataUsageToDB(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = manager.getRunningAppProcesses();
        for (RunningAppProcessInfo runningApp : runningApps) {
            if (runningApp.processName.equals(App.getContext().getApplicationContext().getPackageName())) {
                int uid = runningApp.uid;

                // Get traffic data
                long currentReceivedData = TrafficStats.getUidRxBytes(uid);
                long currentSentData = TrafficStats.getUidTxBytes(uid);

                long dataReceivedToSend;
                long dataSentToSend;

                EntityDeviceDetails deviceDetails = DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord();

                dataReceivedToSend = currentReceivedData - deviceDetails.receivedUsageDataOfApplication;
                dataSentToSend = currentSentData - deviceDetails.sentDataUsageOfApplication;

                DatabaseAccess.getInstance().UpdateField(EnumDatabaseTables.TABLE_DEVICE_DETAILS,
                        TableDeviceDetails.COLUMN_DEBUG_INFO_RECEIVED_DATA_USAGE, dataReceivedToSend);
                DatabaseAccess.getInstance().UpdateField(EnumDatabaseTables.TABLE_DEVICE_DETAILS,
                        TableDeviceDetails.COLUMN_DEBUG_INFO_SENT_DATA_USAGE, dataSentToSend);

                Calendar calander = Calendar.getInstance();
                long thisDay = calander.getTimeInMillis();

                calander.add(Calendar.DAY_OF_YEAR, -1);
                long lastDay = calander.getTimeInMillis();

                String message = String.format("Cell data usage: Up%s Down%s From%s To%s",
                        dataSentToSend, dataReceivedToSend, lastDay, thisDay);
                TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), message,
                        DebugInfoModuleId.Data_Usage.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

                return;
            }
        }
    }
}
