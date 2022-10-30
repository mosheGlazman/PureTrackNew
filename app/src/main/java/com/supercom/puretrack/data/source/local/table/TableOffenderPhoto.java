package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityOffenderPhoto;

import java.util.ArrayList;
import java.util.List;

public class TableOffenderPhoto extends DatabaseTable {

    private static final String TABLE_OFFENDER_PHOTO = "OffenderPhoto";

    public static final String REQUEST_ID = "requestId";
    public static final String PHOTO_ENCODED_TO_BASE_64 = "photoEncodedToBase64";
    public static final String EVENT_ID = "eventId";

    public TableOffenderPhoto() {
        this.tableName = TABLE_OFFENDER_PHOTO;

        this.AddColumn(new DatabaseColumn(REQUEST_ID, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(PHOTO_ENCODED_TO_BASE_64, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(EVENT_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
    }

    public List<EntityOffenderPhoto> getOffenderPhotos() {
        Cursor cursor = databaseReference.rawQuery("select * from OffenderPhoto", null);
        List<EntityOffenderPhoto> offenderPhotos = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String requestId = cursor.getString(cursor.getColumnIndex(REQUEST_ID));
            String photoEncodedToBase64 = cursor.getString(cursor.getColumnIndex(PHOTO_ENCODED_TO_BASE_64));
            int eventId = cursor.getInt(cursor.getColumnIndex(EVENT_ID));
            offenderPhotos.add(new EntityOffenderPhoto(photoEncodedToBase64, requestId, eventId));
            cursor.moveToNext();
        }
        cursor.close();
        return offenderPhotos;
    }

    public void insertRecord(EntityOffenderPhoto recordOffenderPhoto) {
        ContentValues values = new ContentValues();
        values.put(REQUEST_ID, recordOffenderPhoto.requestId);
        values.put(PHOTO_ENCODED_TO_BASE_64, recordOffenderPhoto.photoEncodedToBase64);
        values.put(EVENT_ID, recordOffenderPhoto.eventId);
        databaseReference.insert(TABLE_OFFENDER_PHOTO, null, values);
    }

    public void deleteOffenderPhoto(String requestId){
        databaseReference.delete(TABLE_OFFENDER_PHOTO,
                REQUEST_ID + "=" + requestId,
                null);
    }

    public void wipeTable() {
        databaseReference.delete(TABLE_OFFENDER_PHOTO,
                "1",
                null);
    }

    @Override
    public int GetColumnNum() {
        return super.GetColumnNum();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityOffenderPhoto recordOffenderPhoto = (EntityOffenderPhoto) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(REQUEST_ID, recordOffenderPhoto.requestId);
        values.put(PHOTO_ENCODED_TO_BASE_64, recordOffenderPhoto.photoEncodedToBase64);
        values.put(EVENT_ID, recordOffenderPhoto.eventId);
        return database.insert(TABLE_OFFENDER_PHOTO, null, values);
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion <= 162) {
            database.execSQL("CREATE TABLE " + TABLE_OFFENDER_PHOTO + " (" + REQUEST_ID + " text,"
                    + PHOTO_ENCODED_TO_BASE_64 + " text," + EVENT_ID + " integer " + ", PRIMARY KEY(" + REQUEST_ID + "));");
        }
        switch (oldVersion) {
            case 163:
                database.execSQL("alter table " + TABLE_OFFENDER_PHOTO + " add column " + EVENT_ID + " integer " + " default -1");
                break;
        }
    }
}
