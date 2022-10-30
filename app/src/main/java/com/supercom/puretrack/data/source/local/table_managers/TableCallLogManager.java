package com.supercom.puretrack.data.source.local.table_managers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.database.CallLogObserver;
import com.supercom.puretrack.model.database.entities.EntityCallLog;
import com.supercom.puretrack.util.shared_preferences.PureTrackSharedPreferences;
import com.supercom.puretrack.util.date.TimeUtil;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TableCallLogManager {

    private final Context context;
    private final CallLogObserver callLogObserver;

    public TableCallLogManager(Context context) {
        this.context = context;
        callLogObserver = new CallLogObserver(new callLogHandler(this));
        context.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);
    }

    /**
     * @param callType the type of the call
     *                 return 0 outgoing call, 1 of others
     */
    public int getPhoneCallLogType(int callType) {
        if (callType == CallLog.Calls.OUTGOING_TYPE) {
            return 0;
        }
        return 1;
    }

    /**
     * @param length the length of the call
     *               return 0 if user did not pick up the call or 1 if user picked up the call
     */
    public int getConductedType(long length) {
        if (length == 0) {
            return 0;
        }
        return 1;
    }

    public void getCallDetailsFromPhone() {
        try {

            Uri contacts = CallLog.Calls.CONTENT_URI;
            String where = String.format("(%s BETWEEN %d AND %d)", CallLog.Calls.DATE, TimeUtil.GetLocalTime() - TimeUnit.HOURS.toMillis(24), TimeUtil.GetLocalTime());
            Cursor managedCursor = context.getContentResolver().query(contacts, null, where, null, null);

            if (managedCursor != null) {

                int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
                int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
                int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
                int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);

                String dateString;
                long dateItemLong;
                long lastPhoneCallTime = PureTrackSharedPreferences.getLastPhoneCallTime();
                long tempLatestCallTime = lastPhoneCallTime;
                while (managedCursor.moveToNext()) {
                    String numberString = managedCursor.getString(number);
                    int typeInt = Integer.parseInt(managedCursor.getString(type));
                    String durationString = managedCursor.getString(duration);
                    dateString = managedCursor.getString(date);
                    dateItemLong = Long.parseLong(dateString);

                    int callTypeFiltered = getPhoneCallLogType(typeInt);
                    int callTypeConducted = getConductedType(Long.parseLong(durationString));

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateItemLong);
                    long callTimeFiltered = TimeUtil.convertDateToUTCTimeInSeconds(cal.getTime());

                    // if there was new call action(income, outcome or missing and etc..) from last check
                    if (dateItemLong > lastPhoneCallTime) {
                        EntityCallLog recordCallLog = new EntityCallLog(numberString, typeInt, durationString, dateString, 0, callTypeFiltered, callTypeConducted, callTimeFiltered);
                        DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_CALL_LOG, recordCallLog);

                        //find the latest call in cycle in order to save later to SharedPreferences
                        if (dateItemLong > tempLatestCallTime) {
                            tempLatestCallTime = dateItemLong;
                        }
                    }
                }

                managedCursor.close();

                // update to last phone call time
                PureTrackSharedPreferences.setLastPhoneCallTime(tempLatestCallTime);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterCallLogContentObserver() {
        context.getContentResolver().unregisterContentObserver(callLogObserver);
    }

    private static class callLogHandler extends Handler {

        WeakReference<TableCallLogManager> callLogManagerClassReference;

        public callLogHandler(TableCallLogManager tableCallLogManager) {
            callLogManagerClassReference = new WeakReference<TableCallLogManager>(tableCallLogManager);
        }

        public void handleMessage(Message msg) {
            TableCallLogManager tableCallLogManager = callLogManagerClassReference.get();
            if (tableCallLogManager != null) {
                tableCallLogManager.getCallDetailsFromPhone();
            }
        }
    }

}

