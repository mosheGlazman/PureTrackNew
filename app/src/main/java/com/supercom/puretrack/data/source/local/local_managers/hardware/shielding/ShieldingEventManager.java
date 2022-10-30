package com.supercom.puretrack.data.source.local.local_managers.hardware.shielding;

import android.util.Log;

import com.supercom.puretrack.data.source.local.table.TableDebugInfo;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;

import java.util.concurrent.TimeUnit;

public class ShieldingEventManager {

    public static void openEvent(){
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "Opening shielding event",
                DebugInfoModuleId.Shielding.ordinal(), TableDebugInfo.DebugInfoPriority.HIGH_PRIORITY);
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceShieldingOpen);
        Log.e(DeviceShieldingManager.TAG, "OPEN event success");
    }

    public static  void closeEvent(){
        Log.i(DeviceShieldingManager.TAG, "CLOSE event success");
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), "Closing shielding event",
                DebugInfoModuleId.Shielding.ordinal(), TableDebugInfo.DebugInfoPriority.HIGH_PRIORITY);
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.deviceShieldingClosed);
    }
}
