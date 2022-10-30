package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntitySchedule;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.enums.EnumScheduleType;

public class TableSchedule extends DatabaseTable {
    // Table name
    private static final String TABLE_SCHEDULE = "Schedule";
    // Column names
    private static final String COLUMN_SCHEDULE_ID = "SchedRecId";
    private static final String COLUMN_SCHEDULE_OFF_ID = "SchedOffId";
    private static final String COLUMN_SCHEDULE_START = "SchedStartTime";
    private static final String COLUMN_SCHEDULE_END = "SchedEndTime";
    private static final String COLUMN_SCHEDULE_TYPE = "SchedType";
    private static final String COLUMN_SCHEDULE_TESTS_NUM = "SchedTestsNum";
    private static final String COLUMN_SCHEDULE_NOTE = "SchedNote";

    /**
     * Constructor: update table's name, build columns
     */
    public TableSchedule() {

        this.tableName = TABLE_SCHEDULE;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OFF_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_START, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_END, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_TESTS_NUM, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_NOTE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     * Add event Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntitySchedule SchedRec = (EntitySchedule) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_ID, SchedRec.AppointmentId);
        values.put(COLUMN_SCHEDULE_OFF_ID, SchedRec.OffenderId);
        values.put(COLUMN_SCHEDULE_START, SchedRec.StartTime);
        values.put(COLUMN_SCHEDULE_END, SchedRec.EndTime);
        values.put(COLUMN_SCHEDULE_TYPE, SchedRec.Type);
        values.put(COLUMN_SCHEDULE_TESTS_NUM, SchedRec.BioTestsNum);
        values.put(COLUMN_SCHEDULE_NOTE, SchedRec.Note);
        // Insert to database
        return database.insert(TABLE_SCHEDULE, null, values);
    }

    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        // no default data

        addRecord(db, new EntitySchedule(1, 888, 1409881252000L, 1409881852000L, EnumScheduleType.SCHEDULE_TYPE_CGO.getValue(), 1, "yesterday"));  // yesterday
        addRecord(db, new EntitySchedule(1, 888, 1410391672000L, 1410399652000L, EnumScheduleType.SCHEDULE_TYPE_CGO.getValue(), 1, "over midnight"));  // over midnight
        addRecord(db, new EntitySchedule(1, 888, 1410412252000L, 1410413452000L, EnumScheduleType.SCHEDULE_TYPE_CGO.getValue(), 1, "today"));  // today
        addRecord(db, new EntitySchedule(1, 888, 1413005452000L, 1413009052000L, EnumScheduleType.SCHEDULE_TYPE_CGO.getValue(), 1, "future"));  // future
    }
}
