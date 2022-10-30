package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;

import java.util.ArrayList;
import java.util.List;


public class TableGpsPoints extends DatabaseTable {


    // Table: GPS points
    private static final String TABLE_GPS_POINTS = "GpsPoints";
    // Columns names
    private static final String COLUMN_GPS_ID = "GpsId";
    private static final String COLUMN_GPS_OFF_ID = "GpsOffId";
    private static final String COLUMN_GPS_COMM_STAT = "GpsCommStat";
    private static final String COLUMN_GPS_TIME = "GpsTime";
    private static final String COLUMN_GPS_LAT = "GpsLat";
    private static final String COLUMN_GPS_LONG = "GpsLong";
    private static final String COLUMN_GPS_ALT = "GpsAlt";
    private static final String COLUMN_GPS_ACCURACY = "GpsAccuracy";
    private static final String COLUMN_GPS_SATELITES = "GpsSatNum";
    private static final String COLUMN_GPS_PROVIDER_TYPE = "GpsProviderType";
    private static final String COLUMN_GPS_SYNC_RETRY_COUNT = "GpsSyncRetryCount";
    private static final String COLUMN_GPS_MOBILE_DATA_ENABLED = "GpsMobileDataEnabled";
    private static final String COLUMN_GPS_SPEED = "GpsSpeed";
    private static final String COLUMN_GPS_BEARING = "GpsBearing";
    private static final String COLUMN_GPS_IS_MOCK_LOCATION = "IsMockLocation";
    private static final String COLUMN_GPS_MOTION_TYPE = "MotionType";
    private static final String COLUMN_GPS_DEVICE_IN_CHARGING = "DeviceInCharging";
    private static final String COLUMN_GPS_DEVICE_XYZ_STRING = "DeviceXYZString";
    private static final String COLUMN_GPS_DEVICE_TILT = "DeviceTilt";
    private static final int MOBILE_DATA_ENABLED = -1;
    private static final double SPEED = -1;
    private static final double BEARING = -1;
    private static final int IS_MOCK_LOCATION = -1;

