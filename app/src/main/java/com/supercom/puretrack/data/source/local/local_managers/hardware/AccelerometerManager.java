package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.Toast;

import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.objects.AccelerometerConfig;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.general.LoggingUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AccelerometerManager implements SensorEventListener {

    public interface IUpdateActivitySensor {
        // gps points request
        void onAccelerometerStateChanged(boolean isMotionMode, boolean stateChanged);
    }

    private IUpdateActivitySensor updateActivityListener;
    private double mAccelCurrent = 0;
    private double mAccelLast = 0;
    private int windowDurationCounter = 0;
    private int windowMotionCounter = 0;
    private int windowStaticCounter = 0;
    private int debugCounter = 0;
    private double LastDelta = 0;
    private boolean MotionState = true;
    private String latestAccelerometerValues;
    private int isDeviceLayingFlatAsInt = 0;

    private int motionWin = 200;
    private int motionPerc = 50;
    private double motionThr = 2.2;
    private int staticWin = 2400;
    private int staticPerc = 85;
    private double staticThr = 1.6;
    private int mode = 1; // 0=disabled, 1=enabled, 2=enabled+debug

    // home device motion mechanism
    private boolean homeDeviceMotionStatus = false;
    private int homeDeviceMotionWindowDuration = 25;    // seconds
    private int homeDeviceMotionPercentage = 30; // 0-100
    private int homeDeviceMotionSampleDuration = 5;
    private int homeDeviceMotionValue = 0; // motion at the last 'homeDeviceMotionWindow' seconds, 0-100
    private int homeDeviceMotionCounter = 0;
    private int homeDeviceMotionSampleTotalCounts = 0;
    private long homeDeviceMotionSampleStartTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    private List<Integer> sampleAverageList = new ArrayList<>();
    private String motionDebugMessage;

    public int accelerationThreshold=105;
    public int accelerationMillisProximity=20000;

    private long lastMovementTime=new Date().getTime();

    public void updateAccelerometerConfig(AccelerometerConfig settings) {
        mode = settings.Enabled;
        if (mode == 0 && updateActivityListener != null) {
            // force "normal mode" - in motion
            updateActivityListener.onAccelerometerStateChanged(true, true);
        }
        motionWin = settings.MotionWinSamples;
        motionPerc = settings.MotionWinPercentage;
        motionThr = settings.MotionThreshold;
        staticWin = settings.StaticWinSamples;
        staticPerc = settings.StaticWinPercentage;
        staticThr = settings.staticThreshold;

        accelerationMillisProximity = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity().accelerationMillisProximity;
        accelerationThreshold = DatabaseAccess.getInstance().tableCaseTamper.getCaseTamperEntity().accelerationThreshold;

        windowDurationCounter = 0;
        windowMotionCounter = 0;
        windowStaticCounter = 0;
        MotionState = true;
        // home device settings
        if (settings.motion_sample_time == 0) {
            homeDeviceMotionSampleDuration = 5;
        } else {
            homeDeviceMotionSampleDuration = settings.motion_sample_time;
        }
        homeDeviceMotionWindowDuration = settings.motion_window_time;
        homeDeviceMotionPercentage = settings.motion_window_level;
    }

    public String getLatestAccelerometerValues() {
        return latestAccelerometerValues;
    }

    public int getIsDeviceLayingFlatAsInt() {
        return isDeviceLayingFlatAsInt;
    }

    private static final AccelerometerManager INSTANCE = new AccelerometerManager();

    public static synchronized AccelerometerManager getInstance() {
        return INSTANCE;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setActivityListener(IUpdateActivitySensor updateActivityListener) {
        this.updateActivityListener = updateActivityListener;
    }

    public void onSensorChanged(SensorEvent event) {

        if (mode == 0) {
            // disabled
            return;
        }
        debugCounter++;

        float x = event.values[0] * 10;
        float y = event.values[1] * 10;
        float z = event.values[2] * 10;

        if (x > 20 || x < -20 || y > 20 || y < -20) {
            isDeviceLayingFlatAsInt = 0;
        } else {
            isDeviceLayingFlatAsInt = 1;
        }
        latestAccelerometerValues = "{X:" + x + ",Y:" + y + ",Z:" + z + "}";

        mAccelLast = mAccelCurrent;

        mAccelCurrent = Math.sqrt(x * x + y * y + z * z);
        if(mAccelCurrent > accelerationThreshold){
            if (SensorDataSource.getInstance().add(new SensorData(SensorData.E_SensorType.Accelerometer,mAccelCurrent))){
                Log.i("CaseTamperTest", "Accelerometer Move "+mAccelCurrent);
            }
            lastMovementTime = new Date().getTime();
        }

        double currDelta = mAccelCurrent - mAccelLast;

        homeDeviceMotionSampleTotalCounts++;
        if ((Math.abs(currDelta) > motionThr)) {
            // motion detected, add to motion samples counter
            homeDeviceMotionCounter++;
        }
        // calculate motion sample percentage
        if ((TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - homeDeviceMotionSampleStartTime) > homeDeviceMotionSampleDuration) {
            // sample window ended
            int samplePercentage = (int) ((float) homeDeviceMotionCounter / (float) homeDeviceMotionSampleTotalCounts * 100);
            sampleAverageList.add(samplePercentage);
            // handle sample running average

            int samplesInWindow = homeDeviceMotionWindowDuration / homeDeviceMotionSampleDuration;
            if (sampleAverageList.size() >= samplesInWindow) {
                // reached full window, calculatre sample average
                //SamplePercentage = SamplePercentageSum / SampleCount;
                homeDeviceMotionValue = 0;
                for (int i = 0; i < sampleAverageList.size(); i++) {
                    homeDeviceMotionValue += sampleAverageList.get(i);
                }
                int totalVal = homeDeviceMotionValue;
                // total window running average
                homeDeviceMotionValue /= sampleAverageList.size();
                // set current motion status
                homeDeviceMotionStatus = homeDeviceMotionValue >= homeDeviceMotionPercentage;
                if (mode == 3) {
                    motionDebugMessage = "motion: " + homeDeviceMotionStatus + " total: " + totalVal + " samp: " + sampleAverageList.size() + " samp_cnt: " +
                            homeDeviceMotionSampleTotalCounts + " mot_cnt: " + homeDeviceMotionCounter;
                    //Toast.makeText(MainApplication.getPureTrackApplicationContext(), "motion: " + homeDeviceMotionStatus + " total "+totalVal + " samp: "+sampleAverageList.size(), Toast.LENGTH_SHORT).show();
                }
                if (motionDebugMessage != null)
                    TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                            "motion for wifi: " + motionDebugMessage, DebugInfoModuleId.Zones.ordinal(), TableDebugInfo.DebugInfoPriority.NORMAL_PRIORITY);

                // for running average, remove oldest item
                sampleAverageList.remove(0);
            }
            // clear sample counters and time
            homeDeviceMotionSampleTotalCounts = 0;
            homeDeviceMotionCounter = 0;
            homeDeviceMotionSampleStartTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        }


        // handle motion for General device state (location interval etc.)
        if (!MotionState) {
            // not in motion, look for motion
            if ((Math.abs(currDelta) > motionThr)/* && (Math.abs(LastDelta) > motionThr)*/) {
                // motion detected, add to motion samples counter
                windowMotionCounter++;
            }
            // check if window finished
            if (windowDurationCounter++ >= motionWin) {
                int Percentage = (int) ((float) windowMotionCounter / (float) motionWin * 100);
                if (mode == 2) {
                    String msg, msg2 = "";
                    if (Percentage > staticPerc) {
                        msg2 = ", Changing to motion";
                    }
                    msg = "\n" + TimeUtil.getCurrentTimeStr() + " ACCEL, Static state, " + windowMotionCounter + " / " + motionWin + ", percentage: " + Percentage + msg2 + "\n";
                    LoggingUtil.fileLogZonesUpdate(msg);
                    Toast.makeText(App.getContext(), "Motion Mode: Static, " + windowMotionCounter + "/" + motionWin + msg2, Toast.LENGTH_SHORT).show();
                }
                // check motion percentage in the window
                if (Percentage > motionPerc) {
                    // motion detected
                    MotionState = true;
                    // update new state
                    updateActivityListener.onAccelerometerStateChanged(true, true);
                } else {
                    updateActivityListener.onAccelerometerStateChanged(false, false);
                }
                // reset window counters
                windowMotionCounter = 0;
                windowDurationCounter = 0;
                windowStaticCounter = 0;
            }
        } else { // motion state
            // motion, look for static
            if ((Math.abs(currDelta) < staticThr)/* && (Math.abs(LastDelta) < staticThr)*/) {
                // static detected, add to static samples counter
                windowStaticCounter++;
            }
            // check if window finished
            if (windowDurationCounter++ >= staticWin) {
                int Percentage = (int) ((float) windowStaticCounter / (float) staticWin * 100);
                if (mode == 2) {
                    String msg, msg2 = "";
                    if (Percentage > staticPerc) {
                        msg2 = ", Changing to static";
                    }
                    msg = "\n" + TimeUtil.getCurrentTimeStr() + " ACCEL, Motion state, " + windowStaticCounter + " / " + staticWin + ", percentage: " + Percentage + msg2 + "\n";
                    LoggingUtil.fileLogZonesUpdate(msg);
                    Toast.makeText(App.getContext(), "Motion Mode: Motion, " + windowStaticCounter + "/" + staticWin + msg2, Toast.LENGTH_SHORT).show();
                }
                // check motion percentage in the window
                if (Percentage > staticPerc) {
                    // static state detected
                    MotionState = false;
                    // update new state
                    updateActivityListener.onAccelerometerStateChanged(false, true);
                } else {
                    updateActivityListener.onAccelerometerStateChanged(true, false);
                }
                // reset window counters
                windowMotionCounter = 0;
                windowDurationCounter = 0;
                windowStaticCounter = 0;
            }
        }
        LastDelta = currDelta;
    }

    public long getLastMovementTime() {
        return lastMovementTime;
    }

    public void handleDebugRequestButton() {
        if (mode == 2) {
            if (MotionState) {
                Toast.makeText(App.getContext(), "Current motion mode: Motion " + debugCounter, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(App.getContext(), "Current motion mode: Static " + debugCounter, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
