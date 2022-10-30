package com.supercom.puretrack.data.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableApnDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.date.TimeUtil;

public class SimChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        boolean simInserted = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
        boolean simRemoved = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT;
        if (simInserted && DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_SIM_CARD) != -1) {
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] SimChangedReceiver - Sim card inserted", false);
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.SimCardInserted, -1, -1);
            TableApnDetailsManager.sharedInstance().createApnIfNotExists();
        } else {
            if (simRemoved && DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.VIOLATION_SIM_CARD) == -1) {
                LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] SimChangedReceiver - Sim card removed", false);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.SimCardRemoved, -1, -1);
            }
        }
    }

}
