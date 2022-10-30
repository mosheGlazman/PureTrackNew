package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoDetails;

import java.util.ArrayList;
import java.util.List;

public class TableDeviceInfoDetails extends DatabaseTable {
    public final static int MAX_LOG_PER_REQ = 50;
    //Table
    private static final String TABLE_DEVICE_INFO_DETAILS = "DeviceInfoDetails";

    // Columns names
    public static final String COLUMN_DEVICE_DETAILS_SW_VERSION = "sw_version";
    public static final String COLUMN_DEVICE_DETAILS_HW_PHONE_MODEL = "hw_version_phone_model";
    public static final String COLUMN_DEVICE_DETAILS_HW_COMPONENTS = "hw_components";
    public static final String COLUMN_DEVICE_DETAILS_IMEI = "imei";
    public static final String COLUMN_DEVICE_DETAILS_OS_VERSION = "os_version";
    public static final String COLUMN_DEVICE_DETAILS_DB_VERSION = "db_version";
    public static final String COLUMN_DEVICE_DETAILS_BATTERY_TYPE = "battery_type";

    public TableDeviceInfoDetails() {

        this.tableName = TABLE_DEVICE_INFO_DETAILS;
        this.columnNumber = 0;
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_DETAILS_SW_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_DETAILS_HW_PHONE_MODEL, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_DETAILS_HW_COMPONENTS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_DETAILS_IMEI, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_DETAILS_OS_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_DETAILS_DB_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_DETAILS_BATTERY_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        BuildColumnNameArray();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityDeviceInfoDetails recordDeviceInfoDetails = (EntityDeviceInfoDetails) databaseEntity;

        ContentValues values = new ContentValues();

        values.put(COLUMN_DEVICE_DETAILS_SW_VERSION, recordDeviceInfoDetails.sw_version);
        values.put(COLUMN_DEVICE_DETAILS_HW_PHONE_MODEL, recordDeviceInfoDetails.hw_version_phone_model);
        values.put(COLUMN_DEVICE_DETAILS_HW_COMPONENTS, recordDeviceInfoDetails.hw_components);
        values.put(COLUMN_DEVICE_DETAILS_IMEI, recordDeviceInfoDetails.imei);
        values.put(COLUMN_DEVICE_DETAILS_OS_VERSION, recordDeviceInfoDetails.os_version);
        values.put(COLUMN_DEVICE_DETAILS_DB_VERSION, recordDeviceInfoDetails.db_version);
        values.put(COLUMN_DEVICE_DETAILS_BATTERY_TYPE, recordDeviceInfoDetails.battery_type);
        return database.insert(TABLE_DEVICE_INFO_DETAILS, null, values);
    }

    @Override
    protected EntityDeviceInfoDetails GetRecordFromQueryCursor(Cursor QueryCursor) {
        return new EntityDeviceInfoDetails(
                //	QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEVICE_INFO_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_DETAILS_SW_VERSION)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_DETAILS_HW_PHONE_MODEL)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_DETAILS_HW_COMPONENTS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_DETAILS_IMEI)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_DETAILS_OS_VERSION)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_DETAILS_DB_VERSION)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_DETAILS_BATTERY_TYPE))
        );
    }

    public EntityDeviceInfoDetails getDeviceInfoDetailsRecordForUpload() {
        List<EntityDeviceInfoDetails> recordDebugInfoDetailsList = new ArrayList<EntityDeviceInfoDetails>();
        // Select query
        Cursor cursordebugInfoStatusForUpload = databaseReference.query(
                TABLE_DEVICE_INFO_DETAILS,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                String.valueOf(MAX_LOG_PER_REQ));    // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.

        while (cursordebugInfoStatusForUpload.moveToNext()) {
            recordDebugInfoDetailsList.add(GetRecordFromQueryCursor(cursordebugInfoStatusForUpload));
        }
        cursordebugInfoStatusForUpload.close();
        return recordDebugInfoDetailsList.get(0);
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

    }
}
