package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityDebugInfo;

import java.util.ArrayList;
import java.util.List;

public class TableDebugInfo extends DatabaseTable {

    public interface DebugInfoPriority {
        int LOW_PRIORITY = 0;
        int NORMAL_PRIORITY = 1;
        int HIGH_PRIORITY = 2;
    }

    public final static int MAX_LOG_PER_REQ = 50;

    //Table
    private static final String TABLE_DEBUG_INFO = "DebugInfo";

    // Columns names
    private static final String COLUMN_DEBUG_INFO_DATE_TIME = "dateTime";
    private static final String COLUMN_DEBUG_INFO_DEBUG_LV1_ID = "debugLV1Id";
    private static final String COLUMN_DEBUG_INFO_MESSAGE = "message";
    private static final String COLUMN_DEBUG_INFO_MESSAGE_ID = "messageId";
    private static final String COLUMN_DEBUG_INFO_MODULE_ID = "moduleId";
    private static final String COLUMN_DEBUG_INFO_OBJECT_ID = "objectId";
    private static final String COLUMN_DEBUG_INFO_REQUEST_ID = "requestId";
    private static final String COLUMN_DEBUG_INFO_SUB_MODULE_ID = "subModuleId";
    private static final String COLUMN_DEBUG_INFO_PRIORITY_RECORD = "priorityRecord";

    public TableDebugInfo() {

        this.tableName = TABLE_DEBUG_INFO;
        this.columnNumber = 0;

        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_DATE_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_DEBUG_LV1_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_MESSAGE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_MESSAGE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER_PK));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_MODULE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_OBJECT_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_REQUEST_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_SUB_MODULE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_PRIORITY_RECORD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));


        BuildColumnNameArray();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityDebugInfo recordDebugInfo = (EntityDebugInfo) databaseEntity;

        ContentValues values = new ContentValues();

        values.put(COLUMN_DEBUG_INFO_DATE_TIME, recordDebugInfo.dateTime);
        values.put(COLUMN_DEBUG_INFO_DEBUG_LV1_ID, recordDebugInfo.debugLV1Id);
        values.put(COLUMN_DEBUG_INFO_MESSAGE, recordDebugInfo.message);
        values.put(COLUMN_DEBUG_INFO_MODULE_ID, recordDebugInfo.mouduleId);
        values.put(COLUMN_DEBUG_INFO_OBJECT_ID, recordDebugInfo.objectId);
        values.put(COLUMN_DEBUG_INFO_REQUEST_ID, recordDebugInfo.requestId);
        values.put(COLUMN_DEBUG_INFO_SUB_MODULE_ID, recordDebugInfo.subModuleId);
        values.put(COLUMN_DEBUG_INFO_PRIORITY_RECORD, recordDebugInfo.priorityRecord);


        return database.insert(TABLE_DEBUG_INFO, null, values);
    }

    @Override
    protected EntityDebugInfo GetRecordFromQueryCursor(Cursor QueryCursor) {

        return new EntityDebugInfo(
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_DATE_TIME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_DEBUG_LV1_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_MESSAGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_MESSAGE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_MODULE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_OBJECT_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_REQUEST_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_SUB_MODULE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_PRIORITY_RECORD))
        );
    }

    public List<EntityDebugInfo> getDeviceInfoRecordsForUpload() {
        return getDeviceInfoRecordsForUpload(TableDebugInfo.MAX_LOG_PER_REQ);
    }

    public List<EntityDebugInfo> getDeviceInfoRecordsForUpload(int numberOfRowsToQuery) {

        List<EntityDebugInfo> recordDebugInfoList = new ArrayList<EntityDebugInfo>();

        // Select query
        Cursor cursordebugInfoForUpload = databaseReference.query(
                TABLE_DEBUG_INFO,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                COLUMN_DEBUG_INFO_PRIORITY_RECORD + " DESC",            // Order by insert order
                String.valueOf(numberOfRowsToQuery));    // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.

        while (cursordebugInfoForUpload.moveToNext()) {
            recordDebugInfoList.add(GetRecordFromQueryCursor(cursordebugInfoForUpload));
        }
        cursordebugInfoForUpload.close();
        return recordDebugInfoList;
    }

    public int deleteMoreRowsWithSameInfoMessage(String infoMessage) {
        String Where = COLUMN_DEBUG_INFO_MESSAGE + "=?";
        return databaseReference.delete(tableName, Where, new String[]{infoMessage});
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

    }

}
