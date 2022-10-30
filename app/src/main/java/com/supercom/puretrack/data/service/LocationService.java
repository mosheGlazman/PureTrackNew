package com.supercom.puretrack.data.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.supercom.puretrack.data.source.local.local_managers.hardware.LocationManager;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.model.database.entities.EntityDebugInfo;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.broadcast_receiver.RestartBackgroundReceiver;
import com.supercom.puretrack.ui.activity.MainActivity;


public class LocationService extends Service implements LocationListener {

    private android.location.LocationManager mLocationManager = null;
    public static LocationService sInstance = null;

    private NotificationManager mNotific = null;

    CharSequence name = "Ragav";
    String desc = "this is notific";
    int importance = NotificationManager.IMPORTANCE_HIGH;
    final String ChannelID = "my_channel_01";
    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();

        if (mLocationManager == null) {
            mLocationManager = (android.location.LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        sInstance = this;
        App.writeToZoneLogsAndDebugInfo("ForLoc", "Foreground Service - onCreate", DebugInfoModuleId.Zones);

        createNotificationChannelXXX();

        Intent intent = new Intent(LocationManager.LOCATION_HANDLER_ACTION_EXTRA);
        intent.putExtra(LocationManager.LOCATION_SERVICE_STARTED_STATUS_EXTRA, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    public static LocationService getInstance() {
        return sInstance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        App.writeToZoneLogsAndDebugInfo("ForLoc", "Foreground Service - onDestroy", DebugInfoModuleId.Zones);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("RestartService");
        broadcastIntent.setClass(this, RestartBackgroundReceiver.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onLocationChanged(Location location) {

        MainActivity.playLocationRxTone = true;
        LocationManager.currentActivityRecognizeStatusX = LocationManager.getMotionType(location.getSpeed());

        App.writeToZoneLogsAndDebugInfo("ForLoc", "Foreground Service - New Loc - " + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy(),
                DebugInfoModuleId.Zones);

        LocationManager.locationsArray.add(location);
    }

    @SuppressLint("MissingPermission")
    public void requestGpsLocationUpdates(int interval) {
        if (mLocationManager == null) {
            mLocationManager = (android.location.LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        } else {
            mLocationManager.removeUpdates(LocationService.this);
        }
        App.writeToZoneLogsAndDebugInfo("ForLoc", "Foreground Service - requestGpsLocationUpdates " + interval, DebugInfoModuleId.Zones);
        mLocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, interval * 1000, 0, LocationService.this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannelXXX() {
        App.writeToZoneLogsAndDebugInfo("ForLoc", "Foreground Service - createNotificationChannel", DebugInfoModuleId.Zones);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotific = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(ChannelID, name,
                    importance);
            mChannel.setDescription(desc);
            mChannel.setLightColor(Color.CYAN);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            mNotific.createNotificationChannel(mChannel);
        }

        String Body = "PureTrack Is Running";

        Notification n = new Notification.Builder(this, ChannelID)
                .setContentTitle("PureTrack")
                .setContentText(Body)
                .setBadgeIconType(R.drawable.officer_mode_icon)
                .setNumber(5)
                .setSmallIcon(R.drawable.officer_mode_icon)
                .setAutoCancel(true)
                .build();

        startForeground(2, n);
    }
}

