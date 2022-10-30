package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.EntityScannerType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;

public class TableScannerType extends DatabaseTable {

    private static final String TABLE_SCANNER_TYPE = "ScannerType";

    public static final String NORMAL_SCAN_ENABLED = "NormalScanEnabled";
    public static final String MAC_SCAN_ENABLED = "MacScanEnabled";
    public static final String MANUFACTURER_ID = "ManufacturerId";
    public static final String TAG_MAC_ADDRESS = "TagMacAddress";
    public static final String BEACON_MAC_ADDRESS = "BeaconMacAddress";

    public TableScannerType() {
        this.tableName = TABLE_SCANNER_TYPE;

        this.AddColumn(new DatabaseColumn(NORMAL_SCAN_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MAC_SCAN_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MANUFACTURER_ID, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(TAG_MAC_ADDRESS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(BEACON_MAC_ADDRESS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityScannerType entityScannerType = (EntityScannerType) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(NORMAL_SCAN_ENABLED, entityScannerType.normalScanEnabled);
        values.put(MAC_SCAN_ENABLED, entityScannerType.macScanEnabled);
        values.put(MANUFACTURER_ID, entityScannerType.manufacturerId);
        values.put(TAG_MAC_ADDRESS, entityScannerType.tagMacAddress);
        values.put(BEACON_MAC_ADDRESS, entityScannerType.beaconMacAddress);
        return database.insert(TABLE_SCANNER_TYPE, null, values);
    }


    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        switch (oldVersion) {
            case 184:
                database.execSQL(" create table " + TABLE_SCANNER_TYPE + " (" +
                        NORMAL_SCAN_ENABLED + " integer, " +
                        MAC_SCAN_ENABLED + " integer, " +
                        MANUFACTURER_ID + " text );");
                break;
            case 185:
                database.execSQL("alter table " + tableName + " add column " + TAG_MAC_ADDRESS
                        + " text " + ";");
                break;
            case 189:
                database.execSQL("alter table " + tableName + " add column " + BEACON_MAC_ADDRESS
                        + " text " + ";");
                break;
        }
    }


    public void insertRecord(EntityScannerType entityScannerType) {
        wipeTable();
        ContentValues values = new ContentValues();
        values.put(NORMAL_SCAN_ENABLED, entityScannerType.normalScanEnabled);
        values.put(MAC_SCAN_ENABLED, entityScannerType.macScanEnabled);
        values.put(MANUFACTURER_ID, entityScannerType.manufacturerId);
        values.put(TAG_MAC_ADDRESS, entityScannerType.tagMacAddress);
        values.put(BEACON_MAC_ADDRESS, entityScannerType.beaconMacAddress);
        databaseReference.insert(TABLE_SCANNER_TYPE, null, values);
    }

    private void wipeTable() {
        databaseReference.delete(TABLE_SCANNER_TYPE,
                "1",
                null);
    }

}
