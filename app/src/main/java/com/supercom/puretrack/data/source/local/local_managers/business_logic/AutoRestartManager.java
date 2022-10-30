package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.source.local.SQLLiteAutoRestart;
import com.supercom.puretrack.data.source.local.local_managers.hardware.PreferencesBase;
import com.supercom.puretrack.data.source.local.table.TableAutoRestart;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.OffenderActivation;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AutoRestartManager extends PreferencesBase {
    /*

    class: AutoRestartManager
She handles all the logic and is responsible for rebooting the device according to parameters. You can define what is the period that should pass since the last normal communication, how many attempts to make, and when to try again

Params:
Name: auto_restart,
Value: "enabled":1,"no_comm_to_restart":7000,"next_retries":80000,"count_retries":6,"reset_count_retries":604800;

A more detailed explanation:

If the cycle fails, the following logic will be executed
If the offender is unallocated, nothing will happen

If X seconds have passed (param: no_comm_to_restart) the device will be a reboot.
After that, every X seconds (param: next_retries) a reboot will be performed.
If X reboots were performed in sequence (param: count_retries), the reboot attempts will be stopped until X seconds pass (param: reset_count_retries) and then the reboot attempts will return again

     */
    private static AutoRestartManager instance;

    public static AutoRestartManager getInstance() {
        if (instance == null) {
            instance = new AutoRestartManager();
        }
        return instance;
    }

    private AutoRestartManager() {
        super(App.getContext(), "AutoRestartManager2");
        sqlLiteAutoRestart=new SQLLiteAutoRestart();

        loadAutoRestartParams();
        loadAutoRestartData();
    }

    private AutoRestartParams autoRestartParams;
    private AutoRestartData autoRestartData;
    private  SQLLiteAutoRestart sqlLiteAutoRestart;

    public class AutoRestartData {
        public long lastCycleSuccessTime = new Date().getTime();
        public long lastRebootTime = 0;
        public int rebootCounter = 0;
        public int failedCounter = 0;
        public boolean isAfterReboot = false;
    }

    public class AutoRestartParams {
        public int enabled = 0;
        public long no_comm_to_restart = 7200;
        public long next_retries = 86400;
        public int count_retries = 5;
        public int reset_count_retries = 604800;
    }

    public void setAutoRestartParams(String json) {
        autoRestartParams = new Gson().fromJson(json, AutoRestartParams.class);
        put("Params", json);
    }

    public void loadAutoRestartParams() {
        autoRestartParams = new AutoRestartParams();
        String j = getAutoRestartParams();

        if (j.length() > 0) {
            try {
                autoRestartParams = new Gson().fromJson(j, AutoRestartParams.class);
                if (autoRestartParams == null) {
                    autoRestartParams = new AutoRestartParams();
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAutoRestartParams() {
        return get("Params", "");
    }

    public AutoRestartData getAutoRestartData() {
        String j = sqlLiteAutoRestart.getData();
        if (j==null || j.length() == 0) {
            return null;
        }
        Log.i("DataAA","load:"+j);

        try {
            return new Gson().fromJson(j, AutoRestartData.class);
        } catch (JsonSyntaxException e) {  }

        return null;
    }

    public void saveAsyncAutoRestartData() {
        String s = new Gson().toJson(autoRestartData);
        Log.i("DataAA","put:"+s);
        putAsync("DataAA", s);
        //tableAutoRestart.insertRecord(new EntityAutoRestart(s));
        sqlLiteAutoRestart.saveJson(s);
    }

    public void saveAutoRestartData() {
        String s = new Gson().toJson(autoRestartData);
        Log.i("DataAA","put:"+s);
        put("DataAA", s);
        //tableAutoRestart.insertRecord(new EntityAutoRestart(s));
        sqlLiteAutoRestart.saveJson(s);
    }

    public void loadAutoRestartData() {
        autoRestartData = getAutoRestartData();
        if (autoRestartData == null) {
            Log_i("AutoRestartData is null");
            autoRestartData = new AutoRestartData();
        }

        Log_i("Load AutoRestartData. Counter = " + autoRestartData.rebootCounter);

        if (autoRestartData.isAfterReboot) {
            Log_i("Save autoRestartData.isAfterReboot = false,  Counter = " + autoRestartData.rebootCounter);
            autoRestartData.isAfterReboot = false;
            saveAutoRestartData();
            addEvent(TableEventConfig.EventTypes.eventPowerOn);
        }
    }

    public void cycleSuccess() {
        if (!isEnabled()) {
            return;
        }
        Log_i("cycleSuccess");
        autoRestartData = new AutoRestartData();
        autoRestartData.lastCycleSuccessTime = new Date().getTime();
        saveAsyncAutoRestartData();
        loadAutoRestartData();
    }

    public void cycleFailed(int resultCode) {
        if (!isEnabled()) {
            return;
        }

        Log_e("cycleFailed " + resultCode);
        autoRestartData.failedCounter++;
        saveAsyncAutoRestartData();

        rebootIfRequired();
    }

    private boolean isEnabled() {
        return autoRestartParams.enabled == 1;
    }

    private boolean isOffenderAllocated() {
        try {
            if (TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS) == OffenderActivation.OFFENDER_STATUS_ALLOCATED) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    int isAfterRebootCounter = 0;

    private void rebootIfRequired() {
        if (autoRestartData.isAfterReboot) {
            if (isAfterRebootCounter == 4) {
                autoRestartData.isAfterReboot = false;
                Log_i("Save autoRestartData.isAfterReboot = false, isAfterRebootCounter=0, Counter = " + autoRestartData.rebootCounter);
                saveAutoRestartData();
                isAfterRebootCounter = 0;
            } else {
                isAfterRebootCounter++;
                Log_i("cycle failed but isAfterReboot = true. set counter to " + isAfterRebootCounter);
            }
            return;
        }

        if (!isOffenderAllocated()) {
            Log_i("cycle failed but offender is unallocated");
            return;
        }

        if (autoRestartData.rebootCounter == 0) {
            long lastTime = autoRestartData.lastCycleSuccessTime;
            long paramToPassTime = autoRestartParams.no_comm_to_restart;
            long passMillis = new Date().getTime() - lastTime;
            passMillis /= 1000;
            boolean isPassTime = passMillis > paramToPassTime;
            if (!isPassTime) {
                Log_i("cycle failed. no pass time from last cycle success (" + passMillis + ") no_comm_to_restart:" + autoRestartParams.no_comm_to_restart);
            } else {
                Log_e("autoRestartData.rebootCounter == 0");
                reboot();
            }

            return;
        }

        long lastTime = autoRestartData.lastRebootTime;
        long passMillis = new Date().getTime() - lastTime;
        passMillis /= 1000;
        boolean isPassTime = passMillis > autoRestartParams.next_retries;

        if (!isPassTime) {
            Log_i("cycle failed. after " + autoRestartData.rebootCounter + " retries. no pass time from last reboot (" + passMillis + ") next_retries:" + autoRestartParams.next_retries);
            return;
        }

        if (autoRestartData.rebootCounter <= autoRestartParams.count_retries) {
            Log_i("cycle failed. after " + autoRestartData.rebootCounter + " retries.   time from last reboot (" + passMillis + ") next_retries:" + autoRestartParams.next_retries);
            Log_e("autoRestartData.rebootCounter <= autoRestartParams.count_retries " + autoRestartData.rebootCounter + "<=" + autoRestartParams.count_retries);
            reboot();
            return;
        }

        if (passMillis <= autoRestartParams.reset_count_retries) {
            Log_e("cycle failed. after " + autoRestartData.rebootCounter + " reboot counter. we wait, pass " + passMillis + " sec reset_count_retries:" + autoRestartParams.reset_count_retries);
            return;
        }

        Log_e("cycle failed. after " + autoRestartData.rebootCounter + " reboot counter and pass " + passMillis + " sec reset_count_retries:" + autoRestartParams.reset_count_retries);
        autoRestartData.rebootCounter = 0;

        reboot();
    }

    private void reboot() {
        try {
            if (autoRestartData.isAfterReboot) {
                return;
            }

            autoRestartData.rebootCounter++;
            autoRestartData.lastRebootTime = new Date().getTime();
            autoRestartData.isAfterReboot = true;
            final int nextRebootCounter = autoRestartData.rebootCounter;

            Log_i("Save in reboot " + autoRestartData.rebootCounter);
            saveAutoRestartData();

            addEvent(TableEventConfig.EventTypes.eventConnectionUnavailableDeviceRestart);
            addEvent(TableEventConfig.EventTypes.eventPowerOff);

            showToast("Preparing to reboot");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AutoRestartData  au = getAutoRestartData();
                    while (au.rebootCounter != nextRebootCounter) {
                        Log_i("TreadSleep au.isAfterReboot = false");
                        TreadSleep(1000);
                    }

                    Log_i("isAfterReboot saved!!! Counter = " + au.rebootCounter);

                    TreadSleep(8000);

                    try {
                        Intent intent = new Intent();
                        if (BuildConfig.DEBUG) {
                            intent.setClassName("com.supercom.knox.appmanagement.dev", "com.supercom.knox.appmanagement.RebootActivity");
                        } else {
                            intent.setClassName("com.supercom.knox.appmanagement", "com.supercom.knox.appmanagement.RebootActivity");
                        }
                        App.getContext().startActivity(intent);
                    } catch (Exception ex) {

                    }
                    TreadSleep(1000);

                    App.applicationContext.sendBroadcast(new Intent("com.supercom.reboot"));
                    TreadSleep(10000);

                    int counter = 1;
                    while (counter < 11) {
                        Log_i("Preparing to reboot " + counter);
                        counter++;
                        TreadSleep(1000);
                    }

                    showToast("failed to reboot");
                    Log_e("failed to reboot");
                }
            }).start();
        } catch (Exception e) {
            Log_e(e.getMessage());
        }
    }

    private void addEvent(int event) {
        TableEventsManager.sharedInstance().addEventToLog(event, -1, -1, System.currentTimeMillis() +
                TimeUnit.MINUTES.toMillis(1), "");
    }



    private void Log_i(final String log) {
        Log.i("PTRCK-182", log);
    }

    private void Log_e(final String log) {
        Log.e("PTRCK-182", log);
    }

    private void TreadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.applicationContext, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
