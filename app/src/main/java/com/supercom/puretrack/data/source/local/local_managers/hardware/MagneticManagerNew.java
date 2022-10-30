package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableSelfDiagnosticEvents;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableSelfDiagnosticManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.enums.CaseTamperOperator;
import com.supercom.puretrack.model.database.entities.EntityCaseTamper;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.custom_implementations.OnOnlyFinishedCountdownTimer;
import com.supercom.puretrack.util.custom_implementations.OnOnlySensorChangedEventListener;
import com.supercom.puretrack.util.date.TimeSpan;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MagneticManagerNew extends OnOnlySensorChangedEventListener {

    public interface MagnetRecalibrationListener {
        void onRecalibration();
    }

    //Class Variables - primitives
    private float[] geomagneticValues;
    private long lastUpdate = 0;
    private boolean shouldRecalibrate;

    //Class Variables - Objects
    private OnOnlyFinishedCountdownTimer selfDiagnosticEventTimer;
    private static final MagneticManagerNew magneticManager = new MagneticManagerNew();
    private MagnetRecalibrationListener listener;


    boolean isCaseClose;
    int caseThreshold=0;
    int valueChangeThreshold=0;
    boolean isValueChange;
    CaseTamperOperator caseTamperOperator;
    Date openEventRequired;
    boolean hasOpenEvent;
    double value;

    private MagneticManagerNew() {
        LightSensorManager.getInstance();
        readParams();
    }

    public static MagneticManagerNew getInstance() {
        return magneticManager;
    }

    public void setShouldRecalibrate(boolean shouldRecalibrate) {
        this.shouldRecalibrate = shouldRecalibrate;
    }

    public void setMagnetRecalibrationListener(MagnetRecalibrationListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
                logToServer("TYPE_MAGNETIC_FIELD_UNCALIBRATED");
            } else if (sensorEvent.sensor.getType() != Sensor.TYPE_MAGNETIC_FIELD) {
                logToServer("TYPE_MAGNETIC_UNKNOW");
                return;
            }
            if (sensorEvent.values == null) {
                return;
            }

            geomagneticValues = sensorEvent.values.clone();
            if (shouldRecalibrate) {
                logToServer("recalibrateCaseTamper - SensorChanged");
                recalibrateCaseTamper(geomagneticValues);
                return;
            }
            //Log.i("MagneticManagerNew22", "geomagneticValues - " + Arrays.toString(geomagneticValues));
            handleMagneticSensorData();
            startMagneticDiagnosticTimer();

        }
    }

    public void recalibrateCaseTamper() {
        logToServer("recalibrateCaseTamper");
        try {
            recalibrateCaseTamper(geomagneticValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. if case open, do not calibrate
    // 2.

    private void recalibrateCaseTamper(float[] values) {
        EntityCaseTamper caseTamperEntity = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity();
        if (caseTamperEntity == null) return;
        float totalValue = Math.abs(values[0]) + Math.abs(values[1]) + Math.abs(values[2]);
        if (totalValue >= caseTamperEntity.caseClosedThreshold) {
            PureTrackSharedPreferences.setCaseTamperOperator(CaseTamperOperator.GREATER_EQUALS);
        } else {
            PureTrackSharedPreferences.setCaseTamperOperator(CaseTamperOperator.LESSER_THEN);
        }

        if (listener != null) {
            listener.onRecalibration();
        }
    }

    private void startMagneticDiagnosticTimer() {
        if (selfDiagnosticEventTimer != null) {
            selfDiagnosticEventTimer.cancel();
        }

        int magneticDiagnosticTime = TableSelfDiagnosticManager.sharedInstance().getIntValueByColumnName(TableSelfDiagnosticEvents.MAGNETIC_SENSITIVITY);
        if (magneticDiagnosticTime > 0) {

            long diagnosticTimerMillis = TimeUnit.HOURS.toMillis(magneticDiagnosticTime);
            selfDiagnosticEventTimer = new OnOnlyFinishedCountdownTimer(diagnosticTimerMillis, 1000) {

                @Override
                public void onFinish() {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceDiagnosticReport);
                }
            };
            selfDiagnosticEventTimer.start();
        }
    }

    private void readParams(){
        caseThreshold = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity().caseClosedThreshold;
        caseTamperOperator=PureTrackSharedPreferences.getCaseTamperOperator();
        valueChangeThreshold=caseThreshold/2;
        hasOpenEvent = TableEventsManager.sharedInstance().hasOpenEventInViolationCategory(TableEventConfig.ViolationCategoryTypes.DEVICE_CASE_TAMPER);
    }

    private void handleMagneticSensorData() {
        lastUpdate = System.currentTimeMillis();
        double uT = Math.sqrt(
                Math.pow(geomagneticValues[0], 2) +
                        Math.pow(geomagneticValues[1], 2) +
                        Math.pow(geomagneticValues[2], 2));

        if (Math.abs(uT - value) > valueChangeThreshold) {
            isValueChange = true;
            value = uT;
            NetworkRepository.turnOnScreenX();
            Log.i("CaseTamperTest", "uT: " + value);
        }

        if (caseTamperOperator == CaseTamperOperator.GREATER_EQUALS) {
            isCaseClose = value >= caseThreshold;
        } else {
            isCaseClose = value < caseThreshold;
        }

        handleEvents();
    }

    Date lastHandleEvents=new Date();
    private void handleEvents() {
        if(TimeSpan.getDiff(lastHandleEvents).totalSeconds < 2){
            return;
        }

        lastHandleEvents = new Date();
        readParams();

        if (isCaseClose) {
            if(hasOpenEvent){
                closeEvent();
            }

            isValueChange = false;
            return;
        }else {
            if (hasOpenEvent) {
                Log.i("CaseTamperTest", "hasOpenEvent");
                return;
            }

            if(!isValueChange){
                Log.i("CaseTamperTest", "Value is not change");
                return;
            }

            if (openEventRequired == null){
                if(LightSensorManager.getInstance().lastValue < LightSensorManager.SENSOR_SENSITIVITY) {
                    Log.i("CaseTamperTest", "set openEventRequired");
                    openEventRequired = new Date();
                }else{
                    isValueChange = false;
                    openEvent();
                }
            }else{
                if(LightSensorManager.getInstance().lastValue < LightSensorManager.SENSOR_SENSITIVITY) {
                    TimeSpan ts=TimeSpan.getDiff(openEventRequired);
                    if (ts.totalSeconds < 10) {
                        Log.i("CaseTamperTest", "wait "+ts.totalSeconds+" seconds");
                        return;
                    }

                    openEventRequired=null;
                    cancelEvent();
                }else{
                    openEventRequired=null;
                    isValueChange = false;
                    openEvent();
                }
            }
        }
    }

    private void closeEvent() {
        logToServer("Close Magnetic Event");
        String messageToUpload = "Device Case Closed : magnetX:" + geomagneticValues[0] + ", magnetY: " + geomagneticValues[1];
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceCaseTamperClosed);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload, DebugInfoModuleId.Events.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    private void cancelEvent() {
        SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Message, " Block Magnetics by Light Sensor"));
        long lastMoveBefore=(new Date().getTime() - LightSensorManager.getInstance().lastData.date.getTime())/1000 ;
        logToServer("recalibrateCaseTamper\nFix magnetic by light sensor" +
                " illuminance:"+LightSensorManager.getInstance().lastValue+
                "\n(last reported before " +lastMoveBefore + " seconds)");
        NetworkRepository.turnOnScreenX();
        recalibrateCaseTamper(geomagneticValues);
    }

    private void openEvent() {
        Log.i("CaseTamperTest", "Magnetic Proximity");
        logToServer("Open Magnetic Event. value:" + value + " caseTamperOperator:" + caseTamperOperator);
        SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.ErrorMessage, "Magnetic Proximity"));
        String messageToUpload = "Device Case Opened: magnetX=" + geomagneticValues[0] + ", magnetY= " + geomagneticValues[1];
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceCaseTamperOpen);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload, DebugInfoModuleId.Events.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }


    public void logToServer(String message) {
        Log.i("CaseTamperTest", message);
        //Toast.makeText(App.applicationContext, message, Toast.LENGTH_SHORT).show();
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                message, DebugInfoModuleId.Magnetic.ordinal(), DebugInfoPriority.HIGH_PRIORITY);
    }
}
