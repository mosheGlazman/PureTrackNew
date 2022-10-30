package com.supercom.puretrack.data.source.local.table_managers;

import android.os.Handler;

import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityDebugInfo;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.runnable.BaseFutureRunnable;

import java.util.concurrent.TimeUnit;

public class TableDebugInfoManager {

    Handler cyclicDBRowCounterLogHandler;
    CyclicRowCounterLogRunnable cyclicRowCounterLogRunnable;

    private static final TableDebugInfoManager INSTANCE = new TableDebugInfoManager();

    private TableDebugInfoManager() {
    }

    public static TableDebugInfoManager sharedInstance() {
        return INSTANCE;
    }

    public void addNewRecordToDB(long currentTimeInSeconds, String message, int debugInfoMoudleId, int priorityRecord) {

        int logConfig = TableOffenderDetailsManager.sharedInstance().getDebugInfoConfig();
        int mask = (1 << debugInfoMoudleId);
        if (((logConfig & mask) > 0) || (priorityRecord >= DebugInfoPriority.HIGH_PRIORITY)) {
            EntityDebugInfo recordDebugInfo = new EntityDebugInfo(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), 0, message, -1,
                    debugInfoMoudleId, 0, 0, 0, priorityRecord);
            DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_DEBUG_INFO, recordDebugInfo);
        }
    }

    public void startCyclicRowCounterLogRunnable() {
        cyclicRowCounterLogRunnable = new CyclicRowCounterLogRunnable();
        cyclicDBRowCounterLogHandler = new Handler();
        scheduleFutureRunForRowCounterLog();
    }

    public class CyclicRowCounterLogRunnable extends BaseFutureRunnable {

        @Override
        public void run() {
            String eventLogRowCounter = String.valueOf(DatabaseAccess.getInstance().tableEventLog.getAllEventLogRecords().size());
            String gpsPointRowCounter = String.valueOf(DatabaseAccess.getInstance().tableGpsPoint.getAllGpsPointRecords().size());
            String opernEventsLogRowCounter = String.valueOf(DatabaseAccess.getInstance().tableOpenEventsLog.getAllOpenEventLogRecords().size());

            String rowCounters = "EventLog = " + eventLogRowCounter + ", GpsPoint = " + gpsPointRowCounter + ", OpenEventsLog = " + opernEventsLogRowCounter;
            LoggingUtil.updateNetworkLog("\n" + rowCounters, false);
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), rowCounters,
                    DebugInfoModuleId.DB.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            scheduleFutureRunForRowCounterLog();
        }
    }

    private void scheduleFutureRunForRowCounterLog() {
        long networkCycleInterval = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL);
        cyclicRowCounterLogRunnable.scheduleFutureRun(cyclicDBRowCounterLogHandler, 2 * TimeUnit.SECONDS.toMillis(networkCycleInterval));
    }

}
