package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.supercom.puretrack.data.BuildConfig;
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
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.custom_implementations.OnOnlyFinishedCountdownTimer;
import com.supercom.puretrack.util.custom_implementations.OnOnlySensorChangedEventListener;
import com.supercom.puretrack.util.hardware.VoiceManager;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MagneticManager extends OnOnlySensorChangedEventListener {

    public interface MagnetRecalibrationListener {
        void onRecalibration();
    }

    int magneticValue;
    int magneticDiffFactor;

    //Class Variables - primitives
    private float[] geomagneticValues;
    private long lastUpdate = 0;
    private long lastEventUpdate = 0;
    private boolean shouldRecalibrate;
    private boolean shouldSaveMagneticValue;
    int totalValue, lastTotalValue, diffPercent,lastDiffPercent;
    boolean isCaseOn;
    int caseClosedThreshold;

    //Class Variables - Objects
    private OnOnlyFinishedCountdownTimer selfDiagnosticEventTimer;
    private static final MagneticManager magneticManager = new MagneticManager();
    private MagnetRecalibrationListener listener;

    private MagneticManager() {
        magneticValue = PureTrackSharedPreferences.getMagneticsValue();
        if(magneticValue==0){
            magneticValue=1;
        }
    }

    public void saveMagneticValue(){
        shouldSaveMagneticValue=true;
    }

    public static MagneticManager getInstance() {
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
        Log.i("MMCT","onSensorChanged");
        synchronized (this) {

            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    startMagneticDiagnosticTimer();

                case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                    if (sensorEvent.values == null) return;
                    geomagneticValues = sensorEvent.values.clone();
                //    if (shouldRecalibrate) {
                //        recalibrateCaseTamper(geomagneticValues);
                //        return;
                //    }

                    handleMagneticSensorData();
                    startMagneticDiagnosticTimer();
                    break;

                default:
                    break;
            }
        }
    }

    private void recalibrateCaseTamper(float[] values) {
        EntityCaseTamper caseTamperEntity = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity();
        if (caseTamperEntity == null) return;
        float totalValue = Math.abs(values[0]) + Math.abs(values[1]) + Math.abs(values[2]);
        if (totalValue >= caseTamperEntity.caseClosedThreshold) {
            PureTrackSharedPreferences.setCaseTamperOperator(CaseTamperOperator.GREATER_EQUALS);
        } else {
            PureTrackSharedPreferences.setCaseTamperOperator(CaseTamperOperator.LESSER_THEN);
        }
        listener.onRecalibration();
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

    boolean lastIsCaseOn;
    boolean hasOpenEventInDeviceCaseTamperCategory;

    private void handleMagneticSensorData() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdate) < 3000) return;
        lastUpdate = currentTime;
        isCaseOn = isCaseOn();

        hasOpenEventInDeviceCaseTamperCategory = TableEventsManager.sharedInstance().hasOpenEventInViolationCategory(TableEventConfig.ViolationCategoryTypes.DEVICE_CASE_TAMPER);

        // Option one - case is on and no previous event was created in the same category; we do nothing.
        if (isCaseOn && !hasOpenEventInDeviceCaseTamperCategory) return;

        // Option two - case is not on and no previous event was created in the same category; we open a 'Device Case Tamper' event.
        if (!isCaseOn && !hasOpenEventInDeviceCaseTamperCategory) {
            String messageToUpload = "Device Case Opened: magnetX=" + geomagneticValues[0] + ", magnetY= " + geomagneticValues[1];
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceCaseTamperOpen);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload, DebugInfoModuleId.Events.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
            SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Magnetics, totalValue));
            SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Magnetics, "Open"));
            SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Magnetics,  magneticValue+"-->"+totalValue+" ="+diffPercent+"%"));

            if(BuildConfig.DEBUG) {
             //   Toast.makeText(App.applicationContext, "Open", Toast.LENGTH_LONG).show();
             //   Toast.makeText(App.applicationContext, "Open", Toast.LENGTH_LONG).show();
             //   Toast.makeText(App.applicationContext, "Open", Toast.LENGTH_LONG).show();

               // VoiceManager.getInstance(App.applicationContext).playWav(VoiceManager.e_files.Error);
            }

            return;
        }
        // Option Three - case is on and previously event was created; we open a 'Device Case Closed' event.
        if (isCaseOn) {
            String messageToUpload = "Device Case Closed : magnetX:" + geomagneticValues[0] + ", magnetY: " + geomagneticValues[1];
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceCaseTamperClosed);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload, DebugInfoModuleId.Events.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
            SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Magnetics, totalValue));
            SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Magnetics, "Closed"));
            SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Magnetics,  magneticValue+"-->"+totalValue+" ="+diffPercent+"%"));

            if(BuildConfig.DEBUG) {
                // Toast.makeText(App.applicationContext, "Closed", Toast.LENGTH_LONG).show();
                // Toast.makeText(App.applicationContext, "Closed", Toast.LENGTH_LONG).show();
                // Toast.makeText(App.applicationContext, "Closed", Toast.LENGTH_LONG).show();

                //VoiceManager.getInstance(App.applicationContext).playWav(VoiceManager.e_files.Success);
            }
        }
    }

    private boolean isCaseOn() {
        Log.i("MMCT","isCaseOn");
        if(magneticValue==0){
            magneticValue=1;
        }

         caseClosedThreshold = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity().caseClosedThreshold;
         totalValue =  (int)(Math.sqrt(
                Math.pow(geomagneticValues[0], 2) +
                        Math.pow(geomagneticValues[1], 2) +
                        Math.pow(geomagneticValues[2], 2)));

        if(shouldSaveMagneticValue){
            shouldSaveMagneticValue=false;
            magneticValue = totalValue;
            PureTrackSharedPreferences.setLastMagneticsValue(magneticValue);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "Set fix value to "+magneticValue, DebugInfoModuleId.Events.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
            Log.i("MMCT","set fix value:"+totalValue);
        }

        if(magneticValue<0){
            return true;
        }

        int percent = caseClosedThreshold;
        while (percent >=100){
            percent /=10;
        }

     //  int max= Math.max(totalValue,magneticValue);
     //  int min= Math.min(totalValue,magneticValue);
     //  int diff = max - min;
     //  int div= (max / 100);
     //  if (div==0)div=1;
     //  diffPercent = diff / div;

        int thresholdPercent = (magneticValue / 100 ) * percent;
        int diff= Math.abs(totalValue - magneticValue);
        boolean res = diff < thresholdPercent;

        try {
            diffPercent = (int)((float)diff / ((float)magneticValue / 100f));
        } catch (Exception e) {
            e.printStackTrace();
        }


        if(res != lastIsCaseOn){
            Log.i("MMCT","--------------------------------------------------");
            Log.i("MMCT","magneticValue:"+magneticValue);
            Log.i("MMCT","value:"+totalValue);
            Log.i("MMCT","percent:"+percent);
            Log.i("MMCT","diff:"+diff);
            Log.i("MMCT","diffPercent:"+diffPercent);
            Log.i("MMCT","isCaseOn:"+res);

            lastIsCaseOn=res;
            lastDiffPercent=diffPercent;
            lastTotalValue = totalValue;
            lastEventUpdate= new Date().getTime();

            SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Magnetics,  magneticValue+"-->"+totalValue+" ="+diffPercent+"%"));
        } else if(BuildConfig.DEBUG){
            Log.i("MMCT","value:"+totalValue);
        }

        return res;
    }


    public void logToServer(String message) {
        //Toast.makeText(App.applicationContext, message, Toast.LENGTH_SHORT).show();
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                message, DebugInfoModuleId.Magnetic.ordinal(), DebugInfoPriority.HIGH_PRIORITY);
    }

    public String toHtmlLog(){
        SimpleDateFormat format_HH_mm_ss =new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

        StringBuilder res=new StringBuilder();
        res.append(SensorDataSource.toHtmlYellow(SensorDataSource.toHtmlUnderline(SensorDataSource.toHtmlBold("Magnetic Case:"))));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlYellow("Value: "));
        res.append(SensorDataSource.toHtmlWhite(magneticValue+""));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlYellow("Threshold(Percent): "));
        res.append(SensorDataSource.toHtmlWhite(caseClosedThreshold+""));
        res.append(SensorDataSource.toHtmlWhite("%"));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.htmlEnter());

        res.append(SensorDataSource.toHtmlGreen(SensorDataSource.toHtmlUnderline(SensorDataSource.toHtmlBold("Last value"))));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlGreen("Time: "));
        res.append(SensorDataSource.toHtmlWhite(format_HH_mm_ss.format(new Date(lastUpdate))));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlGreen("Value: "));
        res.append(SensorDataSource.toHtmlWhite(totalValue+""));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlGreen("Diff: "));
        res.append(SensorDataSource.toHtmlWhite(diffPercent+""));
        res.append(SensorDataSource.toHtmlWhite("%"));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.htmlEnter());

        res.append(SensorDataSource.toHtmlPink(SensorDataSource.toHtmlUnderline(SensorDataSource.toHtmlBold("Last event change"))));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlPink("Time: "));
        res.append(SensorDataSource.toHtmlWhite(format_HH_mm_ss.format(new Date(lastEventUpdate))));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlPink("Value: "));
        res.append(SensorDataSource.toHtmlWhite(lastTotalValue +""));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlPink("State: "));
        res.append(SensorDataSource.toHtmlWhite(hasOpenEventInDeviceCaseTamperCategory ? "Open" : "Close"));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.toHtmlPink("Diff: "));
        res.append(SensorDataSource.toHtmlWhite(lastDiffPercent+""));
        res.append(SensorDataSource.toHtmlWhite("%"));
        res.append(SensorDataSource.htmlEnter());
        res.append(SensorDataSource.htmlEnter());

        return res.toString();
    }
}

