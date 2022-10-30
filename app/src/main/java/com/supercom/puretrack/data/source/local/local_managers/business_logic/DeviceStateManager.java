package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.local_managers.hardware.OffenderPreferencesManager;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.CommunicationInterval;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.objects.BatteryThreshold;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.general.LoggingUtil;

import java.util.Date;

public class DeviceStateManager {
    public static final String TAG = "DeviceState";
    private int prevWasChargingState = ChargingState.UNKNOWN;
    public int curChargingState = ChargingState.UNKNOWN;
    public DeviceStateListener deviceStateListener;

    private BroadcastReceiver batteryChangeReceiver;
    private int newBatteryLevel;

    public interface ChargingState {
        int UNKNOWN = -1;
        int ON_CHARGING = 1;
        int NOT_CHARGING = 0;
    }

    public interface DEVICE_BATTERY_CONS {
        int DEVICE_BATTERY_HIGH_STATUS = 0;
        int DEVICE_BATTERY_MEDIUM_STATUS = 1;
        int DEVICE_BATTERY_LOW_STATUS = 2;
        int DEVICE_BATTERY_CRITICAL_STATUS = 3;

    }

    private static final DeviceStateManager DEVICE_STATE_INST = new DeviceStateManager();

    private DeviceStateManager() {
    }

    public static DeviceStateManager getInstance() {
        return DEVICE_STATE_INST;
    }

    public void setDeviceStateListener(DeviceStateListener deviceStateListener) {
        this.deviceStateListener = deviceStateListener;
    }

    public interface DeviceStateListener {
        void onBatteryPercentageChanged(int percentage);

        void onBatteryStatusChanged(int newStatus);

        void onBatteryChargingStatusChanged(boolean isCharging);
    }

    public int getPrevWasChargingState() {
        return prevWasChargingState;
    }

    public int getCurChargingState() {
        return curChargingState;
    }

