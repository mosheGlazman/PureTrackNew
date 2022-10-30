package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityDeviceJamming;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;

public class TableDeviceJamming extends DatabaseTable{

    private static final String TABLE_DEVICE_JAMMING = "DeviceJamming";

    public static final String ENABLED = "enabled";
    public static final String MINIMUM_GOOD_CELLULAR_LEVEL_WCDMA_3G = "minimumGoodCellularLevelWcdma3G";
    public static final String MINIMUM_GOOD_CELLULAR_LEVEL_LTE_4G = "minimumGoodCellularLevelLte4G";
    public static final String JAMMING_EVENT_TIMER_SENSITIVITY = "jammingEventTimerSensitivity";
    public static final String CELLULAR_LEVEL_SAMPLE_INTERVAL = "cellularLevelSampleInterval";

    public TableDeviceJamming() {
        this.tableName = TABLE_DEVICE_JAMMING;

        this.AddColumn(new DatabaseColumn(ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MINIMUM_GOOD_CELLULAR_LEVEL_WCDMA_3G, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MINIMUM_GOOD_CELLULAR_LEVEL_LTE_4G, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(JAMMING_EVENT_TIMER_SENSITIVITY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(CELLULAR_LEVEL_SAMPLE_INTERVAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityDeviceJamming entityDeviceJamming = (EntityDeviceJamming) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(ENABLED, entityDeviceJamming.enabled);
        values.put(MINIMUM_GOOD_CELLULAR_LEVEL_WCDMA_3G, entityDeviceJamming.minimumGoodCellularLevelWcdma3G);
        values.put(MINIMUM_GOOD_CELLULAR_LEVEL_LTE_4G, entityDeviceJamming.minimumGoodCellularLevelLte4G);
        values.put(JAMMING_EVENT_TIMER_SENSITIVITY, entityDeviceJamming.jammingEventTimerSensitivity);
        values.put(CELLULAR_LEVEL_SAMPLE_INTERVAL, entityDeviceJamming.cellularLevelSampleInterval);
        return database.insert(TABLE_DEVICE_JAMMING, null, values);
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion == 186) {
            database.execSQL(" create table " + TABLE_DEVICE_JAMMING + " (" +
                    ENABLED + " integer, " +
                    MINIMUM_GOOD_CELLULAR_LEVEL_WCDMA_3G + " integer, " +
                    MINIMUM_GOOD_CELLULAR_LEVEL_LTE_4G + " integer, " +
                    JAMMING_EVENT_TIMER_SENSITIVITY + " integer, " +
                    CELLULAR_LEVEL_SAMPLE_INTERVAL + " integer );");
        }
    }

    public void insertRecord(EntityDeviceJamming entityDeviceJamming) {
        wipeTable();
        ContentValues values = new ContentValues();
        values.put(ENABLED, entityDeviceJamming.enabled);
        values.put(MINIMUM_GOOD_CELLULAR_LEVEL_WCDMA_3G, entityDeviceJamming.minimumGoodCellularLevelWcdma3G);
        values.put(MINIMUM_GOOD_CELLULAR_LEVEL_LTE_4G, entityDeviceJamming.minimumGoodCellularLevelLte4G);
        values.put(JAMMING_EVENT_TIMER_SENSITIVITY, entityDeviceJamming.jammingEventTimerSensitivity);
        values.put(CELLULAR_LEVEL_SAMPLE_INTERVAL, entityDeviceJamming.cellularLevelSampleInterval);
        databaseReference.insert(TABLE_DEVICE_JAMMING, null, values);
    }

    private void wipeTable() {
        databaseReference.delete(TABLE_DEVICE_JAMMING,
                "1",
                null);
    }

    public EntityDeviceJamming getDeviceJammingConfigEntity() {
        Cursor cursor = databaseReference.rawQuery("select * from DeviceJamming limit 1", null);
        EntityDeviceJamming entityDeviceJamming = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int enabled = cursor.getInt(cursor.getColumnIndex(ENABLED));
            int minimumGoodCellularLevelWcdma3G = cursor.getInt(cursor.getColumnIndex(MINIMUM_GOOD_CELLULAR_LEVEL_WCDMA_3G));
            int minimumGoodCellularLevelLte4G = cursor.getInt(cursor.getColumnIndex(MINIMUM_GOOD_CELLULAR_LEVEL_LTE_4G));
            int jammingEventTimerSensitivity = cursor.getInt(cursor.getColumnIndex(JAMMING_EVENT_TIMER_SENSITIVITY));
            int cellularLevelSampleInterval = cursor.getInt(cursor.getColumnIndex(CELLULAR_LEVEL_SAMPLE_INTERVAL));
            entityDeviceJamming = new EntityDeviceJamming(enabled, minimumGoodCellularLevelWcdma3G, minimumGoodCellularLevelLte4G,
                    jammingEventTimerSensitivity, cellularLevelSampleInterval);
            cursor.moveToNext();
        }
        cursor.close();
        return entityDeviceJamming;
    }


}
