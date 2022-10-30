package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityEventLog;

import java.util.ArrayList;
import java.util.List;

public class TableEventLog extends DatabaseTable {
    private final static int MAX_EVENT_LOG_PER_REQ = 50;
    public final static int MAX_SYNC_RETRY_COUNT = 1;
    public final static int REC_STATUS_UPLOADING = 1;

    public static final String TAG = "TableEventLog";
    // Table: GPS points
    private static final String TABLE_EVENT_LOG = "EventLog";
    // Column names
    private static final String COLUMN_PK_EVENT_ID = "PKEventId";
    private static final String COLUMN_EVENT_ZONE_ID = "EventZoneId";
    private static final String COLUMN_EV_TYPE = "EventType";
    private static final String COLUMN_EV_UTC_TIME = "EventUtcTime";
    private static final String COLUMN_EV_TZ = "EventTimezone";
    private static final String COLUMN_EV_ENTITY_ID = "EventEntityId";
    private static final String COLUMN_EV_DEV_STATUS = "EventDevStatus";
    private static final String COLUMN_EV_OFF_STATUS = "EventOffStatus";
    private static final String COLUMN_EV_EXTRA_DATA = "EventExtraData";
    private static final String COLUMN_EV_RELATED_ID = "EventRelatedId";
    private static final String COLUMN_EV_REQUEST_ID = "EventRequestId";
    private static final String COLUMN_EV_REC_STAT = "EventStat";

    private static final String COLUMN_EV_TAG_CASE_TAMPER_STAT = "EventTagCaseTamperStat";
    private static final String COLUMN_EV_TAG_STRAP_TAMPER_STAT = "EventTagStrapTamperStat";
    private static final String COLUMN_EV_TAG_BATTERY_TAMPER_STAT = "EventTagBatteryTamperStat";
    private static final String COLUMN_EV_BEACON_LAST_COMMUNICATION = "EventBeaconLastCommunication";
    private static final String COLUMN_EV_BEACON_BATTERY_TAMPER_STAT = "EventBeaconBatteryTamperStat";
    private static final String COLUMN_EV_BEACON_CASE_TAMPER_STAT = "EventBeaconCaseTamperStat";
    private static final String COLUMN_EV_BEACON_PROX_TAMPER_STAT = "EventBeaconProxTamperStat";

    private static final String COLUMN_EV_OFFENDER_IS_IN_RANGE = "EventOffIsInRange";

    private static final String COLUMN_EV_SYNC_RETRY_COUNT = "GpsSyncRetryCount";
    private static final String COLUMN_EV_ADDITIONAL_INFO = "AdditionalInfo";

