package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.EntityAutoRestart;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;

public class TableAutoRestart extends DatabaseTable {

    public static final String TABLE_AutoRestart = "AutoRestart";

    public static final String JSON_DATA = "JSON_DATA";

    public TableAutoRestart() {
        this.tableName = TABLE_AutoRestart;

        this.AddColumn(new DatabaseColumn(JSON_DATA, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
    }

    public void insertRecord(EntityAutoRestart entityAutoRestart) {
        databaseReference.delete(TABLE_AutoRestart, "1", null);
        ContentValues values = new ContentValues();
        values.put(JSON_DATA, entityAutoRestart.json);
        databaseReference.insert(TABLE_AutoRestart, null, values);
    }

    public EntityAutoRestart getEntityAutoRestart() {
        Cursor cursor = databaseReference.rawQuery("select * from " + TABLE_AutoRestart, null);
        if (cursor == null || !cursor.moveToFirst()) return null;
        cursor.moveToFirst();
        String json = cursor.getString(cursor.getColumnIndex(JSON_DATA));
        cursor.close();

        EntityAutoRestart res = new EntityAutoRestart(json);
        return res;
    }


    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

    }
}