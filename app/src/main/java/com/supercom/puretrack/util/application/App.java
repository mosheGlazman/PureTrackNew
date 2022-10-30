package com.supercom.puretrack.util.application;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.webkit.WebView;

import com.supercom.puretrack.data.cycle.CycleService;
import com.supercom.puretrack.data.repositories.KnoxProfileConfig;
import com.supercom.puretrack.data.service.heart_beat.HeartBeatServiceJava2;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.ProximitySensorManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.activity.BaseActivity;
import com.supercom.puretrack.ui.activity.CheckPermissionActivity;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.ui.views.ToolbarViewsDataManager;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.hardware.AppsSharedDataManager;
import com.supercom.puretrack.util.hardware.FilesManager;

import java.util.List;
import java.util.concurrent.TimeUnit;
import androidx.core.app.ActivityCompat;

public class App extends Application {
    public static final String LOG_TAG = "MainApp";
    public static boolean IS_PINCODE_TYPED = false;


    private static Context _context;
    public static Context applicationContext;


    @Override
    public void onCreate() {
        super.onCreate();
        //new WebView(this).destroy();

        applicationContext = getApplicationContext();
        _context= getApplicationContext();

        CheckPermissionActivity.startIfRequired(getApplicationContext());
        KnoxProfileConfig.getInstance();

        AppsSharedDataManager.getInstance().listenToAskAllowedIncomingList();
        ToolbarViewsDataManager.getInstance(applicationContext).register();

        CycleService.Companion.start(getApplicationContext());
/*
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                printStuckTraceToFile(e, true);
                e.printStackTrace();
            }
        });
*/

        HeartBeatServiceJava2.start();

        ProximitySensorManager.getInstance().register();
    }


    public void restartApplication() {
        Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
        mStartActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(2);
    }

    public String printStuckTraceToFile(Throwable e, boolean shouldWriteToLogs) {
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString() + "\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i = 0; i < arr.length; i++) {
            report += "    " + arr[i].toString() + "\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause

        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if (cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report += "    " + arr[i].toString() + "\n";
            }
        }
        report += "-------------------------------\n\n";

        String messageToUpload = e.getMessage() + "\n" + report;
        if (shouldWriteToLogs) {
            LoggingUtil.updateNetworkLog(LOG_TAG + "\n\n" + messageToUpload, true);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                    messageToUpload, DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.HIGH_PRIORITY);
        }

        return messageToUpload;

    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        ToolbarViewsDataManager.getInstance(applicationContext).unregister();
    }

    public static Context getAppContext() {
        return _context;
    }

    public static void setAppContext(Context context) {
        _context = context;
    }

    public static Context getContext() {
        return applicationContext;
    }


    public static String getDeviceInfo() {
        String deviceInformation = null;
        try {
            PackageInfo packageInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);


            deviceInformation = "Version Data - \n" +
                    "\n Version Name [ " + packageInfo.versionName + " ]" +
                    "\n Version Code [ " + packageInfo.versionCode + " ]" +
                    "\n Version Release [ " + Build.VERSION.RELEASE + " ]" +
                    "\n\nDevice Data - \n" +
                    "\n Device Version SDK Level [ " + Build.VERSION.SDK_INT + " ]" +
                    "\n Device Model [ " + Build.MODEL + " ]" +
                    "\n Device Manufacturer [ " + Build.MANUFACTURER + " ]" +
                    "\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceInformation;
    }

    public static String GetCommDebugString() {
        String str;

        str = "Battery=" + DeviceStateManager.getInstance().getNewBatteryLevel();
        str += ",Motion=" + LocationManager.getMotionState();
        str += ",BLE=" + (MainActivity.lastBleTagRxDebug / 1000);
        str += "," + (MainActivity.LongLastPureBeaconPacketRx / 1000);
        str += ",IsInPureComZone=" + LocationManager.isDeviceInPureComZoneState();
        return str;
    }

    public static String getInstalledVersionNumber() {
        PackageInfo packageInfo;
        String installedVersion = "";
        try {
            packageInfo = App.getContext().getPackageManager().getPackageInfo(App.getContext().getPackageName(), 0);
            installedVersion = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return installedVersion;
    }

    public static boolean isActivityAlreadyRunning(Context context, String className) {
        boolean isActivityRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (int i = 0; i < recentTasks.size(); i++) {
            if (className.equals(recentTasks.get(i).baseActivity.getClassName())) {
                isActivityRunning = true;
                break;
            }
        }
        return isActivityRunning;
    }

    public static boolean isActivityOnForegroundTop(String className) {
        final int MAX_OF_ENTRIES = 1;
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(MAX_OF_ENTRIES);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            return topActivity.getClassName().equals(className);
        }
        return false;
    }

    public void setActivityToForegroundIfNeeded() {

        final ActivityManager activityManager = (ActivityManager) App.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < recentTasks.size(); i++) {

            // bring to front
            if (recentTasks.get(i).baseActivity.toShortString().indexOf(App.getContext().getPackageName()) > -1) {
                activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
            }

            BaseActivity.isCameFromRegularActivityCode = true;
        }

    }

    public static void writeToNetworkLogsAndDebugInfo(String TAG, String messageToUpload, DebugInfoModuleId debugInfoModuleId) {
        Log.i(TAG, messageToUpload);
        LoggingUtil.updateNetworkLog("\n\n***  [" + TimeUtil.getCurrentTimeStr() + "]" + messageToUpload, false);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                debugInfoModuleId.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    public static void writeToNetworkLogsAndDebugInfo(String TAG, String logCatMsgToUpload, String csvFileMsgToUpload, DebugInfoModuleId debugInfoModuleId) {
        Log.i(TAG, logCatMsgToUpload);
        LoggingUtil.updateNetworkLog("\n\n***  [" + TimeUtil.getCurrentTimeStr() + "]" + csvFileMsgToUpload, false);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), csvFileMsgToUpload,
                debugInfoModuleId.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    public static void writeToBleLogsAndDebugInfo(String TAG, String logCatMsgToUpload, String csvFileMsgToUpload, DebugInfoModuleId debugInfoModuleId) {
        Log.i(TAG, logCatMsgToUpload);
        LoggingUtil.writeBleLogsToFile(csvFileMsgToUpload);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), csvFileMsgToUpload,
                debugInfoModuleId.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    public static void writeToZoneLogsAndDebugInfo(String TAG, String messageToUpload, DebugInfoModuleId debugInfoModuleId) {
        Log.i(TAG, messageToUpload);
        LoggingUtil.fileLogZonesUpdate("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "]" + messageToUpload);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                debugInfoModuleId.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    public void launchPureTracActivity(String extraName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(App.getContext().getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(extraName, true);
        startActivity(intent);
    }

    public void wakeUpApplicationIfNeeded() {
        PowerManager powerManager = (PowerManager) (App.getContext().getSystemService(Context.POWER_SERVICE));
        boolean isSceenAwake = powerManager.isScreenOn();
        WakeLock screenLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG");
        if (!isSceenAwake) {
            screenLock.acquire();
        }

        if (screenLock.isHeld()) {
            screenLock.release();
        }
    }
}
