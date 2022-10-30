package com.supercom.puretrack.data.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.CURRENT_COMM_NETWORK_FAILURE_RESET_STATE;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.repositories.NetworkRepository;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isInInitiatedFlightMode = KnoxUtil.getInstance().isInInitializedOffenderFlightMode();
        if (!isInInitiatedFlightMode) {
            String messageToUpload = "";
            boolean isMobileDataWasEnabledFromLocalDB = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                    (OFFENDER_STATUS_CONS.OFF_IS_MOBILE_DATA_ENABLED) == 1;
            boolean isMobileDataEnabled = DeviceStateManager.getInstance().getIsMobileDataAvaliable() == 1;
            boolean isInAirplaneMode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

            if (isMobileDataEnabled && !isInAirplaneMode && !isMobileDataWasEnabledFromLocalDB) {
                boolean isNetworkAvailable = NetworkRepository.getInstance().isNetworkAvailable();

                messageToUpload = "NetworkChangeReceiver -> Mobile data: " + isMobileDataEnabled + " , Airplande mode: " + isInAirplaneMode + " isMobileDataWasEnabledFromLocalDB " +
                        isMobileDataWasEnabledFromLocalDB + " isNetworkAvailable " + isNetworkAvailable;
                App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Receivers);

                if (isNetworkAvailable) {
                    KnoxUtil.getInstance().handleDeviceWasInInitiatedFlightModeIfNeeded();

                    TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_IS_MOBILE_DATA_ENABLED, 1);

                    int currentCommNetworkTestStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                            (OFFENDER_STATUS_CONS.OFF_CURRENT_COMM_NETWORK_TEST_STATUS);
                    if (currentCommNetworkTestStatus >= CURRENT_COMM_NETWORK_FAILURE_RESET_STATE.RESTART_MOBILE_DATA) {
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.applicationInitializedMobileDataRestart, -1, -1);
                    } else {
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.mobileDataRestored, -1, -1);
                    }
                }
            } else {
                if (!isMobileDataEnabled) {
                    int currentCommNetworkTestStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_COMM_NETWORK_TEST_STATUS);
                    if (currentCommNetworkTestStatus >= CURRENT_COMM_NETWORK_FAILURE_RESET_STATE.RESTART_MOBILE_DATA) {
                        handleMobileDataNotEnabled(isMobileDataEnabled, isInAirplaneMode);

                        boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();
                        if (isKnoxLicenceActivated) {
                            KnoxUtil.getInstance().getKnoxSDKImplementation().setMobileDataMode(true);
                        }
                    } else if (isMobileDataWasEnabledFromLocalDB) {
                        handleMobileDataNotEnabled(isMobileDataEnabled, isInAirplaneMode);

                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.mobileDataDisabled, -1, -1);
                    }
                } else if (isInAirplaneMode && isMobileDataWasEnabledFromLocalDB) {
                    handleMobileDataNotEnabled(isMobileDataEnabled, isInAirplaneMode);

                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.mobileDataDisabled, -1, -1);
                }
            }
        }
    }


    private void handleMobileDataNotEnabled(boolean isMobileDataEnabled, boolean isInAirplandeMode) {
        String messageToUpload;
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_IS_MOBILE_DATA_ENABLED, 0);

        messageToUpload = "NetworkChangeReceiver -> Mobile data: " + isMobileDataEnabled + " , Airplande mode: " + isInAirplandeMode;
        App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Receivers);
    }
}
