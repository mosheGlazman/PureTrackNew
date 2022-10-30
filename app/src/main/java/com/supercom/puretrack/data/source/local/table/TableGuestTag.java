package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityGuestTag;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class TableGuestTag extends DatabaseTable {

    private static final String TABLE_GUEST_TAG = "GuestTag";

    public static final String DEVICE_CONFIG_GUEST_TAG_ID = "guestTagId";
    public static final String DEVICE_CONFIG_GUEST_TAG_TIME = "lastReceivedTime";


    public TableGuestTag() {
        this.tableName = TABLE_GUEST_TAG;

        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_GUEST_TAG_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_GUEST_TAG_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
    }


    public boolean wasTagReceived(int guestTagId) {
        Cursor cursor = databaseReference.rawQuery("select * from GuestTag where guestTagId = " + guestTagId, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public void checkForInvalidTags() {
        int guestTagTime = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GUEST_TAG_TIME);
        boolean guestTagEnabled = TableOffenderDetailsManager.sharedInstance().getIsGuestTagEnabled();
        if (guestTagTime < 1 || !guestTagEnabled) return;
        long currentTime = System.currentTimeMillis() / 1000;
        Cursor cursor = databaseReference.rawQuery("select * from GuestTag " +
                " where " + currentTime + " - lastReceivedTime " + " > " + guestTagTime, null);
        List<String> invalidTagsList = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            invalidTagsList.add(cursor.getString(cursor.getColumnIndex(DEVICE_CONFIG_GUEST_TAG_ID)));
            cursor.moveToNext();
        }

        cursor.close();
        if (invalidTagsList.isEmpty()) return;
        for (int i = 0; i < invalidTagsList.size(); i++) {
            String tagId = invalidTagsList.get(i);
            deleteGuestTag(tagId);
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig
                            .EventTypes.eventGuestTagLeft, -1, -1, System.currentTimeMillis(),
                    "{\"info\":\"TagId=" + tagId + "\"}");
        }
    }

    public void deleteGuestTag(String tagId){
        databaseReference.delete(TABLE_GUEST_TAG,
                DEVICE_CONFIG_GUEST_TAG_ID + "=" + tagId,
                null);
    }

    public void insertRecord(int tagId) {
        ContentValues values = new ContentValues();
        values.put(DEVICE_CONFIG_GUEST_TAG_ID, tagId);
        values.put(DEVICE_CONFIG_GUEST_TAG_TIME, System.currentTimeMillis() / 1000);
        databaseReference.insert(TABLE_GUEST_TAG, null, values);
    }

    public void updateGuestTagTime(int guestTagId) {
        String newTime = String.valueOf(System.currentTimeMillis() / 1000);
        Cursor cursor = databaseReference.rawQuery("update GuestTag " +
                "set lastReceivedTime = " + newTime + " " +
                "where guestTagId = " + guestTagId, null);
        cursor.moveToFirst();
        cursor.close();
    }

    public void wipeTable() {
        databaseReference.delete(TABLE_GUEST_TAG,
                "1",
                null);
    }

    @Override
    public int GetColumnNum() {
        return super.GetColumnNum();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityGuestTag recordGuestTag = (EntityGuestTag) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(DEVICE_CONFIG_GUEST_TAG_ID, recordGuestTag.enabled);
        values.put(DEVICE_CONFIG_GUEST_TAG_TIME, recordGuestTag.time);
        return database.insert(TABLE_GUEST_TAG, null, values);
    }


    public EntityGuestTag GetRecord(SQLiteDatabase db) {
        // Select query
        Cursor QueryCursor = db.query(TABLE_GUEST_TAG, columnNamesArray,
                null, null, null, null, null);

        EntityGuestTag recordGuestTag;

        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            Log.i(TAG, "GuestTag Table is empty, will create default values !");
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] GuestTag Table is empty, will create default values !", false);
            return null;
        } else {
            QueryCursor.moveToFirst();
            recordGuestTag = GetRecFromQueryCursor(QueryCursor);
        }

        QueryCursor.close();

        return recordGuestTag;
    }

    private EntityGuestTag GetRecFromQueryCursor(Cursor queryCursor) {
        return new EntityGuestTag(
                queryCursor.getInt(queryCursor.getColumnIndex(DEVICE_CONFIG_GUEST_TAG_ID)),
                queryCursor.getInt(queryCursor.getColumnIndex(DEVICE_CONFIG_GUEST_TAG_TIME)));
    }


    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion >= 153 && oldVersion <= 157) {
            database.execSQL("CREATE TABLE " + TABLE_GUEST_TAG + " (" + DEVICE_CONFIG_GUEST_TAG_ID + " INTEGER,"
                    + DEVICE_CONFIG_GUEST_TAG_TIME +
                    " INTEGER,tagId INTEGER,PRIMARY KEY(" + DEVICE_CONFIG_GUEST_TAG_ID + "));");
        }
    }

}
