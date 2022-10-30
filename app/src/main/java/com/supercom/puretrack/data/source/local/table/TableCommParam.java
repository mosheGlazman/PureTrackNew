package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.util.constants.database_defaults.DefaultDatabaseData;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.EntityCommParam;

public class TableCommParam extends DatabaseTable {
    // Table name
    private static final String TABLE_COMM_PARAM = "CommParam";
    // Column names
    private static final String COLUMN_PARAM_INTERVAL_OK = "CommPrmIntervalOk";
    private static final String COLUMN_PARAM_INTERVAL_LOW = "CommPrmIntervalLow";
    private static final String COLUMN_PARAM_RETRY_TIME = "CommPrmRetryWaitTime";
    private static final String COLUMN_PARAM_HTTP_TIMEOUT = "CommPrmHttpTimeout";
    // Record for quick access
    private EntityCommParam CommParamRec;


    /**
     * Constructor: update table's name, build columns
     */
    public TableCommParam() {
        this.tableName = TABLE_COMM_PARAM;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_PARAM_INTERVAL_OK, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_PARAM_INTERVAL_LOW, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_PARAM_RETRY_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_PARAM_HTTP_TIMEOUT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     * Get record from any query cursor
     */
    private EntityCommParam GetRecFromQueryCursor(Cursor QueryCursor) {
        EntityCommParam DevRec = new EntityCommParam(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_PARAM_INTERVAL_OK)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_PARAM_INTERVAL_LOW)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_PARAM_RETRY_TIME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_PARAM_HTTP_TIMEOUT)));

        DevRec.SetRowId(QueryCursor.getLong(QueryCursor.getColumnIndex("rowid")));

        return DevRec;
    }

    public EntityCommParam GetRecord(SQLiteDatabase db) {
        // Select query
        Cursor QueryCursor = db.query(TABLE_COMM_PARAM, columnNamesArray, null, null, null, null, null);
        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }

        return GetRecFromQueryCursor(QueryCursor);
    }

    /**
     * Add default data record/s
     */
    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        // 	default data record
        addRecord(db, new EntityCommParam(DefaultDatabaseData.CommSyncIntervalOk,
                DefaultDatabaseData.CommSyncIntervalLowBattery,
                DefaultDatabaseData.CommRetryWaitTime,
                DefaultDatabaseData.CommHttpTimeout));
    }

    @Override
    public void LoadData(SQLiteDatabase db) {
        CommParamRec = this.GetRecord(db);
    }

    public void Update(EntityCommParam Rec) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARAM_INTERVAL_OK, Rec.Interval);
        values.put(COLUMN_PARAM_INTERVAL_LOW, Rec.IntervalLowBattery);
        values.put(COLUMN_PARAM_RETRY_TIME, Rec.RetryWaitTime);
        values.put(COLUMN_PARAM_HTTP_TIMEOUT, Rec.HttpTimeout);

        databaseReference.update(TABLE_COMM_PARAM,
                values,
                null,
                null);
        // Load local copy
        LoadData(databaseReference);
    }

    public EntityCommParam Get() {
        return CommParamRec;
    }
}
	
	
	
	
	
