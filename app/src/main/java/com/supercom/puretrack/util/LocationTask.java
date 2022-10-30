package com.supercom.puretrack.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.supercom.puretrack.util.application.App;

public class LocationTask {
    android.location.LocationListener listener;
    android.location.LocationManager locationManager;
    Location location;
    Context context;

    int secondsTimeOut = 0;
    TaskCallBack callBack;

    int counter = 0;
    boolean running;
    boolean byGPS;
    boolean byNetwork;

    public void start(boolean byGPS, boolean byNetwork, int secondsTimeOut, TaskCallBack taskCallBack) {
        this.secondsTimeOut = secondsTimeOut;
        this.callBack = taskCallBack;
        this.byGPS = byGPS;
        this.byNetwork = byNetwork;
        this.context = App.getContext();

        addLog("start running");

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        running = true;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                requestLocationUpdates();
            }
        });

        runStopTread();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            running = false;
            return;
        }

        listener = createListener();
        locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (byGPS) {
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1, 1, listener);
        }
        if (byNetwork) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, listener);
        }
    }

    private static void addLog(String line) {
        Log.i("LocationTask", line);
    }

    private static void addErrorLog(String line) {
        Log.e("LocationTask", line);
    }

    private void runStopTread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (location == null && counter < secondsTimeOut && running) {
                    addLog("thread sleep 1000 ms");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter++;
                }

                if (running || location == null) {
                    addErrorLog("no location received");
                }

                if (running) {
                    stopRunning();
                }
            }
        }).start();
    }

    private LocationListener createListener() {
        return new android.location.LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                addLog("location received");
                LocationTask.this.location = location;
                if (callBack.onReceived(location)) {
                    locationManager.removeUpdates(listener);
                    stopRunning();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                addLog("onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                addLog("onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                addLog("onProviderDisabled");
            }
        };
    }

    public boolean isRunning() {
        return running;
    }

    public void stopRunning() {
        running = false;
        callBack.onStop(location!= null);
    }
}
