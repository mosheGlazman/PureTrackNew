package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.shielding.CellularStrengthModel;
import com.supercom.puretrack.model.database.entities.EntityDeviceJamming;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.custom_implementations.OnOnlyFinishedCountdownTimer;

import java.util.concurrent.TimeUnit;

public class DeviceJammingManager {

    //Class Variables - Booleans
    private  boolean isSimCardAvailable;
    private boolean isCellularSampleTimerRunning = false;
    private boolean isJammingEventTimerRunning = false;

    //Class Variables - Objects
    private OnOnlyFinishedCountdownTimer cellularSampleIntervalTimer = null;
    private OnOnlyFinishedCountdownTimer jammingEventTimer = null;
    private final HardwareUtilsManager hardwareUtilsManager = new HardwareUtilsManager();
    private EntityDeviceJamming entityDeviceJamming;

    //Class Variables - Singleton Instance
    private static final DeviceJammingManager instance = new DeviceJammingManager();


    private void handleCellularSampling(final TelephonyManager telephonyManager) {
        initVariables();
        if (entityDeviceJamming.enabled == 0) {
            stopJamming();
            return;
        }
        CellularStrengthModel cellularStrengthModel = hardwareUtilsManager.calculateCellularStrength(telephonyManager);
        boolean is4gLteSignalAboveConfiguredLevel = cellularStrengthModel.getReceptionType() == HardwareUtilsManager.ReceptionType.LTE_4G &&
                cellularStrengthModel.getDbmStrength() > entityDeviceJamming.minimumGoodCellularLevelLte4G;
        boolean is3gWcdmaSignalAboveConfiguredLevel = cellularStrengthModel.getReceptionType() == HardwareUtilsManager.ReceptionType.WCDMA_3G &&
                cellularStrengthModel.getDbmStrength() > entityDeviceJamming.minimumGoodCellularLevelWcdma3G;
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                "Jamming - Network type - " + cellularStrengthModel.getReceptionType().name() + " Reception level - " + cellularStrengthModel.getDbmStrength(),
                DebugInfoModuleId.Jamming.ordinal(), TableDebugInfo.DebugInfoPriority.HIGH_PRIORITY);
        if (is4gLteSignalAboveConfiguredLevel || is3gWcdmaSignalAboveConfiguredLevel) {
            if (isJammingEventTimerRunning) return;
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                    "Jamming - Starting Jamming event timer",
                    DebugInfoModuleId.Jamming.ordinal(), TableDebugInfo.DebugInfoPriority.HIGH_PRIORITY);
            jammingEventTimer.start();
            isJammingEventTimerRunning = true;
        } else {
            jammingEventTimer.cancel();
            isJammingEventTimerRunning = false;
        }
    }

    private void initVariables() {
        entityDeviceJamming = DatabaseAccess.getInstance().tableDeviceJamming.getDeviceJammingConfigEntity();
        final TelephonyManager telephonyManager = (TelephonyManager) App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) return;
        isSimCardAvailable = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
        if (entityDeviceJamming == null) return;
        cellularSampleIntervalTimer = new OnOnlyFinishedCountdownTimer(entityDeviceJamming.cellularLevelSampleInterval, 1000) {
            @Override
            public void onFinish() {
                handleCellularSampling(telephonyManager);
                start();
            }
        };
        jammingEventTimer = new OnOnlyFinishedCountdownTimer(entityDeviceJamming.jammingEventTimerSensitivity, 1000) {
            @Override
            public void onFinish() {
                TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                        "Jamming - Opening Jamming event tamper",
                        DebugInfoModuleId.Jamming.ordinal(), TableDebugInfo.DebugInfoPriority.HIGH_PRIORITY);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceJammingTamper);
            }
        };
    }

    public static DeviceJammingManager getInstance() {
        return instance;
    }


    public void handleJamming(boolean isNetworkAvailable) {
        if (entityDeviceJamming == null || entityDeviceJamming.enabled == 0 || !isSimCardAvailable) return;
        if (!isNetworkAvailable) startJamming(); else stopJamming();

    }

    private void stopJamming() {
        cellularSampleIntervalTimer.cancel();
        isCellularSampleTimerRunning = false;
        boolean hasOpenEventInDeviceJammingCategory = TableEventsManager
                .sharedInstance()
                .hasOpenEventInViolationCategory(TableEventConfig.ViolationCategoryTypes.DEVICE_JAMMING);
        if (!hasOpenEventInDeviceJammingCategory) return;
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                "Jamming - Closing jamming event",
                DebugInfoModuleId.Jamming.ordinal(), TableDebugInfo.DebugInfoPriority.HIGH_PRIORITY);
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceJammingClosed);
    }

    private void startJamming() {
        if (isCellularSampleTimerRunning) return;
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                "Jamming - No communication with PureMonitor, starting cellular sampling",
                DebugInfoModuleId.Jamming.ordinal(), TableDebugInfo.DebugInfoPriority.HIGH_PRIORITY);
        cellularSampleIntervalTimer.start();
        isCellularSampleTimerRunning = true;
    }


}
