package com.supercom.puretrack.data.service.heart_beat;

import static com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager.TAG_MAC_ADDRESS;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.OffenderPreferencesManager;
import com.supercom.puretrack.data.source.local.table_managers.TableScannerTypeManager;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class HeartBeatServiceJava2 extends Service implements HeartBeatTaskJava.HeartBeatTaskListener {
    public static String TAG = "HeartBeatTask";
    public final static int NOTIFICATION_STATUS_ID = 12348;
    public static final String CHANNEL_ID = "HeartBeatServiceJava";
    public static boolean running;
    long runTaskDate;
    public static HeartBeatTaskJava activeTask;
    public static String pureTagMacAddress="";
    @Nullable
    public static final String ACTION="com.supercom.testtagvibrate.ACTION";
    public static final String ACTION_STOP="com.supercom.testtagvibrate.ACTION_STOP";
    public static final String ACTION_ScanDevice="ACTION_ScanDevice";
    OffenderPreferencesManager.TagVibrateOnDisconnectParams params;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        running = true;

        startForeground(12399, ServiceUtils.getNotificationChannel(getApplicationContext()));

        sendLogLine("Service" , "onCreate",null);
        LoggingUtil.updateHeartBeatLog("Service onCreate");

        //ServiceUtils.setTimeout(5);
        runThread();
    }



    @SuppressLint("MissingPermission")
    private void runThread() {


        new Thread(new Runnable() {
            @Override
            public void run() {
                int interval = 20000;
                boolean editInterval = false;

                while (running) {
                    if (!checkParams()){
                        sendLogLine("Service", lastError, null);
                        sleep(30000);
                        continue;
                    }

                    long passTime = new Date().getTime() - runTaskDate;

                    if (passTime > interval) {
                        editInterval = false;
                        runTask();
                    } else {
                        if (!editInterval && activeTask.status == HeartBeatTaskJava.e_status.finish) {
                            if (activeTask.result == HeartBeatTaskJava.e_result.gattDisconnect_257) {
                                interval = params.hbinterval * 2000;
                            } else if (activeTask.result == HeartBeatTaskJava.e_result.gattDisconnect_133) {
                                interval = 7000;
                            } else {
                                interval = params.hbinterval*1000;
                            }

                            editInterval = true;
                        }
                    }

                    sleep(2000);
                }
            }
        }).start();
    }

    private boolean checkParams() {
        params =  OffenderPreferencesManager.getInstance().getTagVibrateOnDisconnectParams();

        if(params.enabled !=1) {
            pureTagMacAddress = "";
            lastError = "service is disabled";
            lastSuccessConnect = null;
            lastFailedCounter = 0;
            stopSelf();
            return false;
        }

        pureTagMacAddress = TableScannerTypeManager.sharedInstance().getStringValueByColumnName(TAG_MAC_ADDRESS);
        if(pureTagMacAddress.equals("AA:AA:AA:AA:AA:AA")){
            pureTagMacAddress="";
        }

        if (pureTagMacAddress == null || pureTagMacAddress.length() == 0) {
            lastError = "no mac address";
            lastSuccessConnect=null;
            lastFailedCounter=0;
            stopSelf();
            return false;
        }

        lastError = "";
        return true;
    }

    public static Date lastSuccessConnect;
    public static int lastFailedCounter;
    public static String lastError;

    private void runTask() {
        if (activeTask != null) {
            if (activeTask.status != HeartBeatTaskJava.e_status.finish) {
                activeTask.finish(HeartBeatTaskJava.e_result.cancel);
            }

            if(activeTask.result== HeartBeatTaskJava.e_result.success){
                lastSuccessConnect=new Date();
                lastFailedCounter=0;
                lastError="";
            }else {
                lastError=activeTask.result+"";
                lastFailedCounter++;

                if(lastFailedCounter>=6){
                    LoggingUtil.updateHeartBeatLog("------------ ERROR------------\n");
                    LoggingUtil.updateHeartBeatLog("lastFailedCounter:" + lastFailedCounter);
                    LoggingUtil.updateHeartBeatLog("------------ ERROR------------\n");
                }
            }
        }

        Log.i(TAG+"F","Service2 "+"run task");
        runTaskDate = new Date().getTime();
        activeTask = new HeartBeatTaskJava();
        activeTask.start(getApplicationContext(),pureTagMacAddress,params.timeouttovibrate, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        LoggingUtil.updateHeartBeatLog("Service onDestroy");

        if (activeTask != null) {
            if (activeTask.status != HeartBeatTaskJava.e_status.finish) {
                activeTask.finish(HeartBeatTaskJava.e_result.cancel);
            }
        }

        sendLogLine("Service" , "onDestroy",null);
    }


    @Override
    public void onStatusChange(HeartBeatTaskJava task) {
        if (task.status == HeartBeatTaskJava.e_status.finish) {
            Log.i(TAG+"F","Service2 "+"finish task: " + task.result);

            if (activeTask.result == HeartBeatTaskJava.e_result.searchDeviceTimeOut) {
                BluetoothManager.device=null;
                ServiceUtils.turnOnScreen();
                sendBroadcast( new Intent(ACTION_ScanDevice));
            }

            sendBroadcast(new Intent(ACTION_STOP));
        }
    }

    private void sendLogLine(String tag, String text, Boolean actionStatus) {
        if(!BuildConfig.DEBUG){
            return;
        }
        Log.i(TAG,tag+" " + text);

        LogLine line=new LogLine();
        line.setActionStatus(actionStatus);
        line.setTag(tag);
        line.setText(text);

        log(line);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void start(){
        if(running){
            return;
        }

        Context context=App.applicationContext;

        running=true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, HeartBeatServiceJava2.class));
        } else {
            context.startService(new Intent(context, HeartBeatServiceJava2.class));
        }
    }

    public static void stop(Context context){
        if(!running){
            return;
        }

        running=false;
        context.stopService(new Intent(context, HeartBeatServiceJava2.class));
    }

    @Override
    public void log(LogLine line) {
        Intent intent=new Intent(ACTION);
        intent.putExtra("line",line);
        sendBroadcast(intent);
    }

}