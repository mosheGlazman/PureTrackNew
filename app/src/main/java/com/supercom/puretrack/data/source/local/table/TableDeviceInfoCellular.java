package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoCellular;

import java.util.ArrayList;
import java.util.List;

public class TableDeviceInfoCellular extends DatabaseTable {
    public final static int MAX_LOG_PER_REQ = 50;

    //Table
    private static final String TABLE_DEVICE_INFO_CELLULAR = "DeviceInfoCellular";

    // Columns names
    public static final String COLUMN_DEVICE_CELLULAR_UTC_TIME = "cell_utc_time";
    public static final String COLUMN_DEVICE_CELLULAR_REGISTRATION_TYPE = "registration_type";
    public static final String COLUMN_DEVICE_CELLULAR_NETWORK_ID = "network_id";
    public static final String COLUMN_DEVICE_CELLULAR_CELL_RECEPTION = "cell_reception";
    public static final String COLUMN_DEVICE_CELLULAR_CELL_MOBILE_DATA = "cell_mobile_data";
    public static final String COLUMN_DEVICE_CELLULAR_SIM_ID = "sim_id";
    public static final String COLUMN_DEVICE_CELLULAR_DEVICE_PHONE_NUMBER = "device_phone_number";

    public TableDeviceInfoCellular() {

        this.tableName = TABLE_DEVICE_INFO_CELLULAR;
        this.columnNumber = 0;
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_CELLULAR_UTC_TIME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_CELLULAR_REGISTRATION_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_CELLULAR_NETWORK_ID, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_CELLULAR_CELL_RECEPTION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_CELLULAR_CELL_MOBILE_DATA, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_CELLULAR_SIM_ID, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_CELLULAR_DEVICE_PHONE_NUMBER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        BuildColumnNameArray();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityDeviceInfoCellular recordDeviceInfoStatus = (EntityDeviceInfoCellular) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_CELLULAR_UTC_TIME, recordDeviceInfoStatus.cell_utc_time);
        values.put(COLUMN_DEVICE_CELLULAR_REGISTRATION_TYPE, recordDeviceInfoStatus.registration_type);
        values.put(COLUMN_DEVICE_CELLULAR_NETWORK_ID, recordDeviceInfoStatus.network_id);
        values.put(COLUMN_DEVICE_CELLULAR_CELL_RECEPTION, recordDeviceInfoStatus.cell_reception);
        values.put(COLUMN_DEVICE_CELLULAR_CELL_MOBILE_DATA, recordDeviceInfoStatus.cell_mobile_data);
        values.put(COLUMN_DEVICE_CELLULAR_SIM_ID, recordDeviceInfoStatus.sim_id);
        values.put(COLUMN_DEVICE_CELLULAR_DEVICE_PHONE_NUMBER, recordDeviceInfoStatus.device_phone_number);

        return database.insert(TABLE_DEVICE_INFO_CELLULAR, null, values);
    }

    @Override
    protected EntityDeviceInfoCellular GetRecordFromQueryCursor(Cursor QueryCursor) {
        return new EntityDeviceInfoCellular(
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_CELLULAR_UTC_TIME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_CELLULAR_REGISTRATION_TYPE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_CELLULAR_NETWORK_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_CELLULAR_CELL_RECEPTION)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_CELLULAR_CELL_MOBILE_DATA)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_CELLULAR_SIM_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEVICE_CELLULAR_DEVICE_PHONE_NUMBER))
        );
    }

    public List<EntityDeviceInfoCellular> getDeviceInfoCellularRecordsForUpload() {
        List<EntityDeviceInfoCellular> recordDebugInfoCellularList = new ArrayList<EntityDeviceInfoCellular>();

        // Select query
        Cursor cursordebugInfoCellularForUpload = databaseReference.query(
                TABLE_DEVICE_INFO_CELLULAR,        // Table name
                columnNamesArray,        // Columns names
                null,                // Where ... null - returns all rows
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                String.valueOf(MAX_LOG_PER_REQ));    // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.

        while (cursordebugInfoCellularForUpload.moveToNext()) {
            recordDebugInfoCellularList.add(GetRecordFromQueryCursor(cursordebugInfoCellularForUpload));
        }
        cursordebugInfoCellularForUpload.close();
        return recordDebugInfoCellularList;
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

    }
}