    /**
     * Constructor: update table's name, build columns
     */
    public TableEventLog() {

        this.tableName = TABLE_EVENT_LOG;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_PK_EVENT_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER_PK));
        this.AddColumn(new DatabaseColumn(COLUMN_EVENT_ZONE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_UTC_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_TZ, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_ENTITY_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_DEV_STATUS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_OFF_STATUS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_EXTRA_DATA, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_RELATED_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_REQUEST_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_REC_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_EV_TAG_CASE_TAMPER_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_TAG_STRAP_TAMPER_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_TAG_BATTERY_TAMPER_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_BEACON_LAST_COMMUNICATION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_BEACON_BATTERY_TAMPER_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_BEACON_CASE_TAMPER_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_BEACON_PROX_TAMPER_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_EV_OFFENDER_IS_IN_RANGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_EV_SYNC_RETRY_COUNT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EV_ADDITIONAL_INFO, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     * Add event Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityEventLog EvRec = (EntityEventLog) databaseEntity;

        ContentValues values = new ContentValues();


        values.put(COLUMN_EV_TYPE, EvRec.EvType);
        values.put(COLUMN_EV_UTC_TIME, EvRec.UtcTime);
        values.put(COLUMN_EV_TZ, EvRec.Timezone);
        values.put(COLUMN_EV_ENTITY_ID, EvRec.EntityId); // Event Id
        values.put(COLUMN_EVENT_ZONE_ID, EvRec.EventZoneId); // Event related Zone Id
        values.put(COLUMN_EV_DEV_STATUS, EvRec.DevStatus); // DviceStatus
        values.put(COLUMN_EV_OFF_STATUS, EvRec.OffStatus); // Offender Status
        values.put(COLUMN_EV_EXTRA_DATA, EvRec.ExtraData); // Comment
        values.put(COLUMN_EV_RELATED_ID, EvRec.RelatedEvId); // Open Event
        values.put(COLUMN_EV_REQUEST_ID, EvRec.RequestId); //
        values.put(COLUMN_EV_REC_STAT, EvRec.RecStatus); // New or Sent

        values.put(COLUMN_EV_TAG_CASE_TAMPER_STAT, EvRec.TagCaseTamperStat);
        values.put(COLUMN_EV_TAG_STRAP_TAMPER_STAT, EvRec.TagStrapTamperStat);
        values.put(COLUMN_EV_TAG_BATTERY_TAMPER_STAT, EvRec.TagBatteryTamperStat);
        values.put(COLUMN_EV_BEACON_LAST_COMMUNICATION, EvRec.BeaconLastCommunication);
        values.put(COLUMN_EV_BEACON_BATTERY_TAMPER_STAT, EvRec.BeaconBatteryTamperStat);
        values.put(COLUMN_EV_BEACON_CASE_TAMPER_STAT, EvRec.BeaconCaseTamperStat);
        values.put(COLUMN_EV_BEACON_PROX_TAMPER_STAT, EvRec.BeaconProxTamperStat);
        values.put(COLUMN_EV_OFFENDER_IS_IN_RANGE, EvRec.OffIsInRange);

        values.put(COLUMN_EV_SYNC_RETRY_COUNT, EvRec.PureMonitorSyncRetryCount);
        values.put(COLUMN_EV_ADDITIONAL_INFO, EvRec.additionalInfo);

        // Insert to database

        printContentValues(values);
        return database.insert(TABLE_EVENT_LOG, null, values);
    }

    public int getLastEventLogIdByEventType(int type){
        Cursor cursor = databaseReference.rawQuery("select * from EventLog " +
                " where " + COLUMN_EV_TYPE + " = " + type + " order by " + COLUMN_PK_EVENT_ID + " limit 1 " , null);
        int recordEventLogId = -1;
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            recordEventLogId = cursor.getInt(cursor.getColumnIndex(COLUMN_PK_EVENT_ID));
            cursor.moveToNext();
        }
        cursor.close();
        return recordEventLogId;
    }

    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        // no default data
    }

    private EntityEventLog GetRecFromQueryCursor(Cursor QueryCursor) {
        EntityEventLog EvRec = new EntityEventLog(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_PK_EVENT_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EVENT_ZONE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_TYPE)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_EV_UTC_TIME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_EV_TZ)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_ENTITY_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_DEV_STATUS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_OFF_STATUS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_EV_EXTRA_DATA)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_RELATED_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_REQUEST_ID)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_TAG_CASE_TAMPER_STAT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_TAG_STRAP_TAMPER_STAT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_TAG_BATTERY_TAMPER_STAT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_BEACON_LAST_COMMUNICATION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_BEACON_BATTERY_TAMPER_STAT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_BEACON_CASE_TAMPER_STAT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_BEACON_PROX_TAMPER_STAT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_OFFENDER_IS_IN_RANGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EV_SYNC_RETRY_COUNT)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_EV_ADDITIONAL_INFO))
        );

        // Add fields which are not covered by the construct
        EvRec.RecStatus = QueryCursor.getColumnIndex(COLUMN_EV_REC_STAT);
        return EvRec;
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
    }

    /**
     * @return List <RecordEventLog>
     */
    public List<EntityEventLog> getAllEventLogRecords() {
        List<EntityEventLog> recordEventLogList = new ArrayList<EntityEventLog>();
        // Select query
        Cursor cursorEventLogAll = databaseReference.query(
                TABLE_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                "rowid",            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.


        while (cursorEventLogAll.moveToNext()) {
            recordEventLogList.add(GetRecFromQueryCursor(cursorEventLogAll));
        }
        cursorEventLogAll.close();

        return recordEventLogList;
    }

    public List<EntityEventLog> getAllNonSentEventLogRecords() {
        List<EntityEventLog> recordEventLogList = new ArrayList<>();

        // Select query
        Cursor cursorAllNonSentEventLogRecords = databaseReference.query(
                TABLE_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                "rowid",            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.

        int maxEventLogCounter = 0;
        while ((cursorAllNonSentEventLogRecords.moveToNext()) && maxEventLogCounter < MAX_EVENT_LOG_PER_REQ) {
            EntityEventLog recordEventLog = GetRecFromQueryCursor(cursorAllNonSentEventLogRecords);
            ContentValues values = new ContentValues();

            values.put(COLUMN_EV_REC_STAT, REC_STATUS_UPLOADING);
            int numOfUpdatedRows = databaseReference.update(TABLE_EVENT_LOG, values, String.format("%s = %d", COLUMN_PK_EVENT_ID, recordEventLog.PKEventId), null);

            if (numOfUpdatedRows > 0) {
                recordEventLogList.add(recordEventLog);
                maxEventLogCounter++;
            }
        }
        cursorAllNonSentEventLogRecords.close();

        return recordEventLogList;
    }

    public EntityEventLog getEventLogRecByRowId(long rowId) {
        EntityEventLog recordEventLog = null;
        String Where = String.format("(%s = %d)", "rowid", rowId);
        Cursor QueryCursor = databaseReference.query(TABLE_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        if (QueryCursor.getCount() > 0) {
            QueryCursor.moveToFirst();
            recordEventLog = GetRecFromQueryCursor(QueryCursor);
        }
        QueryCursor.close();

        return recordEventLog;
    }

    public boolean isEventExistsInDB(int type) {
        boolean hasEventExistsInDB = false;
        String Where = String.format("(%s = %d)", COLUMN_EV_TYPE, type);
        Cursor QueryCursor = databaseReference.query(TABLE_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        if (QueryCursor.getCount() > 0) {
            QueryCursor.moveToFirst();
            hasEventExistsInDB = true;
        }
        QueryCursor.close();

        return hasEventExistsInDB;
    }

    /**
     * Update the RecordSyncRetriesCount of the EventLog record.
     * Increasing the count till it reaches the allowed maximum, then the record will be deleted. (Default MAX_SYNC_RETRY_COUNT = 1)
     * @param RowId The record RowId to update.
     */
    public void updateRecordSyncRetriesCount(long RowId) {
        ContentValues values = new ContentValues();
        EntityEventLog recordEventLog = getEventLogRecByRowId(RowId);
        if (recordEventLog != null && recordEventLog.PureMonitorSyncRetryCount < TableEventLog.MAX_SYNC_RETRY_COUNT) {
            values.put(COLUMN_EV_SYNC_RETRY_COUNT, recordEventLog.PureMonitorSyncRetryCount + 1);
            databaseReference.update(TABLE_EVENT_LOG, values, String.format("rowid = %d", recordEventLog.PKEventId), null);
        }
    }

    public List<EntityEventLog> getEventsThatDidntFailToSend() {
        List<EntityEventLog> recordEventLogList = new ArrayList<EntityEventLog>();
        String Where = String.format("(%s = %d)", COLUMN_EV_SYNC_RETRY_COUNT, 0);
        Cursor QueryCursor = databaseReference.query(TABLE_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,
                null,// Order by insert order
                String.valueOf(MAX_EVENT_LOG_PER_REQ));                // Limit...

        while (QueryCursor.moveToNext()) {
            recordEventLogList.add(GetRecFromQueryCursor(QueryCursor));
        }
        QueryCursor.close();

        return recordEventLogList;
    }

    public int getNewEventsCount() {
        int Count;

        String Where = String.format("(%s = %d)", COLUMN_EV_SYNC_RETRY_COUNT, 0);

        Cursor QueryCursor = databaseReference.query(TABLE_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,    // Where
                null, // Where Args
                null,
                null,
                null,
                null);


        Count = QueryCursor.getCount();
        QueryCursor.close();

        return Count;
    }

    public int getFailedEventsCount() {
        int Count;

        String Where = String.format("(%s > %d)", COLUMN_EV_SYNC_RETRY_COUNT, 0);
        Cursor QueryCursor = databaseReference.query(TABLE_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,
                null,// Order by insert order
                null);                // Limit...

        Count = QueryCursor.getCount();
        QueryCursor.close();

        return Count;
    }
}