    /**
     * Constructor: update table's name, build columns
     */
    public TableGpsPoints() {

        this.tableName = TABLE_GPS_POINTS;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER_PK));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_OFF_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_COMM_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_LAT, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_LONG, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_ALT, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_ACCURACY, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_SATELITES, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_PROVIDER_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_SYNC_RETRY_COUNT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_MOBILE_DATA_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_SPEED, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_BEARING, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_IS_MOCK_LOCATION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_MOTION_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_DEVICE_IN_CHARGING, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_DEVICE_TILT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_GPS_DEVICE_XYZ_STRING, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityGpsPoint recordGpsPoint = (EntityGpsPoint) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_GPS_OFF_ID, recordGpsPoint.offenderId);
        values.put(COLUMN_GPS_COMM_STAT, recordGpsPoint.commStatus);
        values.put(COLUMN_GPS_TIME, recordGpsPoint.time);
        values.put(COLUMN_GPS_LAT, recordGpsPoint.latitude);
        values.put(COLUMN_GPS_LONG, recordGpsPoint.longitude);
        values.put(COLUMN_GPS_ALT, recordGpsPoint.altitude);
        values.put(COLUMN_GPS_ACCURACY, recordGpsPoint.accuracy);
        values.put(COLUMN_GPS_SATELITES, recordGpsPoint.satellitesNumber);
        values.put(COLUMN_GPS_PROVIDER_TYPE, recordGpsPoint.providerType);
        values.put(COLUMN_GPS_SYNC_RETRY_COUNT, recordGpsPoint.pureMonitorSyncRetryCount);
        values.put(COLUMN_GPS_MOBILE_DATA_ENABLED, recordGpsPoint.mobileDataEnabled);
        values.put(COLUMN_GPS_SPEED, recordGpsPoint.speed);
        values.put(COLUMN_GPS_BEARING, recordGpsPoint.bearing);
        values.put(COLUMN_GPS_IS_MOCK_LOCATION, recordGpsPoint.isMockLocation);
        values.put(COLUMN_GPS_MOTION_TYPE, recordGpsPoint.motionType);
        values.put(COLUMN_GPS_DEVICE_IN_CHARGING, recordGpsPoint.inCharging);
        values.put(COLUMN_GPS_DEVICE_TILT, recordGpsPoint.tilt);
        values.put(COLUMN_GPS_DEVICE_XYZ_STRING, recordGpsPoint.xyzString);
        return database.insert(TABLE_GPS_POINTS, null, values);
    }

    @Override
    public void AddDefaultData(SQLiteDatabase db) {
    }

    private EntityGpsPoint GetGpsPointRecFromQueryCursor(Cursor QueryCursor) {
        return new EntityGpsPoint(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_OFF_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_COMM_STAT)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_GPS_TIME)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_LAT)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_LONG)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_ALT)),
                QueryCursor.getFloat(QueryCursor.getColumnIndex(COLUMN_GPS_ACCURACY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_SATELITES)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_PROVIDER_TYPE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_SYNC_RETRY_COUNT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_MOBILE_DATA_ENABLED)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_SPEED)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_BEARING)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_IS_MOCK_LOCATION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_MOTION_TYPE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_DEVICE_IN_CHARGING)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_DEVICE_TILT)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_GPS_DEVICE_XYZ_STRING))
        );
    }

    @Override
    protected DatabaseEntity GetRecordFromQueryCursor(Cursor QueryCursor) {

        return new EntityGpsPoint(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_OFF_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_COMM_STAT)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_GPS_TIME)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_LAT)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_LONG)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_ALT)),
                QueryCursor.getFloat(QueryCursor.getColumnIndex(COLUMN_GPS_ACCURACY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_SATELITES)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_PROVIDER_TYPE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_SYNC_RETRY_COUNT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_MOBILE_DATA_ENABLED)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_SPEED)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_GPS_BEARING)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_IS_MOCK_LOCATION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_MOTION_TYPE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_DEVICE_IN_CHARGING)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_GPS_DEVICE_TILT)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_GPS_DEVICE_XYZ_STRING))
        );
    }

    /**
     * Update the RecordSyncRetriesCount of the GpsPoint record.
     * Increasing the count till it reaches the allowed maximum, then the record will be deleted. (Default MAX_SYNC_RETRY_COUNT = 3)
     *
     * @param RowId The record RowId to update.
     */
    public void updateRecordSyncRetriesCount(long RowId) {
        ContentValues values = new ContentValues();
        EntityGpsPoint recordGpsPoint = (EntityGpsPoint) DatabaseAccess.getInstance().tableGpsPoint.getRecordByRowId(RowId);
        if (recordGpsPoint != null) {
            if (recordGpsPoint.pureMonitorSyncRetryCount < EntityGpsPoint.MAX_SYNC_RETRY_COUNT) {
                values.put(COLUMN_GPS_SYNC_RETRY_COUNT, recordGpsPoint.pureMonitorSyncRetryCount + 1);
                databaseReference.update(TABLE_GPS_POINTS, values, String.format("rowid = %d", recordGpsPoint.id), null);
            } else {
                deleteRowById(RowId);
            }
        }
    }

    @Override
    public DatabaseEntity GetRecord(SQLiteDatabase db) {

        // Select query
        Cursor QueryCursor = db.query(TABLE_GPS_POINTS, columnNamesArray,
                null, null, null, null, null);
        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }

        return GetGpsPointRecFromQueryCursor(QueryCursor);
    }


    public List<EntityGpsPoint> getGpsPointRecordsForUpload() {
        List<EntityGpsPoint> gpsPointRecordList = new ArrayList<EntityGpsPoint>();
        // Select query
        Cursor cursorGpsPointsForUpload = databaseReference.query(
                TABLE_GPS_POINTS,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                COLUMN_GPS_SYNC_RETRY_COUNT,            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.


        while (cursorGpsPointsForUpload.moveToNext()) {
            gpsPointRecordList.add((EntityGpsPoint) GetRecordFromQueryCursor(cursorGpsPointsForUpload));
        }
        cursorGpsPointsForUpload.close();

        return gpsPointRecordList;
    }

    public EntityGpsPoint getLastGpsRecByRowId(long rowId) {
        EntityGpsPoint recordGpsPoint = null;
        String Where = String.format("(%s = %d)", "rowid", rowId);
        Cursor QueryCursor = databaseReference.query(TABLE_GPS_POINTS,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        if (QueryCursor.getCount() > 0) {
            QueryCursor.moveToFirst();
            recordGpsPoint = GetGpsPointRecFromQueryCursor(QueryCursor);
        }
        QueryCursor.close();

        return recordGpsPoint;
    }

    public int getNewLocationsCount() {
        int Count;

        String Where = String.format("(%s = %d)", COLUMN_GPS_SYNC_RETRY_COUNT, 0);
        Cursor QueryCursor = databaseReference.query(TABLE_GPS_POINTS,        // Table name
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

    public int getFailedLocationsCount() {
        int Count;

        String Where = String.format("(%s > %d)", COLUMN_GPS_SYNC_RETRY_COUNT, 0);
        Cursor QueryCursor = databaseReference.query(TABLE_GPS_POINTS,        // Table name
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

    public List<EntityGpsPoint> getAllGpsPointRecords() {
        List<EntityGpsPoint> recordGpsPointList = new ArrayList<>();
        // Select query
        Cursor cursorGpsPointAll = databaseReference.query(
                TABLE_GPS_POINTS,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                "rowid",            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.


        while (cursorGpsPointAll.moveToNext()) {
            recordGpsPointList.add(GetGpsPointRecFromQueryCursor(cursorGpsPointAll));
        }
        cursorGpsPointAll.close();

        return recordGpsPointList;
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion >= 146 && oldVersion <= 166) {
            database.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + COLUMN_GPS_DEVICE_IN_CHARGING
                    + " INTEGER " + ";");
            database.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + COLUMN_GPS_DEVICE_TILT
                    + " INTEGER " + ";");
            database.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + COLUMN_GPS_DEVICE_XYZ_STRING
                    + " TEXT " + ";");
        }

    }
}
