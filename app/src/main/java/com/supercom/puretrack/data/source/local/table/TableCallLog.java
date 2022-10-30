package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityCallLog;

import java.util.ArrayList;
import java.util.List;

public class TableCallLog extends DatabaseTable {

    //Table
    private static final String TABLE_CALL_LOG = "CallLog";

    // Columns names
    private static final String COLUMN_CALL_NUMBER = "callNumber";
    private static final String COLUMN_CALL_TYPE_SYSTEM = "callType";
    private static final String COLUMN_CALL_LENGTH = "callLength";
    private static final String COLUMN_CALL_TIME_SYSTEM = "callTime";
    private static final String COLUMN_CALL_ID = "columnCallId";
    private static final String COLUMN_CALL_TYPE_FILTERED = "columnTypeFiltered";
    private static final String COLUMN_CALL_CONDUCTED = "columnCallConducted";
    private static final String COLUMN_CALL_TIME_FILTERED = "callTimeFiltered";


    public TableCallLog() {

        this.tableName = TABLE_CALL_LOG;
        this.columnNumber = 0;

        this.AddColumn(new DatabaseColumn(COLUMN_CALL_NUMBER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_CALL_TYPE_SYSTEM, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_CALL_LENGTH, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_CALL_TIME_SYSTEM, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_CALL_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER_PK));
        this.AddColumn(new DatabaseColumn(COLUMN_CALL_TYPE_FILTERED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_CALL_CONDUCTED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_CALL_TIME_FILTERED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityCallLog recordCallLog = (EntityCallLog) databaseEntity;

        ContentValues values = new ContentValues();

        values.put(COLUMN_CALL_TYPE_SYSTEM, recordCallLog.callTypeSystem);
        values.put(COLUMN_CALL_LENGTH, recordCallLog.callLength);
        values.put(COLUMN_CALL_TIME_SYSTEM, recordCallLog.callTimeSystem);
        values.put(COLUMN_CALL_NUMBER, recordCallLog.callNumber);
        values.put(COLUMN_CALL_TYPE_FILTERED, recordCallLog.callTypeFiltered);
        values.put(COLUMN_CALL_CONDUCTED, recordCallLog.conducted);
        values.put(COLUMN_CALL_TIME_FILTERED, recordCallLog.callTimeFiltered);

        return database.insert(TABLE_CALL_LOG, null, values);
    }

    public List<EntityCallLog> getCallLogsRecordsForUpload() {
        List<EntityCallLog> callLogsRecordsList = new ArrayList<EntityCallLog>();
        // Select query
        Cursor cursorCallLogsForUpload = databaseReference.query(
                TABLE_CALL_LOG,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.


        while (cursorCallLogsForUpload.moveToNext()) {
            callLogsRecordsList.add((EntityCallLog) GetRecordFromQueryCursor(cursorCallLogsForUpload));
        }
        cursorCallLogsForUpload.close();

        return callLogsRecordsList;
    }

    @Override
    protected DatabaseEntity GetRecordFromQueryCursor(Cursor QueryCursor) {

        return new EntityCallLog(
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_CALL_NUMBER)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_CALL_TYPE_SYSTEM)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_CALL_LENGTH)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_CALL_CONDUCTED)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_CALL_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_CALL_TYPE_FILTERED)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_CALL_TYPE_SYSTEM)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_CALL_TIME_FILTERED)));
    }
}
