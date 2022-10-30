package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityDeviceShielding;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;

public class TableDeviceShielding extends DatabaseTable {

    private static final String TABLE_DEVICE_SHIELDING = "DeviceShielding";

    public static final String ENABLED = "Enabled";
    public static final String OPEN_EVENT_THRESHOLD = "OpenEventThreshold";
    public static final String OPEN_EVENT_CELL_ENABLED = "OpenEventCellEnabled";
    public static final String OPEN_EVENT_BLUETOOTH_ENABLED = "OpenEventBluetoothEnabled";
    public static final String OPEN_EVENT_WIFI_ENABLED = "OpenEventWifiEnabled";
    public static final String CLOSE_EVENT_THRESHOLD = "CloseEventThreshold";
    public static final String CLOSE_EVENT_CELL_ENABLED = "CloseEventCellEnabled";
    public static final String CLOSE_EVENT_BLUETOOTH_ENABLED = "CloseEventBluetoothEnabled";
    public static final String CLOSE_EVENT_WIFI_ENABLED = "CloseEventWifiEnabled";
    public static final String CHECK_INTERVAL = "CheckInterval";
    public static final String BLE_THRESHOLD_SEC = "BleThresholdSec";
    public static final String WIFI_THRESHOLD_SEC = "WifiThresholdSec";
    public static final String MOBILE_NETWORK_THRESHOLD_SEC = "MobileNetworkThresholdSec";
    private static final TableDeviceShielding INSTANCE = new TableDeviceShielding();

