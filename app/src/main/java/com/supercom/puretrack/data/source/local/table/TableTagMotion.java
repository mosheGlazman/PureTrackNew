package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityTagMotion;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;

public class TableTagMotion extends DatabaseTable {

    private static final String TABLE_TAG_MOTION = "TagMotion";

    public static final String ENABLED = "Enabled";
    public static final String SIGNALS_TO_NO_MOTION = "SignalsToNoMotion";
    public static final String NO_MOTION_PERCENTAGE = "NoMotionPercentage";
    public static final String SIGNALS_TO_MOTION = "SignalsToMotion";
    public static final String MOTION_PERCENTAGE = "MotionPercentage";

    public TableTagMotion() {
        this.tableName = TABLE_TAG_MOTION;

        this.AddColumn(new DatabaseColumn(ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(SIGNALS_TO_NO_MOTION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(NO_MOTION_PERCENTAGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(SIGNALS_TO_MOTION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(MOTION_PERCENTAGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityTagMotion entityTagMotion = (EntityTagMotion) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(ENABLED, entityTagMotion.enabled);
        values.put(SIGNALS_TO_NO_MOTION, entityTagMotion.signalsToNoMotion);
        values.put(NO_MOTION_PERCENTAGE, entityTagMotion.noMotionPercentage);
        values.put(SIGNALS_TO_MOTION, entityTagMotion.signalsToMotion);
        values.put(MOTION_PERCENTAGE, entityTagMotion.motionPercentage);
        return database.insert(TABLE_TAG_MOTION, null, values);
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion == 175) {
            database.execSQL(" create table " + TABLE_TAG_MOTION + " (" +
                    ENABLED + " integer, " +
                    SIGNALS_TO_NO_MOTION + " integer, " +
                    NO_MOTION_PERCENTAGE + " integer, " +
                    SIGNALS_TO_MOTION + " integer, " +
                    MOTION_PERCENTAGE + " integer);");
        }
    }

    public boolean isTagMotionEnabled() {
        Cursor cursor = databaseReference.rawQuery("select * from TagMotion ", null);
        boolean result = false;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result = cursor.getInt(cursor.getColumnIndex(ENABLED)) > 0;
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    public EntityTagMotion getTagMotionEntity() {
        Cursor cursor = databaseReference.rawQuery("select * from TagMotion ", null);
        EntityTagMotion result = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int enabled = cursor.getInt(cursor.getColumnIndex(ENABLED));
            int signalsToNoMotion = cursor.getInt(cursor.getColumnIndex(SIGNALS_TO_NO_MOTION));
            int noMotionPercentage = cursor.getInt(cursor.getColumnIndex(NO_MOTION_PERCENTAGE));
            int signalsToMotion = cursor.getInt(cursor.getColumnIndex(SIGNALS_TO_MOTION));
            int motionPercentage = cursor.getInt(cursor.getColumnIndex(MOTION_PERCENTAGE));
            result = new EntityTagMotion(enabled, signalsToNoMotion, noMotionPercentage, signalsToMotion, motionPercentage);
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }


    public void deletePreviousRecord() {
        databaseReference.delete(TABLE_TAG_MOTION,
                "1",
                null);
    }
}
