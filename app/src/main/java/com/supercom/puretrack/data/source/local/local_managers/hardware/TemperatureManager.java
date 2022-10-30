package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;

public class TemperatureManager implements SensorEventListener {

    @Override
    public void onSensorChanged(SensorEvent event) {
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_TEMPERATURE, Math.round(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}