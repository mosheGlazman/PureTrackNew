package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityCaseTamper;
import com.supercom.puretrack.model.database.entities.EntityCommServers;
import com.supercom.puretrack.model.database.entities.EntityDeviceJamming;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.util.constants.database_defaults.DefaultDatabaseData;
import com.supercom.puretrack.util.constants.database_defaults.DefaultTableCaseTamper;

public class TableCaseTamper extends DatabaseTable {

    public static final String TABLE_CASE_TAMPER = "CaseTamper";

    public static final String ENABLED = "enabled";
    public static final String CASE_CLOSED_THRESHOLD = "caseClosedThreshold";
    public static final String MAGNETIC_RECALIBRATION_ON_RESTART = "magnetCalibrationOnRestart";
    public static final String MAGNETIC_accelerationThreshold = "accelerationThreshold";
    public static final String MAGNETIC_accelerationMillisProximity = "accelerationMillisProximity";

    public TableCaseTamper() {
        this.tableName = TABLE_CASE_TAMPER;
        this.AddColumn(new DatabaseColumn(ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(CASE_CLOSED_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MAGNETIC_RECALIBRATION_ON_RESTART, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MAGNETIC_accelerationThreshold, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MAGNETIC_accelerationMillisProximity, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityCaseTamper entityCaseTamper = (EntityCaseTamper) databaseEntity;
        ContentValues values = new ContentValues();
        values.put(ENABLED, entityCaseTamper.enabled);
        values.put(CASE_CLOSED_THRESHOLD, entityCaseTamper.caseClosedThreshold);
        values.put(MAGNETIC_RECALIBRATION_ON_RESTART, entityCaseTamper.magnetCalibrationOnRestart);
        values.put(MAGNETIC_accelerationThreshold, entityCaseTamper.accelerationThreshold);
        values.put(MAGNETIC_accelerationMillisProximity, entityCaseTamper.accelerationMillisProximity);
        return database.insert(tableName, null, values);
    }

    public void insertRecord(EntityCaseTamper entityCaseTamper) {
        databaseReference.delete(tableName, "1", null);
        ContentValues values = new ContentValues();
        values.put(ENABLED, entityCaseTamper.enabled);
        values.put(CASE_CLOSED_THRESHOLD, entityCaseTamper.caseClosedThreshold);
        values.put(MAGNETIC_RECALIBRATION_ON_RESTART, entityCaseTamper.magnetCalibrationOnRestart);
        values.put(MAGNETIC_accelerationThreshold, entityCaseTamper.accelerationThreshold);
        values.put(MAGNETIC_accelerationMillisProximity, entityCaseTamper.accelerationMillisProximity);
        databaseReference.insert(tableName, null, values);
    }

    /**
     * Add default data record/s
     */
    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        // 	default data record
        addRecord(db,getDefault());
    }

    private EntityCaseTamper getDefault() {
        return new EntityCaseTamper(
                DefaultTableCaseTamper.DEFAULT_MAGNETIC_enabled,
                DefaultTableCaseTamper.DEFAULT_MAGNETIC_caseClosedThreshold,
                DefaultTableCaseTamper.DEFAULT_MAGNETIC_magnetCalibrationOnRestart,
                DefaultTableCaseTamper.DEFAULT_MAGNETIC_accelerationThreshold,
                DefaultTableCaseTamper.DEFAULT_MAGNETIC_accelerationMillisProximity
        );
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion == 190) {
            database.execSQL(" create table " + tableName + " (" +
                    ENABLED + " integer, " +
                    CASE_CLOSED_THRESHOLD + " integer, " +
                    MAGNETIC_RECALIBRATION_ON_RESTART + " text );");
        }

        if (oldVersion == 196 || oldVersion == 197) {
            database.execSQL("alter table " + tableName + " add column " + MAGNETIC_accelerationThreshold +
                    " integer default " + DefaultTableCaseTamper.DEFAULT_MAGNETIC_accelerationThreshold);

            database.execSQL("alter table " + tableName + " add column " + MAGNETIC_accelerationMillisProximity +
                    " integer default " + DefaultTableCaseTamper.DEFAULT_MAGNETIC_accelerationMillisProximity);
        }
    }

    public EntityCaseTamper getCaseTamperEntity() {
        Cursor cursor = databaseReference.rawQuery("select * from CaseTamper limit 1", null);
        EntityCaseTamper entityCaseTamper = getDefault();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int enabled = cursor.getInt(cursor.getColumnIndex(ENABLED));
            int caseClosedThreshold = cursor.getInt(cursor.getColumnIndex(CASE_CLOSED_THRESHOLD));
            int recalibrationOnRestart = cursor.getInt(cursor.getColumnIndex(MAGNETIC_RECALIBRATION_ON_RESTART));
            int accelerationThreshold = cursor.getInt(cursor.getColumnIndex(MAGNETIC_accelerationThreshold));
            int accelerationMillisProximity = cursor.getInt(cursor.getColumnIndex(MAGNETIC_accelerationMillisProximity));

            entityCaseTamper = new EntityCaseTamper(enabled,
                    caseClosedThreshold,
                    recalibrationOnRestart,
                    accelerationThreshold,
                    accelerationMillisProximity);

            cursor.moveToNext();
        }
        cursor.close();
        return entityCaseTamper;
    }
}
