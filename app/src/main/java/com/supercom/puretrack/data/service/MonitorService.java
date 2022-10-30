package com.supercom.puretrack.data.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.List;

public class MonitorService extends Service {

    private Handler handler;
    Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String ProcessNameChrome = "com.android.chrome";
                            ActivityManager manager = (ActivityManager) MonitorService.this.getSystemService(Context.ACTIVITY_SERVICE);
                            List<ActivityManager.RunningAppProcessInfo> listOfProcesses = manager.getRunningAppProcesses();
                            for (ActivityManager.RunningAppProcessInfo process : listOfProcesses) {
                                if (process.processName.contains(ProcessNameChrome)) {
                                    android.os.Process.killProcess(process.pid);
                                    android.os.Process.sendSignal(process.pid, android.os.Process.SIGNAL_KILL);
                                    manager.killBackgroundProcesses(process.processName);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
                handler.postDelayed(this, 10);
            }
        };
        handler.postDelayed(runnable, 1000);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "SERVICE START COMMAND", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Toast.makeText(this, "SERVICE STARTED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "SERVICE DESTROY", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MonitorService.class);
        startService(intent);
    }


}
