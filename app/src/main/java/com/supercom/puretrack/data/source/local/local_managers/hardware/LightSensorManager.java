package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.custom_implementations.OnOnlySensorChangedEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LightSensorManager extends OnOnlySensorChangedEventListener {
    private static LightSensorManager instance;
    private ArrayList<Integer> listValues;

    public static LightSensorManager getInstance() {
        if (instance == null) {
            instance = new LightSensorManager();
        }

        return instance;
    }

    SensorManager mSensorManager;
    Sensor mLight;
    Context context;
    SensorData lastData = new SensorData(SensorData.E_SensorType.Light);
    public int lastValue = -1;
    Date startUp = new Date();
    int listValuesLength = 1;
    public static int SENSOR_SENSITIVITY = 30;
    ScreenReceiver mReceiver;
    boolean isScreenOn;

    private LightSensorManager() {
        listValues = new ArrayList<>();
        for (int i = 0; i < listValuesLength; i++) listValues.add(0);

        if(true){
            return;
        }

        register();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int value = getValue();
                Log.i("LSM", "value:" + value);
            }
        },10000,10000);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
         mReceiver =new ScreenReceiver();
        context.registerReceiver(mReceiver, filter);
    }

    public void register() {
        context = App.getContext();

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    int onSensorChangedCounter=0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            int v = (int) event.values[0];
            listValues.add(0,v);
            listValues.remove(listValuesLength);
            onSensorChangedCounter++;
            if (onSensorChangedCounter % listValuesLength != 0) {
                return;
            }

            int value = getValue();

            Log.i("LightSensorManager", "value:" + value);

            if (lastValue == -1 || Math.abs(value - lastValue) > SENSOR_SENSITIVITY) {
                lastValue = value;
                lastData = new SensorData(SensorData.E_SensorType.Light, event.values[0]);
                if (lastData.date.getTime() - startUp.getTime() < 3000) {
                    //MagneticManager.getInstance().logToServer("illuminance value change at startup");
                    //lastData=null;
                    return;
                }

                SensorDataSource.getInstance().add(lastData);
            }
        }
    }

    private int getValue() {
        int res = 0;
        for (int i = 0; i < listValuesLength; i++) {
            res += listValues.get(i);
        }

        return res / listValuesLength;
    }

     class ScreenReceiver extends BroadcastReceiver {

         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction() == Intent.ACTION_SCREEN_OFF) {
                 SensorData data = new SensorData(SensorData.E_SensorType.Screen, 1);
                 SensorDataSource.getInstance().add(data);
                 Log.i("onSensorChanged ", "ACTION_SCREEN_OFF");
                 isScreenOn = false;
             } else if (intent.getAction() == Intent.ACTION_SCREEN_ON) {
                 SensorData data = new SensorData(SensorData.E_SensorType.Screen, 2);
                 SensorDataSource.getInstance().add(data);
                 Log.i("onSensorChanged ", "ACTION_SCREEN_ON");
                 isScreenOn = true;
             }
         }
     }
}
