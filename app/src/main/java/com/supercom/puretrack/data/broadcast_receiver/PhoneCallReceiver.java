package com.supercom.puretrack.data.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.CURRENT_COMM_NETWORK_FAILURE_RESET_STATE;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.ui.activity.BaseActivity;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.concurrent.TimeUnit;

public class PhoneCallReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
            goForProperShutDown();
        } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            LoggingUtil.updateNetworkLog("\n\n***  [" + TimeUtil.getCurrentTimeStr() + "] PhoneCallReceiver BOOT_COMPLETED\n", false);
            String messageToUpload = "BOOT_COMPLETED";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Receivers.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            App.setAppContext(context);
            launchPureTrack(context);
        } else if ("android.intent.action.REBOOT".equals(intent.getAction())) {
            goForProperShutDown();
        }
    }

    private void goForProperShutDown() {
        int currentCommNetworkTestStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_COMM_NETWORK_TEST_STATUS);

        if (currentCommNetworkTestStatus != CURRENT_COMM_NETWORK_FAILURE_RESET_STATE.RESTART_DEVICE) {
            Log.i(getClass().getSimpleName(), "goForProperShutDown()");
            TableOffenderDetailsManager.sharedInstance().updateColumnLong(OFFENDER_DETAILS_CONS.DETAILS_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI, 0);


            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventPowerOff, -1, -1, System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(1));
        }
    }

    private void launchPureTrack(Context context) {
        if (!App.isActivityAlreadyRunning(context, MainActivity.class.getName())) {
            BaseActivity.isCameFromLauncherActivityCode = true;
            ((App) App.getContext()).launchPureTracActivity(MainActivity.SHOULD_CREATE_STARTUP_EVENT);
        } else {
            TableEventsManager.sharedInstance().addDeviceSatrtupEventToLogIfNeed();
        }
    }

}
