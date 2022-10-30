package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.util.constants.database_defaults.DefaultDatabaseData;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityDeviceDetails;


public class TableDeviceDetails extends DatabaseTable {
    // Table name
    private static final String TABLE_DEV_DETAILS = "DeviceDetails";
    // Column names
    private static final String COLUMN_DEV_ID = "DevId";
    public static final String COLUMN_DEV_SN = "DevSn";
    private static final String COLUMN_DEV_COMM_KEY = "DevCommKey";
    private static final String COLUMN_DEV_SW_VERSION = "DevSwVer";
    private static final String COLUMN_DEV_FW_VERSION = "DevFwVer";

    public static final String COLUMN_DEBUG_INFO_RECEIVED_DATA_USAGE = "DevReceivedDataUsage";
    public static final String COLUMN_DEBUG_INFO_SENT_DATA_USAGE = "DevSentDataUsage";
    // Record for quick access
    private EntityDeviceDetails DevDetailsRec;

    /**
     * Constructor: update table's name, build columns
     */
    public TableDeviceDetails() {

        this.tableName = TABLE_DEV_DETAILS;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_DEV_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEV_SN, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEV_COMM_KEY, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEV_SW_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_DEV_FW_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_RECEIVED_DATA_USAGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEBUG_INFO_SENT_DATA_USAGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     *  Add offender details Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityDeviceDetails DevRec = (EntityDeviceDetails) databaseEntity;

        ContentValues values = new ContentValues();

        values.put(COLUMN_DEV_ID, DevRec.DeviceId);
        values.put(COLUMN_DEV_SN, DevRec.getDeviceSerialNumber());
        values.put(COLUMN_DEV_SW_VERSION, DevRec.SwVersion);
        values.put(COLUMN_DEV_FW_VERSION, DevRec.RfFwVersion);
        values.put(COLUMN_DEBUG_INFO_RECEIVED_DATA_USAGE, DevRec.receivedUsageDataOfApplication);
        values.put(COLUMN_DEBUG_INFO_SENT_DATA_USAGE, DevRec.sentDataUsageOfApplication);

        // Update local copy
        DevDetailsRec = DevRec;
        // Insert the record
        return database.insert(TABLE_DEV_DETAILS, null, values);
    }

    /**
     * Add default data record/s
     */
    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        // 	default data record
        addRecord(db, new EntityDeviceDetails(DefaultDatabaseData.DeviceId,
                DefaultDatabaseData.DeviceSerialNumber,
                DefaultDatabaseData.DeviceCommKey,
                DefaultDatabaseData.DeviceSwVer,
                DefaultDatabaseData.DeviceFwVer,
                0,
                0));
    }

    private EntityDeviceDetails GetRecFromQueryCursor(Cursor QueryCursor) {
        EntityDeviceDetails DevRec = new EntityDeviceDetails(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEV_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEV_SN)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEV_COMM_KEY)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEV_SW_VERSION)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_DEV_FW_VERSION)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_RECEIVED_DATA_USAGE)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_DEBUG_INFO_SENT_DATA_USAGE)));

        DevRec.SetRowId(QueryCursor.getLong(QueryCursor.getColumnIndex("rowid")));

        return DevRec;
    }

    public EntityDeviceDetails GetRecord(SQLiteDatabase db) {
        // Select query
        Cursor QueryCursor = db.query(TABLE_DEV_DETAILS, columnNamesArray, null, null, null, null, null);
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
        DevDetailsRec = this.GetRecord(db);
    }

    public void Update(EntityDeviceDetails Rec) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEV_ID, Rec.DeviceId);
        values.put(COLUMN_DEV_SN, Rec.getDeviceSerialNumber());
        values.put(COLUMN_DEV_COMM_KEY, Rec.CommunicationKey);
        values.put(COLUMN_DEV_SW_VERSION, Rec.SwVersion);
        values.put(COLUMN_DEV_FW_VERSION, Rec.RfFwVersion);

        databaseReference.update(TABLE_DEV_DETAILS,
                values,
                null,
                null);
        // Load local copy
        LoadData(databaseReference);
    }

    public EntityDeviceDetails getDeviceDetailsRecord() {
        return DevDetailsRec;
    }

    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

    }


}

