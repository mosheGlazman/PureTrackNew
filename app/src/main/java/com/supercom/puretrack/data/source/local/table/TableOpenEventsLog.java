package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityOpenEventLog;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationSeverityTypes;

import java.util.ArrayList;
import java.util.List;


public class TableOpenEventsLog extends DatabaseTable {
    public int OpenEventId;
    public int OpenEventType;
    public int OpenEventZoneId;
    public int OpenEventScheduleId;
    public int OpenEventViolationCategory;
    public int OpenEventViolationSeverity;

    public interface IsHandledEvent {
        int NOT_HANDLED = 0;
        int HANDLED = 1;
    }

    public enum Flow_Type {
        RegularFlow,
        Unallocated
    }

    private static final int IS_HANDLED = 0;

    // Table name
    private static final String TABLE_OPEN_EVENT_LOG = "OpenEventLog";
    // Column names
    private static final String COLUMN_OPEN_EVENT_ID = "OpenEventId";    // Will hold the primary key EventId from TableEventLog
    private static final String COLUMN_OPEN_EVENT_TYPE = "OpenEventType";
    private static final String COLUMN_OPEN_EVENT_ZONE_ID = "OpenEventZoneId";
    private static final String COLUMN_OPEN_EVENT_SCHEDULE_ID = "OpenEventScheduleId";
    private static final String COLUMN_OPEN_EVENT_VIOLATION_CATEGORY = "OpenEventViolationCategory";
    private static final String COLUMN_OPEN_EVENT_VIOLATION_SEVERITY = "OpenEventViolationSeverity";
    private static final String COLUMN_OPEN_EVENT_IS_HANDLED = "OpenEventisHandeled";

    private EntityOpenEventLog _recOpenEventLog;

    public TableOpenEventsLog() {
        this.tableName = TABLE_OPEN_EVENT_LOG;
        this.columnNumber = 0;

        this.AddColumn(new DatabaseColumn(COLUMN_OPEN_EVENT_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OPEN_EVENT_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OPEN_EVENT_ZONE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OPEN_EVENT_SCHEDULE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OPEN_EVENT_VIOLATION_CATEGORY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OPEN_EVENT_VIOLATION_SEVERITY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OPEN_EVENT_IS_HANDLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));


        // Build a string array of column names (useful for some queries)
        buildColumnNameArrayWithoutRowId();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityOpenEventLog recOpenEventLog = (EntityOpenEventLog) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_OPEN_EVENT_ID, recOpenEventLog.OpenEventId);
        values.put(COLUMN_OPEN_EVENT_TYPE, recOpenEventLog.OpenEventType);
        values.put(COLUMN_OPEN_EVENT_ZONE_ID, recOpenEventLog.OpenEventZoneId);
        values.put(COLUMN_OPEN_EVENT_SCHEDULE_ID, recOpenEventLog.OpenEventScheduleId);
        values.put(COLUMN_OPEN_EVENT_VIOLATION_CATEGORY, recOpenEventLog.OpenEventViolationCategory);
        values.put(COLUMN_OPEN_EVENT_VIOLATION_SEVERITY, recOpenEventLog.OpenEventViolationSeverity);
        values.put(COLUMN_OPEN_EVENT_IS_HANDLED, recOpenEventLog.OpenEventIsHandeled);


