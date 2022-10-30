package com.supercom.puretrack.data.source.local.local_managers.hardware.shielding;


import static com.supercom.puretrack.data.source.local.local_managers.hardware.shielding.ShieldingEventManager.closeEvent;
import static com.supercom.puretrack.data.source.local.local_managers.hardware.shielding.ShieldingEventManager.openEvent;
import static com.supercom.puretrack.data.source.local.local_managers.hardware.shielding.ShieldingInitializationManager.getDeviceShieldingEntity;
import static com.supercom.puretrack.data.source.local.local_managers.hardware.shielding.ShieldingInitializationManager.initConfigParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.HardwareUtilsManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.PreferencesBase;
import com.supercom.puretrack.data.source.remote.requests_listeners.TestConnectionListener;
import com.supercom.puretrack.model.business_logic_models.shielding.CellularStrengthModel;
import com.supercom.puretrack.model.database.entities.EntityDeviceShielding;
import com.supercom.puretrack.ui.views.ToolbarViewsDataManager;
import com.supercom.puretrack.util.application.App;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceShieldingManager extends PreferencesBase {
    public static String TAG = "ShieldingTest";

    //Class Variables - booleans
    public boolean isEnabled = true; // TODO: Should be initialized immediately from the db
    public boolean isBleOpenEventEnabled = false;
    public boolean isCellNetworkOpenEventEnabled = false;
    public boolean isWifiNetworkOpenEventEnabled = false;
    public boolean isBleClosingEventEnabled = false;
    public boolean isCellNetworkClosingEventEnabled = false;
    public boolean isWifiNetworkClosingEventEnabled = false;
    public boolean isCheckingForOpenEvent = false;

    public long checkIntervalMilSec = 30 * 1000L;
    public long bleThresholdMilSec = 30 * 1000L;
    public long wifiThresholdMilSec = 30 * 1000L;
    public long mobileNetworkThresholdMilSec = 30 * 1000L;
    private boolean testConnectionRunning = false;

    //Class Variables - long
    public Long lastSuccessfulBleMillis = Calendar.getInstance().getTimeInMillis();

    //Class Variables - int
    public int threshold = 10;
    public int remainingThreshold = -1;

    //Class Variables - Objects
    private Timer shieldingTimer;

    //Class Variables - instance
    private static DeviceShieldingManager instance;

    private DeviceShieldingManager() {
        super(App.getContext(), "DeviceShieldingManager");

    }

    public static DeviceShieldingManager getInstance() {
        if (instance == null) {
            instance = new DeviceShieldingManager();
        }
        return instance;
    }

    private void startShieldingProcess() {
        Log.i(TAG, "startShieldingProcess");
        if (hasOpenEvent()) {
            checkForClosingEvent();
        } else {
            checkForOpenEvent();
        }
    }

    private boolean hasOpenEvent() {
        return get("openEvent", "").equals("true");
    }

    private void setOpenEvent(boolean value) {
        put("openEvent", value + "");
    }

    public void checkForClosingEvent() {
        Log.i(TAG, "checkForClosingEvent");
        if (requiredToClosingEvent()) {
            showToastOnDebug("close Shielding Event", 7);
            setOpenEvent(false);
            closeEvent();
        }
    }


    public void checkForOpenEvent() {
        Log.i(TAG, "checkForOpenEvent");
        if (requiredToOpenEvent()) {
            showToastOnDebug("open Shielding Event", 7);
            setOpenEvent(true);
            openEvent();
        }
    }

    public boolean requiredToOpenEvent() {
        Log.i(TAG, "check to open event");

        if (isCellNetworkOpenEventEnabled) {
            if (!hasNetworkViolation()) {
                Log.i(TAG, "has no NetworkViolation");
                return false;
            } else {
                Log.e(TAG, "has NetworkViolation");
            }
        }

        if (isBleOpenEventEnabled) {
            if (!hasBleViolation()) {
                Log.i(TAG, "has no BleViolation");
                return false;
            } else {
                Log.e(TAG, "has BleViolation");
            }
        }

        if (isWifiNetworkOpenEventEnabled) {
            if (!hasWIFIViolation()) {
                Log.i(TAG, "has no WIFIViolation");
                return false;
            } else {
                Log.e(TAG, "has WIFIViolation");
            }
        }

        Log.e(TAG, "has Violations");
        return isBleOpenEventEnabled || isWifiNetworkOpenEventEnabled || isCellNetworkOpenEventEnabled;
    }

    public boolean requiredToClosingEvent() {
        Log.e(TAG, "check to close event");

        if (isCellNetworkClosingEventEnabled) {
            if (!hasNetworkViolation()) {
                Log.i(TAG, "has no NetworkViolation");
                return true;
            } else {
                Log.e(TAG, "has NetworkViolation");
            }
        }

        if (isBleClosingEventEnabled) {
            if (!hasBleViolation()) {
                Log.i(TAG, "has no BleViolation");
                return true;
            } else {
                Log.e(TAG, "has BleViolation");
            }
        }

        if (isWifiNetworkClosingEventEnabled) {
            if (!hasWIFIViolation()) {
                Log.i(TAG, "has no WIFIViolation");
                return true;
            } else {
                Log.e(TAG, "has WIFIViolation");
            }
        }

        Log.e(TAG, "has Violations");
        return false;
    }

    public void enableShielding() {
        shieldingTimer = new Timer();
        TimerTask shieldingTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    EntityDeviceShielding entityDeviceShielding = getDeviceShieldingEntity();
                    if (entityDeviceShielding == null) return;

                    initConfigParams(entityDeviceShielding);

                    startShieldingProcess();
                }
            }

        };
        shieldingTimer.scheduleAtFixedRate(shieldingTask, 0, checkIntervalMilSec);
    }

    public void disableShielding() {
        try {
            shieldingTimer.cancel();
            shieldingTimer.purge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Date lastCellularSignalLevelOk = new Date();

    public boolean hasNetworkViolation() {
        Log.i("ShieldingNetwork", "hasNetworkViolation");
        if (!isEnabled) {
            Log.i("ShieldingNetwork", "not isEnabled");
            return false;
        }

        //
        // Check TelephonyManager
        //
        TelephonyManager telephonyManager = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        HardwareUtilsManager hardwareUtilsManager = new HardwareUtilsManager();
        if (telephonyManager != null) {
            CellularStrengthModel cellularStrengthModel = hardwareUtilsManager.calculateCellularStrength(telephonyManager);
            if (cellularStrengthModel != null) {
                if (cellularStrengthModel.getSignalStrength() > 0) {
                    Log.i("ShieldingNetwork", "set by TelephonyManager");
                    testConnectionRunning = false;
                    lastCellularSignalLevelOk = new Date();
                    return false;
                }
            }
        }

        Log.e(TAG + "Network", "has no Violations");
        //
        // Check Network
        //
        ConnectivityManager connectivityManager = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isOn = mobileNetworkInfo.isAvailable() && mobileNetworkInfo.isConnectedOrConnecting();
        if (isOn) {
            Log.i("ShieldingNetwork", "set by ConnectivityManager");
            testConnectionRunning = false;
            lastCellularSignalLevelOk = new Date();
            return false;
        }

        //
        // Check Network by Service
        //
        ToolbarViewsDataManager toolbarManager = ToolbarViewsDataManager.getInstance(App.getContext());
        if (toolbarManager.getLastCellularSignalLevel() > 0) {
            if(toolbarManager.getLastCellularSignalLevelOk().getTime() > lastCellularSignalLevelOk.getTime()) {
                Log.i("ShieldingNetwork", "set by toolbarManager");
                testConnectionRunning = false;
                lastCellularSignalLevelOk = new Date();
                return false;
            }
        }

        if (!testConnectionRunning) {
            Log.i("ShieldingNetwork", "run test connection task");
            testConnectionRunning = true;
            NetworkRepository.getInstance().testConnection(new TestConnectionListener() {
                @Override
                public void onResponse(boolean success) {
                    testConnectionRunning = false;
                    if (success) {
                        Log.i("ShieldingNetwork", "test connection success");
                        Log.i("ShieldingNetwork", "set by testConnection");
                        lastCellularSignalLevelOk = new Date();
                    } else {
                        Log.i("ShieldingNetwork", "test connection failed");
                    }
                }
            });
        }

        long l = new Date().getTime() - lastCellularSignalLevelOk.getTime();
        boolean res = (l >= mobileNetworkThresholdMilSec * 1000);
        Log.i("ShieldingNetwork", "return " + res + "  (pass " + l + " millis)");
        return res;
    }

    public boolean hasBleViolation() {
        if (!isEnabled) {
            return false;
        }

        if (BluetoothManager.lastDeviceFound == null) {
            return false;
        }

        long l = new Date().getTime() - BluetoothManager.lastDeviceFound.getTime();
        if (l < bleThresholdMilSec) {
            return false;
        }

        return true;
    }

    public boolean hasWIFIViolation() {
        if (!isEnabled) {
            return false;
        }

        return false;
    }


    private void showToastOnDebug(final String message, final int count) {
        if (BuildConfig.DEBUG) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < count; i++) {
                        Toast.makeText(App.getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
