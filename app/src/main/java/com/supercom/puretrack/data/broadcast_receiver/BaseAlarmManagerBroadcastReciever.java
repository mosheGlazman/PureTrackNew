package com.supercom.puretrack.data.broadcast_receiver;

import static com.supercom.puretrack.data.source.local.table.TableScannerType.NORMAL_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MAC_SCAN_ENABLED;
import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.MANUFACTURER_ID;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager;

public class BaseAlarmManagerBroadcastReciever extends BroadcastReceiver {

    final public static String ONE_TIME = "onetime";

    @Override
    public void onReceive(Context context, Intent intent) {


        boolean isManufacturerIdEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MANUFACTURER_ID) > 0;
        boolean isMaScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(MAC_SCAN_ENABLED) > 0;
        boolean isNormalScanEnabled = TableScannerTypeManager.sharedInstance().getIntValueByColumnName(NORMAL_SCAN_ENABLED) > 0;
        boolean isDozeModeScanEnabled = isMaScanEnabled || isManufacturerIdEnabled;
        if (!isDozeModeScanEnabled || isNormalScanEnabled) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BaseAlaramManagerBroadcastReciever");
            wl.acquire();

            handleOnReceive(context);

            wl.release();
        }
    }

    public void setRepeatingAlarm(Context context, long triggerlMillis, long intervalMillis, Class<?> callbackReceiverClass, int requestAlaramCode) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, callbackReceiverClass);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestAlaramCode, intent, PendingIntent.FLAG_IMMUTABLE);

        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerlMillis, intervalMillis, pi);
    }

    public void CancelAlarm(Context context, Class<?> callbackReceiverClass, int requestAlaramCode, String action) {
        PendingIntent sender = PendingIntent.getBroadcast(context, requestAlaramCode, getIntent(context, callbackReceiverClass, null, action),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();
    }

    public void setOnetimeTimer(Context context, long triggerlMillis, Class<?> callbackReceiverClass, int requestAlaramCode) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, callbackReceiverClass);
        intent.putExtra(ONE_TIME, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestAlaramCode, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, triggerlMillis, pi);
    }

    @SuppressLint("NewApi")
    public void setAlaramClock(Context context, long triggerlMillis, Class<?> callbackReceiverClass, int requestAlaramCode, String type, String action) {
        writeToLogs(triggerlMillis, requestAlaramCode);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = getIntent(context, callbackReceiverClass, type, action);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestAlaramCode, intent,PendingIntent.FLAG_IMMUTABLE);
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerlMillis, pi), pi);
    }

    protected void writeToLogs(long triggerlMillis, int requestAlaramCode) {

    }

    protected void handleOnReceive(Context context) {
    }

    protected Intent getIntent(Context context, Class<?> callbackReceiverClass, String type, String action) {
        return new Intent(context, callbackReceiverClass);
    }

}