    public EntityDeviceShielding getDeviceShieldingEntity() {
        Cursor cursor = databaseReference.rawQuery("select * from " + tableName + " limit 1", null);
        cursor.moveToFirst();
        EntityDeviceShielding entityDeviceShielding = null;
        while(!cursor.isAfterLast()) {
            int enabled = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ENABLED)));
            int openEventThreshold = Integer.parseInt(cursor.getString(cursor.getColumnIndex(OPEN_EVENT_THRESHOLD)));
            int openEventCellEnabled = Integer.parseInt(cursor.getString(cursor.getColumnIndex(OPEN_EVENT_CELL_ENABLED)));
            int openEventBluetoothEnabled = Integer.parseInt(cursor.getString(cursor.getColumnIndex(OPEN_EVENT_BLUETOOTH_ENABLED)));
            int openEventsWifiEnabled = Integer.parseInt(cursor.getString(cursor.getColumnIndex(OPEN_EVENT_WIFI_ENABLED)));
            int closeEventThreshold = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CLOSE_EVENT_THRESHOLD)));
            int closeEventCellEnabled = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CLOSE_EVENT_CELL_ENABLED)));
            int closeEventBluetoothEnabled = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CLOSE_EVENT_BLUETOOTH_ENABLED)));
            int closeEventWifiEnabled = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CLOSE_EVENT_WIFI_ENABLED)));
            int checkIntervalSec = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CHECK_INTERVAL)));
            int bleThresholdSec = Integer.parseInt(cursor.getString(cursor.getColumnIndex(BLE_THRESHOLD_SEC)));
            int wifiThresholdSec = Integer.parseInt(cursor.getString(cursor.getColumnIndex(WIFI_THRESHOLD_SEC)));
            int mobileNetworkThresholdSec = Integer.parseInt(cursor.getString(cursor.getColumnIndex(MOBILE_NETWORK_THRESHOLD_SEC)));

            entityDeviceShielding = new EntityDeviceShielding(enabled, openEventThreshold, openEventCellEnabled, openEventBluetoothEnabled,
                    openEventsWifiEnabled, closeEventThreshold, closeEventCellEnabled, closeEventBluetoothEnabled, closeEventWifiEnabled, checkIntervalSec, bleThresholdSec, wifiThresholdSec, mobileNetworkThresholdSec);
            cursor.moveToNext();
        }
        cursor.close();
        return entityDeviceShielding;
    }


    public TableDeviceShielding() {
        this.tableName = TABLE_DEVICE_SHIELDING;
        this.AddColumn(new DatabaseColumn(ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OPEN_EVENT_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OPEN_EVENT_CELL_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OPEN_EVENT_BLUETOOTH_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OPEN_EVENT_WIFI_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(CLOSE_EVENT_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(CLOSE_EVENT_CELL_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(CLOSE_EVENT_BLUETOOTH_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(CLOSE_EVENT_WIFI_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(CHECK_INTERVAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(BLE_THRESHOLD_SEC, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(WIFI_THRESHOLD_SEC, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MOBILE_NETWORK_THRESHOLD_SEC, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
    }



    @Override
    public int GetColumnNum() {
        return super.GetColumnNum();
    }


    public EntityDeviceShielding GetRecord(SQLiteDatabase db) {
        // Select query
        Cursor QueryCursor = db.query(TABLE_DEVICE_SHIELDING, columnNamesArray,
                null, null, null, null, null);

        EntityDeviceShielding recordDeviceShielding;

        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
            recordDeviceShielding = GetRecFromQueryCursor(QueryCursor);
        }

        QueryCursor.close();

        return recordDeviceShielding;
    }


    private EntityDeviceShielding GetRecFromQueryCursor(Cursor queryCursor) {
        return new EntityDeviceShielding(
                queryCursor.getInt(queryCursor.getColumnIndex(ENABLED)),
                queryCursor.getInt(queryCursor.getColumnIndex(OPEN_EVENT_THRESHOLD)),
                queryCursor.getInt(queryCursor.getColumnIndex(OPEN_EVENT_CELL_ENABLED)),
                queryCursor.getInt(queryCursor.getColumnIndex(OPEN_EVENT_BLUETOOTH_ENABLED)),
                queryCursor.getInt(queryCursor.getColumnIndex(OPEN_EVENT_WIFI_ENABLED)),
                queryCursor.getInt(queryCursor.getColumnIndex(CLOSE_EVENT_THRESHOLD)),
                queryCursor.getInt(queryCursor.getColumnIndex(CLOSE_EVENT_CELL_ENABLED)),
                queryCursor.getInt(queryCursor.getColumnIndex(CLOSE_EVENT_BLUETOOTH_ENABLED)),
                queryCursor.getInt(queryCursor.getColumnIndex(CLOSE_EVENT_WIFI_ENABLED)),
                queryCursor.getInt(queryCursor.getColumnIndex(CHECK_INTERVAL)),
                queryCursor.getInt(queryCursor.getColumnIndex(BLE_THRESHOLD_SEC)),
                queryCursor.getInt(queryCursor.getColumnIndex(WIFI_THRESHOLD_SEC)),
                queryCursor.getInt(queryCursor.getColumnIndex(MOBILE_NETWORK_THRESHOLD_SEC))
        );
    }

    /**
     * We multiply all duration values by 1000 because we get them as seconds and want
     * to use them as milliseconds.
     */
    public void insertRecord(EntityDeviceShielding recordToAdd) {
        databaseReference.delete(TABLE_DEVICE_SHIELDING, null, null);
        ContentValues values = new ContentValues();
        values.put(ENABLED, recordToAdd.enabled);
        values.put(OPEN_EVENT_THRESHOLD, recordToAdd.openEventThreshold);
        values.put(OPEN_EVENT_CELL_ENABLED , recordToAdd.openEventCellEnabled);
        values.put(OPEN_EVENT_BLUETOOTH_ENABLED , recordToAdd.openEventBluetoothEnabled);
        values.put(OPEN_EVENT_WIFI_ENABLED , recordToAdd.openEventWifiEnabled);
        values.put(CLOSE_EVENT_THRESHOLD , recordToAdd.closeEventThreshold);
        values.put(CLOSE_EVENT_CELL_ENABLED , recordToAdd.closeEventCellEnabled);
        values.put(CLOSE_EVENT_BLUETOOTH_ENABLED , recordToAdd.closeEventBluetoothEnabled);
        values.put(CLOSE_EVENT_WIFI_ENABLED , recordToAdd.closeEventWifiEnabled);
        values.put(CHECK_INTERVAL , recordToAdd.checkIntervalSec);
        values.put(BLE_THRESHOLD_SEC , recordToAdd.bleThresholdSec);
        values.put(WIFI_THRESHOLD_SEC , recordToAdd.wifiThresholdSec);
        values.put(MOBILE_NETWORK_THRESHOLD_SEC , recordToAdd.mobileNetworkThresholdSec);

        Log.d("insertValue", "insertValue - " + databaseReference.insert(TABLE_DEVICE_SHIELDING, null, values));
    }


    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityDeviceShielding recordDeviceShielding = (EntityDeviceShielding) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(ENABLED, recordDeviceShielding.enabled);
        values.put(OPEN_EVENT_THRESHOLD, recordDeviceShielding.openEventThreshold);
        values.put(OPEN_EVENT_CELL_ENABLED , recordDeviceShielding.openEventCellEnabled);
        values.put(OPEN_EVENT_BLUETOOTH_ENABLED , recordDeviceShielding.openEventBluetoothEnabled);
        values.put(OPEN_EVENT_WIFI_ENABLED , recordDeviceShielding.openEventWifiEnabled);
        values.put(CLOSE_EVENT_THRESHOLD , recordDeviceShielding.closeEventThreshold);
        values.put(CLOSE_EVENT_CELL_ENABLED , recordDeviceShielding.closeEventCellEnabled);
        values.put(CLOSE_EVENT_BLUETOOTH_ENABLED , recordDeviceShielding.closeEventBluetoothEnabled);
        values.put(CLOSE_EVENT_WIFI_ENABLED , recordDeviceShielding.closeEventWifiEnabled);
        values.put(CHECK_INTERVAL , recordDeviceShielding.checkIntervalSec);
        values.put(BLE_THRESHOLD_SEC , recordDeviceShielding.bleThresholdSec);
        values.put(WIFI_THRESHOLD_SEC , recordDeviceShielding.wifiThresholdSec);
        values.put(MOBILE_NETWORK_THRESHOLD_SEC , recordDeviceShielding.mobileNetworkThresholdSec);
        return database.insert(TABLE_DEVICE_SHIELDING, null, values);
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion >= 152 && oldVersion <= 168) {
            database.execSQL(" CREATE TABLE " + TABLE_DEVICE_SHIELDING + " (" +
                    ENABLED + " INTEGER, " +
                    "CellReceptionSampleInterval" + " INTEGER, " +
                    "MinimumGoodCellularLevelWcdma3G" + " INTEGER, " +
                    "MinimumGoodCellularLevelLte4G" + " INTEGER, " +
                    "IdleToTriggeredToIdleCellularLevelSensitivity" + " INTEGER, " +
                    "TriggeredGoodLevelPercentage" + " INTEGER, " +
                    "CellularReceptionDropThreshold" + " INTEGER, " +
                    "CellularReceptionDropInterval" + " INTEGER, " +
                    "ShieldEventTimerSensitivity" + " INTEGER, " +
                    "StopShieldMinimumReceptionLevelPercentage" + " INTEGER, " +
                    "StopShieldMinimumReceptionLevelDuration" + " INTEGER, " +
                    "StopShieldMinimumReceptionLevelWcdma3G" + " INTEGER, " +
                    "StopShieldMinimumReceptionLevelLte4G" + " INTEGER, " +
                    "StopShieldOpenEventConditionNoBluetooth" + " INTEGER, " +
                    "StopShieldOpenEventConditionNoLocation" + " INTEGER, " +
                    "StopShieldOpenEventConditionNoLight" + " INTEGER, " +
                    "StopShieldCloseEventConditionNoBluetooth" + " INTEGER, " +
                    "StopShieldCloseEventConditionNoLocation" + " INTEGER, " +
                    "StopShieldCloseEventConditionNoLight" + " INTEGER);");
        }
        switch (oldVersion) {
            case 200:
                database.execSQL("drop table " + TABLE_DEVICE_SHIELDING);
                database.execSQL(" create table " + TABLE_DEVICE_SHIELDING + " (" +
                        ENABLED + " integer, " +
                        OPEN_EVENT_THRESHOLD  + " integer, " +
                        OPEN_EVENT_CELL_ENABLED  + " integer, " +
                        OPEN_EVENT_BLUETOOTH_ENABLED  + " integer, " +
                        OPEN_EVENT_WIFI_ENABLED  + " integer, " +
                        CLOSE_EVENT_THRESHOLD  + " integer, " +
                        CLOSE_EVENT_CELL_ENABLED  + " integer, " +
                        CLOSE_EVENT_BLUETOOTH_ENABLED  + " integer, " +
                        CLOSE_EVENT_WIFI_ENABLED + " integer, " +
                        CHECK_INTERVAL + " integer, " +
                        BLE_THRESHOLD_SEC + " integer, " +
                        WIFI_THRESHOLD_SEC + " integer, " +
                        MOBILE_NETWORK_THRESHOLD_SEC  + " integer);");
                break;
        }
    }

    public static TableDeviceShielding sharedInstance() {
        return INSTANCE;
    }


}