    public void registerBatteryAndUsbChanges() {

        newBatteryLevel = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_PERCENTAGE);
        int curPluggedStatus = App.getContext().registerReceiver
                (null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)).getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        curChargingState = (curPluggedStatus == 0 ? ChargingState.NOT_CHARGING : ChargingState.ON_CHARGING);

        batteryChangeReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive");
                int curPluggedStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                Log.i(TAG, "is charging: " + curChargingState);

                prevWasChargingState = curChargingState;
                curChargingState = (curPluggedStatus == 0 ? ChargingState.NOT_CHARGING : ChargingState.ON_CHARGING);

                //if (prevWasChargingState != curChargingState) {
                    deviceStateListener.onBatteryChargingStatusChanged(curChargingState == ChargingState.ON_CHARGING);
                //}

                handleACPlugged(curPluggedStatus);
                handleUSBPlugged(curPluggedStatus);

                CommunicationInterval currentCommInterval = NetworkRepository.getInstance().getCurrentCommInterval();

                if (currentCommInterval == CommunicationInterval.CommIntervalLow && curChargingState == ChargingState.ON_CHARGING &&
                        prevWasChargingState != curChargingState) {
                    NetworkRepository.getInstance().scheduleNewCycleIfNeeded(false);
                } else if (currentCommInterval == CommunicationInterval.CommInterval && curChargingState == ChargingState.NOT_CHARGING &&
                        prevWasChargingState != curChargingState) {
                    NetworkRepository.getInstance().scheduleNewCycleIfNeeded(false);
                }

                int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                Log.i("BatteryR", "rawLevel:" + rawLevel + " scale:" + scale + " curChargingState:" + curChargingState);

                if (rawLevel >= 0 && scale > 0) {
                    TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_PERCENTAGE, newBatteryLevel);
                    newBatteryLevel = (rawLevel * 100) / scale;

                    handleDeviceBatteryConsumption(newBatteryLevel);
                    deviceStateListener.onBatteryPercentageChanged(newBatteryLevel);
                }
            }
        };

        App.getContext().registerReceiver(batteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void unregisterBatteryAndUsbChanges() {
        try {
            if (batteryChangeReceiver != null) {
                App.getContext().unregisterReceiver(batteryChangeReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleACPlugged(int plugged) {
        boolean hasACOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                (ViolationCategoryTypes.VIOLATION_CHARGING_AC) != -1;
        boolean hasUSBOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                (ViolationCategoryTypes.VIOLATION_CHARGING_USB) != -1;
        if ((plugged == BatteryManager.BATTERY_PLUGGED_AC) && !isUsbConnected()) {
            if (!hasACOpenEvent) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.OnACCharger, -1, -1);
            }
        } else {
            if (hasACOpenEvent && !hasUSBOpenEvent) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.OffACCharger, -1, -1);

                //delete device battery full event from open table if needed
                boolean hasOpenDeviceBatteryFullEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                        (ViolationCategoryTypes.DEVICE_BATTERY_FULL) != -1;
                if (hasOpenDeviceBatteryFullEvent) {
                    DatabaseAccess.getInstance().tableOpenEventsLog.deleteAll_WithViolationCategory
                            (ViolationCategoryTypes.DEVICE_BATTERY_FULL, -1);
                }
            }
        }
    }

    private void handleUSBPlugged(int plugged) {
        boolean hasUSBOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                (ViolationCategoryTypes.VIOLATION_CHARGING_USB) != -1;
        if ((plugged == BatteryManager.BATTERY_PLUGGED_USB) || isUsbConnected()) {
            if (!hasUSBOpenEvent) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.OnUSBCharger, -1, -1);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.OnACCharger, -1, -1);
            }
        } else {
            if (hasUSBOpenEvent) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.OffUSBCharger, -1, -1);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.OffACCharger, -1, -1);

                //delete device battery full event from open table
                boolean hasOpenDeviceBatteryFullEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                        (ViolationCategoryTypes.DEVICE_BATTERY_FULL) != -1;
                if (hasOpenDeviceBatteryFullEvent) {
                    DatabaseAccess.getInstance().tableOpenEventsLog.deleteAll_WithViolationCategory
                            (ViolationCategoryTypes.DEVICE_BATTERY_FULL, -1);
                }
            }
        }
    }

    private boolean isUsbConnected() {
        Intent intent = App.getAppContext().registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        return intent.getExtras().getBoolean("connected");
    }

    public void checkForSuddenShutDown() {
        if(true){
            return;
        }

        long systemParam=android.os.SystemClock.elapsedRealtime();
        long lastParam=TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI);
        Log.i("bug651","systemParam:"+systemParam+" lastParam:"+lastParam +" diff: " + (systemParam-lastParam));

        if (lastParam > systemParam) {
            Log.i("bug651","add event");
            TableEventsManager.sharedInstance().addPowerOnAfterSuddenlyShutDownEventToLogIfNeed();
        }

        TableOffenderDetailsManager.sharedInstance().updateColumnLong(OFFENDER_DETAILS_CONS.DETAILS_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI,
                systemParam);
    }

    public int getIsMobileDataAvaliable() {
        if (Settings.Global.getInt(App.getContext().getContentResolver(), "mobile_data", 1) == 1) {
            return 1;
        } else {
            return 0;
        }
    }

    public int getNewBatteryLevel() {
        return newBatteryLevel;
    }

    private void handleDeviceBatteryConsumption(int newBatteryLevel) {

        int batteryStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT);
        int lastBatteryLevel = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_PERCENTAGE);
        Log.i("BatteryLog","handle newBatteryLevel:"+newBatteryLevel + " batteryStatus:"+batteryStatus + " lastBatteryLevel:"+lastBatteryLevel) ;

        // consumption
        BatteryThreshold batteryThresholdConfiguration = TableOffenderDetailsManager.sharedInstance().getBatteryThresholdConfiguration();
        if (newBatteryLevel < lastBatteryLevel) {
            Log.i("BatteryLog","newBatteryLevel < lastBatteryLevel") ;
            if (newBatteryLevel <= batteryThresholdConfiguration.No_Charger_Medium && newBatteryLevel > batteryThresholdConfiguration.No_Charger_Low
                    && batteryStatus != DEVICE_BATTERY_CONS.DEVICE_BATTERY_MEDIUM_STATUS) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT,
                        DEVICE_BATTERY_CONS.DEVICE_BATTERY_MEDIUM_STATUS);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventDeviceBatteryMedium, -1, -1);
                Log.i("BatteryLog","eventDeviceBatteryMedium") ;
                //
            } else if (newBatteryLevel <= batteryThresholdConfiguration.No_Charger_Low && newBatteryLevel > batteryThresholdConfiguration.No_Charger_Critical
                    && batteryStatus != DEVICE_BATTERY_CONS.DEVICE_BATTERY_LOW_STATUS) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT,
                        DEVICE_BATTERY_CONS.DEVICE_BATTERY_LOW_STATUS);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventDeviceBatteryLow, -1, -1);
                Log.i("BatteryLog","eventDeviceBatteryLow") ;

                CommunicationInterval currentCommInterval = NetworkRepository.getInstance().getCurrentCommInterval();
                if (currentCommInterval == CommunicationInterval.CommInterval) {
                    NetworkRepository.getInstance().scheduleNewCycleIfNeeded(false);
                }
            } else if (newBatteryLevel <= batteryThresholdConfiguration.No_Charger_Critical && newBatteryLevel > 0 &&
                    batteryStatus != DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT,
                        DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventDeviceBatteryCritical, -1, -1);
                Log.i("BatteryLog","eventDeviceBatteryCritical") ;
                deviceStateListener.onBatteryStatusChanged(DEVICE_BATTERY_CONS.DEVICE_BATTERY_CRITICAL_STATUS);
            }
        }

        // charge
        else if (lastBatteryLevel < newBatteryLevel) {
            if (newBatteryLevel <= 100 && newBatteryLevel >= batteryThresholdConfiguration.Charger_High &&
                    batteryStatus != DEVICE_BATTERY_CONS.DEVICE_BATTERY_HIGH_STATUS) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT,
                        DEVICE_BATTERY_CONS.DEVICE_BATTERY_HIGH_STATUS);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventDeviceBatteryHigh, -1, -1);
                Log.i("BatteryLog","eventDeviceBatteryHigh") ;
            } else if (newBatteryLevel < batteryThresholdConfiguration.Charger_High && newBatteryLevel >= batteryThresholdConfiguration.Charger_Medium
                    && batteryStatus != DEVICE_BATTERY_CONS.DEVICE_BATTERY_MEDIUM_STATUS) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT,
                        DEVICE_BATTERY_CONS.DEVICE_BATTERY_MEDIUM_STATUS);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventDeviceBatteryMedium, -1, -1);
                Log.i("BatteryLog","eventDeviceBatteryMedium") ;

                CommunicationInterval currentCommInterval = NetworkRepository.getInstance().getCurrentCommInterval();
                if (currentCommInterval == CommunicationInterval.CommIntervalLow) {
                    NetworkRepository.getInstance().scheduleNewCycleIfNeeded(false);
                }
            } else if (newBatteryLevel <= batteryThresholdConfiguration.Charger_Medium && newBatteryLevel > batteryThresholdConfiguration.Charger_Low
                    && batteryStatus != DEVICE_BATTERY_CONS.DEVICE_BATTERY_LOW_STATUS) {
                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_STAT_DEVICE_BATTERY_STAT,
                        DEVICE_BATTERY_CONS.DEVICE_BATTERY_LOW_STATUS);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventDeviceBatteryLow, -1, -1);
                Log.i("BatteryLog","eventDeviceBatteryLow") ;
            }

            addDeviceBatteryFullEventIfNeeded(newBatteryLevel, lastBatteryLevel);

        }
    }

    private void addDeviceBatteryFullEventIfNeeded(int newBatteryLevel, int lastBatteryLevel) {
        boolean hasACOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                (ViolationCategoryTypes.VIOLATION_CHARGING_AC) != -1;
        boolean hasUSBOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                (ViolationCategoryTypes.VIOLATION_CHARGING_USB) != -1;
        boolean hasOpenDeviceBatteryFullEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory
                (ViolationCategoryTypes.DEVICE_BATTERY_FULL) != -1;

        boolean hasOpenEventOfCharger = hasACOpenEvent || hasUSBOpenEvent;
        if (!hasOpenDeviceBatteryFullEvent && hasOpenEventOfCharger && newBatteryLevel == 100 && lastBatteryLevel < 100) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceBatteryFull, -1, -1);
            Log.i("BatteryLog","deviceBatteryFull") ;
        }
    }

    /**
     * validate if sim card is inserted and working, and if not update DB
     */
    public void validateIfSimCardIsReady() {
        TelephonyManager telephonyManager = (TelephonyManager) App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if ((telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY)
                && DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_SIM_CARD) == -1) {
            Log.i(TAG, "onCreate - Sim card removed");
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onCreate - Sim card removed", false);
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.SimCardRemoved, -1, -1);
        } else if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY
                && DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_SIM_CARD) != -1) {
            Log.i(TAG, "onCreate - Sim card inserted");
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] onCreate - Sim card inserted", false);
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.SimCardInserted, -1, -1);
        }
        validateSimCardReplaced(telephonyManager);
    }

    private void validateSimCardReplaced(TelephonyManager telephonyManager) {
        try {
            String lastCimCCID = TableOffenderStatusManager.sharedInstance().getStringValueByColumnName(OFFENDER_STATUS_CONS.OFF_SIM_ICCID);
            String currentCimCCID = telephonyManager.getSimSerialNumber();
            if (lastCimCCID == null || lastCimCCID.isEmpty()) {
                TableOffenderStatusManager.sharedInstance().updateColumnString(OFFENDER_STATUS_CONS.OFF_SIM_ICCID, currentCimCCID);
            } else if (currentCimCCID != null && !lastCimCCID.equals(currentCimCCID)) {
                TableOffenderStatusManager.sharedInstance().updateColumnString(OFFENDER_STATUS_CONS.OFF_SIM_ICCID, currentCimCCID);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.simCardReplaced, -1, -1, currentCimCCID);
            }
        }catch (Exception ex){

        }
    }

    public void enableMobileDataIfOff() {
        boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();
        boolean isMobileDataEnabled = DeviceStateManager.getInstance().getIsMobileDataAvaliable() == 1;
        if (isKnoxLicenceActivated && !isMobileDataEnabled) {

            App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), "Turned on mobile data after application started", DebugInfoModuleId.Network);

            KnoxUtil.getInstance().getKnoxSDKImplementation().setMobileDataMode(true);
        }
    }
}
