package com.supercom.puretrack.util.custom_implementations;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;

public abstract class OnOnlySensorChangedEventListener implements SensorEventListener {


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