        _recOpenEventLog = recOpenEventLog;
        printContentValues(values);
        // Insert the record
        return database.insert(TABLE_OPEN_EVENT_LOG, null, values);
    }

    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        // No default data.
    }

    private EntityOpenEventLog GetRecFromQueryCursor(Cursor QueryCursor) {
        return new EntityOpenEventLog(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OPEN_EVENT_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OPEN_EVENT_TYPE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OPEN_EVENT_ZONE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OPEN_EVENT_SCHEDULE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OPEN_EVENT_VIOLATION_CATEGORY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OPEN_EVENT_VIOLATION_SEVERITY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OPEN_EVENT_IS_HANDLED))
        );
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
    }

    public long getOpenerIdByViolationCategory(int violationCategory) {
        return getOpenerIdForZoneViolationCategory(violationCategory, -1);
    }

    public long getOpenerIdForZoneViolationCategory(int violationCategory, int zoneId) {
        EntityOpenEventLog recOpenEventLog = null;

        String Where;
        if (zoneId != -1) {
            Where = String.format("%s = %d AND %s = %d", COLUMN_OPEN_EVENT_VIOLATION_CATEGORY, violationCategory,
                    COLUMN_OPEN_EVENT_ZONE_ID, zoneId);
        } else {
            Where = String.format("(%s = %d )", COLUMN_OPEN_EVENT_VIOLATION_CATEGORY, violationCategory);
        }
        Cursor cursorCurrentSchedule = databaseReference.query(TABLE_OPEN_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                null);                // Limit...

        if (cursorCurrentSchedule.getCount() > 0) {
            cursorCurrentSchedule.moveToLast();
            recOpenEventLog = GetRecFromQueryCursor(cursorCurrentSchedule);
        }
        cursorCurrentSchedule.close();

        return (recOpenEventLog != null) ? recOpenEventLog.OpenEventId : -1;
    }


    /*
     * return the max severity type that open by this priority : A. Violation B. Alarm C. Normal
     */
    public int getOffenderStatus() {
        if (hasOpenEventFromSeverityTypeAndNotHandeledManually(ViolationSeverityTypes.VIOLATION)) {
            return ViolationSeverityTypes.VIOLATION;
        } else if (hasOpenEventFromSeverityTypeAndNotHandeledManually(ViolationSeverityTypes.ALARM)) {
            return ViolationSeverityTypes.ALARM;
        }
        return ViolationSeverityTypes.NORMAL;

    }

    private boolean hasOpenEventFromSeverityTypeAndNotHandeledManually(int severityType) {

        Cursor cursor = databaseReference.query(TABLE_OPEN_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                COLUMN_OPEN_EVENT_VIOLATION_SEVERITY + " = ? AND " + COLUMN_OPEN_EVENT_IS_HANDLED + " = ? ",
                new String[]{String.valueOf(severityType), String.valueOf(IsHandledEvent.NOT_HANDLED)},
                null,
                null,
                null,
                null);

        boolean hasOpenEventsFromSeverityType = (cursor.getCount() > 0);

        cursor.close();

        return hasOpenEventsFromSeverityType;
    }

    public boolean hasOpenEventInViolationCategory(int violationCategory) {

        Cursor cursor = databaseReference.query(TABLE_OPEN_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                COLUMN_OPEN_EVENT_VIOLATION_CATEGORY + " = ?",
                new String[]{String.valueOf(violationCategory)},
                null,
                null,
                null,
                null);

        boolean hasOpenEventInViolationCategory = (cursor.getCount() > 0);

        cursor.close();

        return hasOpenEventInViolationCategory;
    }

    /**
     * Will check if there is an Open Event of eventType for the zoneId.
     * @return True if for zoneId there is an Open Event of eventType , otherwise False.
     */
    public boolean hasOpenEvent_ForZone(int eventType, int zoneId) {
        int eventCount;
        String Where = String.format("(%s = %d) AND (%s = %d)", COLUMN_OPEN_EVENT_ZONE_ID, zoneId, COLUMN_OPEN_EVENT_TYPE, eventType);
        // Select query
        Cursor cursor = databaseReference.query(
                TABLE_OPEN_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,                // Where ...
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                COLUMN_OPEN_EVENT_ID,            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.

        eventCount = cursor.getCount();
        cursor.close();

        return eventCount > 0;
    }

    public int deleteAll_WithViolationCategory(int violationCategory, int zoneId) {
        String Where;

        if (zoneId != -1) {
            Where = String.format("%s = %d AND %s = %d", COLUMN_OPEN_EVENT_VIOLATION_CATEGORY, violationCategory,
                    COLUMN_OPEN_EVENT_ZONE_ID, zoneId);
        } else {
            Where = String.format("%s = %d", COLUMN_OPEN_EVENT_VIOLATION_CATEGORY, violationCategory);
        }

        return databaseReference.delete(TABLE_OPEN_EVENT_LOG, Where, null);
    }

    public int deleteOpenEventFromDB(int eventId) {
        String Where = String.format("%s = %d", COLUMN_OPEN_EVENT_ID, eventId);

        return databaseReference.delete(TABLE_OPEN_EVENT_LOG, Where, null);
    }

    public EntityOpenEventLog GetRecord(SQLiteDatabase db) {
        // Select query
        Cursor QueryCursor = db.query(TABLE_OPEN_EVENT_LOG, columnNamesArray, null, null, null, null, null);
        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }

        return GetRecFromQueryCursor(QueryCursor);
    }

    @Override
    public void LoadData(SQLiteDatabase db) {
        _recOpenEventLog = this.GetRecord(db);
    }

    public void Update(EntityOpenEventLog Rec) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_OPEN_EVENT_ID, Rec.OpenEventId);
        values.put(COLUMN_OPEN_EVENT_TYPE, Rec.OpenEventType);
        values.put(COLUMN_OPEN_EVENT_ZONE_ID, Rec.OpenEventZoneId);
        values.put(COLUMN_OPEN_EVENT_SCHEDULE_ID, Rec.OpenEventScheduleId);
        values.put(COLUMN_OPEN_EVENT_VIOLATION_CATEGORY, Rec.OpenEventViolationCategory);
        values.put(COLUMN_OPEN_EVENT_VIOLATION_SEVERITY, Rec.OpenEventViolationSeverity);
        values.put(COLUMN_OPEN_EVENT_IS_HANDLED, Rec.OpenEventIsHandeled);

        databaseReference.update(TABLE_OPEN_EVENT_LOG, values, null, null);
        // Load local copy
        LoadData(databaseReference);
    }

    public EntityOpenEventLog Get() {
        return _recOpenEventLog;
    }

    public List<EntityOpenEventLog> getAllOpenEventLogRecords() {
        List<EntityOpenEventLog> recordOpenEventLogList = new ArrayList<EntityOpenEventLog>();
        // Select query
        Cursor cursorOpenEventLogAll = databaseReference.query(
                TABLE_OPEN_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                "rowid",            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.


        while (cursorOpenEventLogAll.moveToNext()) {
            recordOpenEventLogList.add(GetRecFromQueryCursor(cursorOpenEventLogAll));
        }
        cursorOpenEventLogAll.close();

        return recordOpenEventLogList;
    }

    public List<EntityOpenEventLog> getAllOpenEventLogRecordsThatNotHandeledManually() {

        List<EntityOpenEventLog> recordOpenEventLogList = new ArrayList<EntityOpenEventLog>();
        // Select query
        Cursor cursorOpenEventLogAll = databaseReference.query(
                TABLE_OPEN_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                COLUMN_OPEN_EVENT_IS_HANDLED + " = ? ",        // Where ... null - returns all rows
                new String[]{String.valueOf(IsHandledEvent.NOT_HANDLED)},                // Where Args
                null,                // Group by...
                null,                // Having...
                "rowid",            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.


        while (cursorOpenEventLogAll.moveToNext()) {
            recordOpenEventLogList.add(GetRecFromQueryCursor(cursorOpenEventLogAll));
        }
        cursorOpenEventLogAll.close();

        return recordOpenEventLogList;

    }

    public EntityOpenEventLog getOpenEventByEventId(int eventId) {
        EntityOpenEventLog recOpenEventLog = null;

        String Where = String.format("%s = %d", COLUMN_OPEN_EVENT_ID, eventId);
        Cursor cursorOpenEvent = databaseReference.query(TABLE_OPEN_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                null);                // Limit...

        if (cursorOpenEvent.getCount() > 0) {
            cursorOpenEvent.moveToLast();
            recOpenEventLog = GetRecFromQueryCursor(cursorOpenEvent);
        }
        cursorOpenEvent.close();

        return recOpenEventLog;
    }

    public long getOpenEventByEventType(int eventType) {
        EntityOpenEventLog recOpenEventLog = null;

        String Where = String.format("%s = %d", COLUMN_OPEN_EVENT_TYPE, eventType);
        Cursor cursorOpenEvent = databaseReference.query(TABLE_OPEN_EVENT_LOG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                null);                // Limit...

        if (cursorOpenEvent.getCount() > 0) {
            cursorOpenEvent.moveToLast();
            recOpenEventLog = GetRecFromQueryCursor(cursorOpenEvent);
        }
        cursorOpenEvent.close();

        return (recOpenEventLog != null) ? recOpenEventLog.OpenEventId : -1;
    }

    public void updateIsHandleColumnByOpenEventId(int openEventRecord) {
        String where = COLUMN_OPEN_EVENT_ID + " = ? ";
        DatabaseAccess.getInstance().UpdateField(EnumDatabaseTables.TABLE_OPEN_EVENT_LOG, COLUMN_OPEN_EVENT_IS_HANDLED, IsHandledEvent.HANDLED,
                where, new String[]{String.valueOf(openEventRecord)});
    }
}
