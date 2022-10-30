package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityScannerType;
import com.supercom.puretrack.model.database.entities.EntitySelfDiagnosticEvent;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;

public class TableSelfDiagnosticEvents extends DatabaseTable {

    public static final String TABLE_SELF_DIAGNOSTIC_EVENTS = "SelfDiagnosticEvents";


    public static final String ENABLED = "enabled";
    //Time sensitivity for self diagnostic events is in HOURS and not SECONDS.
    public static final String GYROSCOPE_SENSITIVITY = "GyroscopeSensitivity";
    public static final String MAGNETIC_SENSITIVITY = "MagneticSensitivity";

    public TableSelfDiagnosticEvents() {
        this.tableName = TABLE_SELF_DIAGNOSTIC_EVENTS;

        this.AddColumn(new DatabaseColumn(ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(GYROSCOPE_SENSITIVITY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MAGNETIC_SENSITIVITY, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
    }


    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntitySelfDiagnosticEvent entitySelfDiagnosticEvent = (EntitySelfDiagnosticEvent) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(ENABLED, entitySelfDiagnosticEvent.enabled);
        values.put(GYROSCOPE_SENSITIVITY, entitySelfDiagnosticEvent.gyroscopeSensitivity);
        values.put(MAGNETIC_SENSITIVITY, entitySelfDiagnosticEvent.magneticSensitivity);
        return database.insert(TABLE_SELF_DIAGNOSTIC_EVENTS, null, values);
    }

    public void insertRecord(EntitySelfDiagnosticEvent entitySelfDiagnosticEvent) {
        databaseReference.delete(TABLE_SELF_DIAGNOSTIC_EVENTS, "1", null);
        ContentValues values = new ContentValues();
        values.put(ENABLED, entitySelfDiagnosticEvent.enabled);
        values.put(GYROSCOPE_SENSITIVITY, entitySelfDiagnosticEvent.gyroscopeSensitivity);
        values.put(MAGNETIC_SENSITIVITY, entitySelfDiagnosticEvent.magneticSensitivity);
        databaseReference.insert(TABLE_SELF_DIAGNOSTIC_EVENTS, null, values);
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion == 188) {
            database.execSQL(" create table " + TABLE_SELF_DIAGNOSTIC_EVENTS + " (" +
                    ENABLED + " integer, " +
                    GYROSCOPE_SENSITIVITY + " integer, " +
                    MAGNETIC_SENSITIVITY + " integer );");
        }
    }
}
