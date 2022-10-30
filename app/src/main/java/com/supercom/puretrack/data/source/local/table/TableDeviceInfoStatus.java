package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoStatus;

import java.util.ArrayList;
import java.util.List;

public class TableDeviceInfoStatus extends DatabaseTable {
    public final static int MAX_LOG_PER_REQ = 50;

    //Table
    private static final String TABLE_DEVICE_INFO_STATUS = "DeviceInfoStatus";

    // Columns names
    public static final String COLUMN_DEVICE_STATUS_UTC_TIME = "status_utc_time";
    public static final String COLUMN_DEVICE_STATUS_OPERATIONAL_MODE = "operational_mode";
    public static final String COLUMN_DEVICE_STATUS_BATTERY_LEVEL = "battery_level";
    public static final String COLUMN_DEVICE_STATUS_TEMPERATURE = "temperature";
    public static final String COLUMN_DEVICE_STATUS_TAG_LAST_PING = "tag_last_ping";
    public static final String COLUMN_DEVICE_STATUS_TAG_BATTERY = "tag_battery";
    public static final String COLUMN_DEVICE_STATUS_IS_BEACON_BATTERY_TAMPER = "is_beacon_battery_tamper";
    public static final String COLUMN_DEVICE_STATUS_IS_TAG_BATTERY_TAMPER = "is_Tag_battery_tamper";
    public static final String COLUMN_DEVICE_STATUS_BEACON_LAST_PING = "beacon_last_ping";
    public static final String COLUMN_DEVICE_STATUS_BEACON_BATTERY = "beacon_battery";
    public static final String COLUMN_DEVICE_STATUS_OFFENDER_IN_RANGE = "offender_in_range";
    public static final String COLUMN_DEVICE_STATUS_KNOX_ACTIVATED = "knox_activated";
    public static final String COLUMN_DEVICE_STATUS_KIOSK_MODE_ENABLED = "kiosk_mode_enabled";
    public static final String COLUMN_DEVICE_STATUS_EVENT_UPLOAD_STATUS = "events_upload_status";
    public static final String COLUMN_DEVICE_STATUS_LOCATION_UPLOAD_STATUS = "locations_upload_status";


    public TableDeviceInfoStatus() {

        this.tableName = TABLE_DEVICE_INFO_STATUS;
        this.columnNumber = 0;
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_UTC_TIME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_OPERATIONAL_MODE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_BATTERY_LEVEL, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_TEMPERATURE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_TAG_LAST_PING, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_TAG_BATTERY, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_IS_BEACON_BATTERY_TAMPER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_IS_TAG_BATTERY_TAMPER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_BEACON_LAST_PING, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_BEACON_BATTERY, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_OFFENDER_IN_RANGE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_KNOX_ACTIVATED, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_KIOSK_MODE_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_EVENT_UPLOAD_STATUS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_LOCATION_UPLOAD_STATUS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));


        BuildColumnNameArray();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityDeviceInfoStatus recordDeviceInfoStatus = (EntityDeviceInfoStatus) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_STATUS_UTC_TIME, recordDeviceInfoStatus.status_utc_time);
        values.put(COLUMN_DEVICE_STATUS_OPERATIONAL_MODE, recordDeviceInfoStatus.operational_mode);
        values.put(COLUMN_DEVICE_STATUS_BATTERY_LEVEL, recordDeviceInfoStatus.battery_level);
        values.put(COLUMN_DEVICE_STATUS_TEMPERATURE, recordDeviceInfoStatus.temperature);
        values.put(COLUMN_DEVICE_STATUS_TAG_LAST_PING, recordDeviceInfoStatus.tag_last_ping);
        values.put(COLUMN_DEVICE_STATUS_TAG_BATTERY, recordDeviceInfoStatus.tag_battery);
        values.put(COLUMN_DEVICE_STATUS_IS_BEACON_BATTERY_TAMPER, recordDeviceInfoStatus.is_beacon_battery_tamper);
        values.put(COLUMN_DEVICE_STATUS_IS_TAG_BATTERY_TAMPER, recordDeviceInfoStatus.is_Tag_battery_tamper);
        values.put(COLUMN_DEVICE_STATUS_BEACON_LAST_PING, recordDeviceInfoStatus.beacon_last_ping);
        values.put(COLUMN_DEVICE_STATUS_BEACON_BATTERY, recordDeviceInfoStatus.beacon_battery);
        values.put(COLUMN_DEVICE_STATUS_OFFENDER_IN_RANGE, recordDeviceInfoStatus.offender_in_range);
        values.put(COLUMN_DEVICE_STATUS_KNOX_ACTIVATED, recordDeviceInfoStatus.knox_activated);
        values.put(COLUMN_DEVICE_STATUS_KIOSK_MODE_ENABLED, recordDeviceInfoStatus.kiosk_mode_enabled);
        values.put(COLUMN_DEVICE_STATUS_EVENT_UPLOAD_STATUS, recordDeviceInfoStatus.event_upload_status);
        values.put(COLUMN_DEVICE_STATUS_LOCATION_UPLOAD_STATUS, recordDeviceInfoStatus.location_upload_status);


        return database.insert(TABLE_DEVICE_INFO_STATUS, null, values);
    }

    @Override
    protected EntityDeviceInfoStatus GetRecordFromQueryCursor(Cursor QueryCursor) {
        return new EntityDeviceInfoStatus(
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_UTC_TIME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_OPERATIONAL_MODE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_BATTERY_LEVEL)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_TEMPERATURE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_TAG_LAST_PING)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_TAG_BATTERY)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_IS_TAG_BATTERY_TAMPER)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_BEACON_LAST_PING)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_BEACON_BATTERY)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_IS_BEACON_BATTERY_TAMPER)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_OFFENDER_IN_RANGE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_KNOX_ACTIVATED)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_KIOSK_MODE_ENABLED)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_EVENT_UPLOAD_STATUS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_LOCATION_UPLOAD_STATUS))
        );
    }


    public List<EntityDeviceInfoStatus> getDeviceInfoStatusRecordsForUpload() {

        List<EntityDeviceInfoStatus> recordDebugInfoStatusList = new ArrayList<EntityDeviceInfoStatus>();

        // Select query
        Cursor cursordebugInfoStatusForUpload = databaseReference.query(
                TABLE_DEVICE_INFO_STATUS,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                String.valueOf(MAX_LOG_PER_REQ));    // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.

        while (cursordebugInfoStatusForUpload.moveToNext()) {
            recordDebugInfoStatusList.add(GetRecordFromQueryCursor(cursordebugInfoStatusForUpload));
        }
        cursordebugInfoStatusForUpload.close();
        return recordDebugInfoStatusList;
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

    }
}
