package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.enums.CaseTamperOperator;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MagneticManager2 implements SensorEventListener {
    public static MagneticManager2 magneticManager;
    public static MagneticManager2 getInstance() {
        if(magneticManager==null){
            magneticManager=new MagneticManager2();
        }
        return magneticManager;
    }
    SensorManager mSensorManager;
    Sensor mMagnetic;
    MagneticManager2(){
    //   mSensorManager = (SensorManager) App.applicationContext.getSystemService(Context.SENSOR_SERVICE);
    //   mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    //   mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    int fixedValue,thresholdPercent,lastValue,lastPercent,lastEventPercent,lastEventValue;
    Date lastValueTime,lastEventTime;
    boolean lastEventCaseOpen,shouldSaveMagneticValue;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            try {
                if (lastValueTime != null && (new Date().getTime() - lastValueTime.getTime()) < 1400) {
                    return;
                }

                if (sensorEvent.values == null) {
                    return;
                }
                log("onSensorChanged(" + sensorEvent.sensor.getType() + ")");

                lastValue = getValue(sensorEvent.values);
                lastValueTime = new Date();

                readParams();
                isCaseOpen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMagneticValue(){
        shouldSaveMagneticValue=true;
    }

    private void readParams() {
        thresholdPercent = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity().caseClosedThreshold;
        while (thresholdPercent >=100){
            thresholdPercent /=10;
        }

        if(shouldSaveMagneticValue){
            shouldSaveMagneticValue=false;
            fixedValue = lastValue;
            PureTrackSharedPreferences.setLastMagneticsValue(fixedValue);
        }
    }

    private int getValue(float[] values) {
       return   (int)(Math.sqrt(
                Math.pow(values[0], 2) +
                        Math.pow(values[1], 2) +
                        Math.pow(values[2], 2)));
    }

    private boolean isCaseOpen() {
        int tp = (fixedValue / 100 ) * thresholdPercent;
        int diff= Math.abs(lastValue - fixedValue);
        lastPercent = diff / (fixedValue / 100);
        return diff < tp;
    }

    private void handleMagneticSensorData() {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void logToServer(String message) {

    }

    public void log(String message) {

    }
}
