package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.model.database.entities.EntityZones;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class TableScheduleOfZones extends DatabaseTable {
    // Table name
    private static final String TABLE_SCHEDULE_OF_ZONES = "ScheduleOfZones";
    // Column names
    private static final String COLUMN_SCHEDULE_OF_ZONES_ID = "SchedOfZonesRecId";
    private static final String COLUMN_SCHEDULE_OF_ZONES_ZONE_ID = "SchedOfZonesZoneId";
    private static final String COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_ID = "SchedOfZonesAppointId";
    private static final String COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID = "SchedOfZonesAppointTypeId";
    private static final String COLUMN_SCHEDULE_OF_ZONES_DEVICE_ID = "SchedOfZonesDeviceId";
    private static final String COLUMN_SCHEDULE_OF_ZONES_OFF_ID = "SchedOfZonesOffId";
    private static final String COLUMN_SCHEDULE_OF_ZONES_ENTITY_TYPE_ID = "SchedOfZonesEntityTypeId";
    private static final String COLUMN_SCHEDULE_OF_ZONES_ENTITY_NAME = "SchedOfZonesEntityName";
    private static final String COLUMN_SCHEDULE_OF_ZONES_NOTE = "SchedOfZonesNote";
    private static final String COLUMN_SCHEDULE_OF_ZONES_START = "SchedOfZonesStartTime";
    private static final String COLUMN_SCHEDULE_OF_ZONES_END = "SchedOfZonesEndTime";
    private static final String COLUMN_SCHEDULE_OF_ZONES_AMOUNT_OF_BIOMETRIC_TESTS = "SchedOfZonesAmountOfBiometricTests";

    /**
     * Constructor: update table's name, build columns
     */
    public TableScheduleOfZones() {

        this.tableName = TABLE_SCHEDULE_OF_ZONES;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_ZONE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_DEVICE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_OFF_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_ENTITY_TYPE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_ENTITY_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_NOTE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_START, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_END, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_SCHEDULE_OF_ZONES_AMOUNT_OF_BIOMETRIC_TESTS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     * Add event Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityScheduleOfZones SchedOfZonesRec = (EntityScheduleOfZones) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_OF_ZONES_ID, SchedOfZonesRec.RecId);
        values.put(COLUMN_SCHEDULE_OF_ZONES_ZONE_ID, SchedOfZonesRec.ZoneId);
        values.put(COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_ID, SchedOfZonesRec.AppointmentId);
        values.put(COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID, SchedOfZonesRec.AppointmentTypeId);
        values.put(COLUMN_SCHEDULE_OF_ZONES_DEVICE_ID, SchedOfZonesRec.DeviceId);
        values.put(COLUMN_SCHEDULE_OF_ZONES_OFF_ID, SchedOfZonesRec.OffenderId);
        values.put(COLUMN_SCHEDULE_OF_ZONES_ENTITY_TYPE_ID, SchedOfZonesRec.EntityTypeId);
        values.put(COLUMN_SCHEDULE_OF_ZONES_ENTITY_NAME, SchedOfZonesRec.EntityName);
        values.put(COLUMN_SCHEDULE_OF_ZONES_NOTE, SchedOfZonesRec.Note);
        values.put(COLUMN_SCHEDULE_OF_ZONES_START, SchedOfZonesRec.StartTime);
        values.put(COLUMN_SCHEDULE_OF_ZONES_END, SchedOfZonesRec.EndTime);
        values.put(COLUMN_SCHEDULE_OF_ZONES_AMOUNT_OF_BIOMETRIC_TESTS, SchedOfZonesRec.amountOfBiometricTests);

        printContentValues(values);
        // Insert to database
        return database.insert(TABLE_SCHEDULE_OF_ZONES, null, values);
    }

    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        // no default data
    }

    private EntityScheduleOfZones GetRecFromQueryCursor(Cursor QueryCursor) {
        EntityScheduleOfZones SchedOfZonesRec = new EntityScheduleOfZones(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_ZONE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_DEVICE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_OFF_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_ENTITY_TYPE_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_ENTITY_NAME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_NOTE)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_START)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_END)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_SCHEDULE_OF_ZONES_AMOUNT_OF_BIOMETRIC_TESTS)));

        // Add fields which are not covered by the construct
        SchedOfZonesRec.SetRowId(QueryCursor.getLong(QueryCursor.getColumnIndex("rowid")));

        return SchedOfZonesRec;
    }

    /**
     * Delete records of zone n
     */
    public void DeleteOffenderScheduleOfOneZone(int Zone) {
        databaseReference.delete(TABLE_SCHEDULE_OF_ZONES, COLUMN_SCHEDULE_OF_ZONES_ZONE_ID + " = ?", new String[]{String.valueOf(Zone)});
    }


    public int deleteAllSchedulesOfZone(int zoneId) {
        String Where = String.format("%s = %d", COLUMN_SCHEDULE_OF_ZONES_ZONE_ID, zoneId);
        return databaseReference.delete(TABLE_SCHEDULE_OF_ZONES, Where, null);
    }

    /**
     * Aivars @ 14.05.2015
     * Will return the current schedule for a specific Zone.
     *
     * @param zoneId The ID of the Zone for which we are willing to get the current schedule.
     * @return The current RecordScheduleOfZones for a specific Zone. NUll - in case there is nothing scheduled for the current time.
     */

    public EntityScheduleOfZones getCurrentScheduleOfZone(int zoneId) {
        EntityScheduleOfZones recordScheduleOfZones = null;
        long currentTime = TimeUtil.GetUtcTime();

        if (getScheduleOfZoneCount() > 0) {
            String Where = String.format("(%s = %d) AND (%d >= %s AND %d <= %s)",
                    COLUMN_SCHEDULE_OF_ZONES_ZONE_ID, zoneId,
                    currentTime, COLUMN_SCHEDULE_OF_ZONES_START, currentTime, COLUMN_SCHEDULE_OF_ZONES_END);

            Cursor cursorCurrentSchedule = databaseReference.query(TABLE_SCHEDULE_OF_ZONES,
                    columnNamesArray,
                    Where,
                    null,
                    null,
                    null,
                    "1");

            if (cursorCurrentSchedule.getCount() > 0) {
                cursorCurrentSchedule.moveToFirst();
                recordScheduleOfZones = GetRecFromQueryCursor(cursorCurrentSchedule);
            }

            cursorCurrentSchedule.close();
        }

        return recordScheduleOfZones;
    }

    public int getScheduleOfZoneCount() {
        int scheduleOfZoneCount = 0;
        Cursor queryCursor = databaseReference.query(
                TABLE_SCHEDULE_OF_ZONES,        // Table name
                columnNamesArray,        // Columns names
                null,    // Where
                null,   // Where Args
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                null);                //Limit: 1 line only, if we don't want any limit we need to write instead null

        scheduleOfZoneCount = queryCursor.getCount();
        queryCursor.close();

        return scheduleOfZoneCount;
    }

    public EntityScheduleOfZones GetCurrentMBISchedule() {
        return GetCurrentSchedule(TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI);
    }

    public EntityScheduleOfZones GetCurrentBiometricSchedule() {
        return GetCurrentSchedule(TableZonesManager.SCHEDULE_OF_ZONE_TYPE_BIO);
    }

    private EntityScheduleOfZones GetCurrentSchedule(int type) {
        // Select query
        Cursor QueryCursor = databaseReference.query(TABLE_SCHEDULE_OF_ZONES,        // Table name
                columnNamesArray,        // Columns names
                COLUMN_SCHEDULE_OF_ZONES_START + " < ? AND " + COLUMN_SCHEDULE_OF_ZONES_END + " > ? AND " + COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID + "=" + type,
                //new String [] {Long.toString(TimeUtility.GetLocalTime()), Long.toString(TimeUtility.GetLocalTime())}
                new String[]{Long.toString(TimeUtil.GetUtcTime()), Long.toString(TimeUtil.GetUtcTime())}, // Where Args
                null,                // Group by...
                null,                // Having...
                "rowid",            // Order by insert order
                "1");                // Limit...
        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }

        EntityScheduleOfZones getRecFromQueryCursor = GetRecFromQueryCursor(QueryCursor);
        QueryCursor.close();

        return getRecFromQueryCursor;
    }


    public EntityScheduleOfZones GetNextMBIAppointment() {
        String Where = String.format("(%s > %d) AND (%s =" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI + ")",
                COLUMN_SCHEDULE_OF_ZONES_START, TimeUtil.GetUtcTime(),
                COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID);

        return GetNextAppointment(Where);
    }


    public EntityScheduleOfZones GetNextBiometricAppointment() {
        String Where = String.format("(%s > %d) AND (%s =" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_BIO + ")",
                COLUMN_SCHEDULE_OF_ZONES_START, TimeUtil.GetUtcTime(),
                COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID);

        return GetNextAppointment(Where);
    }

    public EntityScheduleOfZones GetLastAppointmentWhichNotBiometric(int zoneId) {
        String Where = String.format("(%s < %d) AND (%s = %d) AND (%s !=" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_BIO + ")",
                COLUMN_SCHEDULE_OF_ZONES_END, TimeUtil.GetUtcTime(),
                COLUMN_SCHEDULE_OF_ZONES_ZONE_ID, zoneId,
                COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID);

        return GetAppointment(Where,false);
    }

    public EntityScheduleOfZones GetNextAppointmentWhichNotBiometric(int zoneId) {
        String Where = String.format("(%s > %d) AND (%s = %d) AND (%s !=" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_BIO + ")",
                COLUMN_SCHEDULE_OF_ZONES_START, TimeUtil.GetUtcTime(),
                COLUMN_SCHEDULE_OF_ZONES_ZONE_ID, zoneId,
                COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID);

        return GetAppointment(Where);
    }

    private EntityScheduleOfZones getNextAppointmentByZoneIDAndAppointmentType(int zoneId, int appointmentType) {
        String Where = String.format("(%s > %d) AND (%s = %d) AND (%s =" + appointmentType + ")",
                COLUMN_SCHEDULE_OF_ZONES_START, TimeUtil.GetUtcTime(),
                COLUMN_SCHEDULE_OF_ZONES_ZONE_ID, zoneId,
                COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID);

        return GetAppointment(Where);
    }


    public boolean getIsOffenderInSuspendedSchedule(int type) {
        String where = String.format(Locale.ENGLISH, "SchedOfZonesAppointTypeId = %d and SchedOfZonesStartTime <= %d and SchedOfZonesEndTime >= %d",
                type, TimeUtil.GetUtcTime(), TimeUtil.GetUtcTime());
        Cursor cursor = databaseReference.query(
                TABLE_SCHEDULE_OF_ZONES,
                columnNamesArray,
                where,
                null,
                null,
                null,
                null);
        boolean isInSuspend = cursor.getCount() != 0;
        cursor.close();
        return isInSuspend;

    }

    /**
     * Get next record according to given time
     */
    private EntityScheduleOfZones GetNextAppointment(String Where) {

        // Select query
        Cursor QueryCursor = databaseReference.query(TABLE_SCHEDULE_OF_ZONES, // Table name
                columnNamesArray, // Columns names
                Where, // Where
                null, // new String [] {String.valueOf(Before),
                // String.valueOf(After)}, // Where Args
                null, // Group by...
                null, // Having...
                COLUMN_SCHEDULE_OF_ZONES_START, // Order by insert order
                "1"); // Limit...

        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }

        EntityScheduleOfZones getRecFromQueryCursor = GetRecFromQueryCursor(QueryCursor);
        QueryCursor.close();

        return getRecFromQueryCursor;
    }

    private EntityScheduleOfZones GetAppointment(String Where) {
        return GetAppointment(Where,true);
    }
    private EntityScheduleOfZones GetAppointment(String Where,boolean asc) {

        // Select query
        Cursor QueryCursor = databaseReference.query(TABLE_SCHEDULE_OF_ZONES, // Table name
                columnNamesArray, // Columns names
                Where, // Where
                null, // new String [] {String.valueOf(Before),
                // String.valueOf(After)}, // Where Args
                null, // Group by...
                null, // Having...
                COLUMN_SCHEDULE_OF_ZONES_START + (asc ?" ASC" : " DESC"), // Order by insert order
                "1"); // Limit...

        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }

        EntityScheduleOfZones getRecFromQueryCursor = GetRecFromQueryCursor(QueryCursor);
        QueryCursor.close();

        return getRecFromQueryCursor;
    }

    public long getClosestTimeOfMBIAppointment(String timeType) {
        String Where = String.format("(%s > %d) AND (%s =" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI + ")",
                timeType, TimeUtil.getCurrentTime(),
                COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID);
        return getClosestTimeOfAppointment(timeType, Where);
    }

    public long getClosestTimeWhichNotBiometric(String timeType, long time) {
        String Where = String.format("(%s > %d) AND (%s !=" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_BIO + ")",
                timeType, time,
                COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID);
        return getClosestTimeOfAppointment(timeType, Where);
    }

    /**
     * @param timeType
     * @return return closet time of appointment by timeType(start time or end time)
     */
    private long getClosestTimeOfAppointment(String timeType, String Where) {

        // Select query
        Cursor QueryCursor = databaseReference.query(TABLE_SCHEDULE_OF_ZONES, // Table name
                columnNamesArray, // Columns names
                Where, // Where
                null, // new String [] {String.valueOf(Before),
                // String.valueOf(After)}, // Where Args
                null, // Group by...
                null, // Having...
                timeType, // Order by insert order
                "1"); // Limit...

        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            return -1;
        } else {
            QueryCursor.moveToFirst();
        }

        EntityScheduleOfZones getRecFromQueryCursor = GetRecFromQueryCursor(QueryCursor);
        QueryCursor.close();

        if (timeType == COLUMN_SCHEDULE_OF_ZONES_START) {
            return getRecFromQueryCursor.StartTime;
        } else if (timeType == COLUMN_SCHEDULE_OF_ZONES_END) {
            return getRecFromQueryCursor.EndTime;
        }
        return -1;
    }

    /**
     * search on all schedules in "type" that exists in DB and find the closest start or end time of appointment from current time
     *
     * @return the closest time
     */
    public long getClosetTimeOfAppointmentIncludesGraceTime(int type, boolean shouldSearchForGraceTime) {
        long closestTime = -1;
        long closestStartTime = -1;
        long closestEndTime = -1;
        if (type == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI) {
            closestEndTime = getClosestTimeOfMBIAppointment(COLUMN_SCHEDULE_OF_ZONES_END);
            closestStartTime = getClosestTimeOfMBIAppointment(COLUMN_SCHEDULE_OF_ZONES_START);
        } else if (type == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_ALL_EXCEPT_BIO) {
            long graceTime = TimeUnit.SECONDS.toMillis(TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                    (OFFENDER_DETAILS_CONS.DETAILS_OFF_SCHEDULE_GRACE));
            closestEndTime = getClosestTimeWhichNotBiometric(COLUMN_SCHEDULE_OF_ZONES_END, TimeUtil.getCurrentTime() - graceTime);
            closestStartTime = getClosestTimeWhichNotBiometric(COLUMN_SCHEDULE_OF_ZONES_START, TimeUtil.getCurrentTime() - graceTime);
        }

        if (closestStartTime != -1 && closestEndTime != -1) {
            if (closestStartTime < closestEndTime) {
                closestTime = closestStartTime;
            } else {
                closestTime = closestEndTime;
            }
        } else if (closestStartTime != -1) {
            closestTime = closestStartTime;
        } else if (closestEndTime != -1) {
            closestTime = closestEndTime;
        }

        if (closestTime != -1 && shouldSearchForGraceTime) {
            closestTime = getClosestAppointmentIncludesGraceTime(closestTime);
        }

        return closestTime;
    }

    private long getClosestAppointmentIncludesGraceTime(long closetTimeToStartAlarmManager) {
        long graceTime = TimeUnit.SECONDS.toMillis(TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                (OFFENDER_DETAILS_CONS.DETAILS_OFF_SCHEDULE_GRACE));
        long currentTimeMinusClosetTimeToStartAlarmManager = closetTimeToStartAlarmManager - TimeUtil.getCurrentTime();

        // wake up in correct time
        if (currentTimeMinusClosetTimeToStartAlarmManager <= graceTime && currentTimeMinusClosetTimeToStartAlarmManager >= 0) {
        }

        // wake up in correct time + grace
        else if (currentTimeMinusClosetTimeToStartAlarmManager < 0 && currentTimeMinusClosetTimeToStartAlarmManager >= -graceTime) {
            closetTimeToStartAlarmManager = closetTimeToStartAlarmManager + graceTime;
        }

        // wake up in correct time - grace
        else {
            closetTimeToStartAlarmManager = closetTimeToStartAlarmManager - graceTime;
        }

        return closetTimeToStartAlarmManager;
    }

    public long getNextStartTimeOfDefaultMBIZoneWithoutAppointmens() {
        EntityZones zoneWithDefaultScheduleAsMustBeIn = DatabaseAccess.getInstance().tableZones.getZoneWithDefaultScheduleAsMustBeIn();
        if (zoneWithDefaultScheduleAsMustBeIn != null) {
            List<EntityScheduleOfZones> scheduleOfZonesList = new ArrayList<>();
            String Where = String.format("(%s > %d)"
                            + "AND (%s !=" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI + ") "
                            + "AND (%s !=" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_BIO + ")"
                            + "AND (%d = %s)",
                    COLUMN_SCHEDULE_OF_ZONES_END, TimeUtil.getCurrentTime(),
                    COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID,
                    COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID,
                    zoneWithDefaultScheduleAsMustBeIn.ZoneId, COLUMN_SCHEDULE_OF_ZONES_ZONE_ID);

            Cursor QueryCursor = databaseReference.query(TABLE_SCHEDULE_OF_ZONES, // Table name
                    columnNamesArray, // Columns names
                    Where, // Where
                    null, // new String [] {String.valueOf(Before),
                    // String.valueOf(After)}, // Where Args
                    null, // Group by...
                    null, // Having...
                    COLUMN_SCHEDULE_OF_ZONES_START, // Order by insert order
                    null); // Limit...

            while (QueryCursor.moveToNext()) {
                EntityScheduleOfZones rec = GetRecFromQueryCursor(QueryCursor);
                scheduleOfZonesList.add(rec);
            }
            QueryCursor.close();

            EntityScheduleOfZones recordScheduleOfZones;

            for (int i = 0, j = 1; j < scheduleOfZonesList.size(); i++, j++) {
                recordScheduleOfZones = scheduleOfZonesList.get(i);
                if (recordScheduleOfZones.EndTime != scheduleOfZonesList.get(j).StartTime) {
                    return recordScheduleOfZones.EndTime;
                }
            }

            if (!scheduleOfZonesList.isEmpty()) {
                return scheduleOfZonesList.get(scheduleOfZonesList.size() - 1).EndTime;
            }

        }

        return 0;
    }


    public List<EntityScheduleOfZones> getDayScheduleWhereUserMustBeIn(long Before, long After) {
        List<EntityScheduleOfZones> scheduleOfZonesList = new ArrayList<EntityScheduleOfZones>();

        String Where = String.format("(((%s BETWEEN %d AND %d) AND (%s < %d)) OR" + "(%s >= %d AND %s < %d) OR" + "(%s < %d AND %s > %d)) " +
                        "AND ((%s =" + TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI + ") OR (%s = 8))",
                COLUMN_SCHEDULE_OF_ZONES_END, Before, After,
                COLUMN_SCHEDULE_OF_ZONES_START, Before,
                COLUMN_SCHEDULE_OF_ZONES_START, Before, COLUMN_SCHEDULE_OF_ZONES_START, After,
                COLUMN_SCHEDULE_OF_ZONES_START, Before,
                COLUMN_SCHEDULE_OF_ZONES_END, After,
                COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID, COLUMN_SCHEDULE_OF_ZONES_APPOINTMENT_TYPE_ID);

        // Select query
        Cursor cursorScheduleOfZones = databaseReference.query(
                TABLE_SCHEDULE_OF_ZONES,        // Table name
                columnNamesArray,        // Columns names
                Where,                // Where ...
                null,                // Where Args
                null,                // Group by...
                null,                // Having...
                COLUMN_SCHEDULE_OF_ZONES_START,            // Order by insert order
                null);                // Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.

        while (cursorScheduleOfZones.moveToNext()) {
            EntityScheduleOfZones rec = GetRecFromQueryCursor(cursorScheduleOfZones);
            scheduleOfZonesList.add(rec);
        }
        cursorScheduleOfZones.close();
        return scheduleOfZonesList;
    }

    public boolean isOffenderInCurrentAppointmentLocatedInsideZone(EntityScheduleOfZones currentScheduleOfZone) {
        List<EntityZones> zonesOffenderLocatedInsideList = DatabaseAccess.getInstance().tableZones.getAllZonesThatOffenderLocatedInside();
        for (EntityZones recordZone : zonesOffenderLocatedInsideList) {
            if (currentScheduleOfZone.ZoneId == recordZone.ZoneId) {
                return true;
            }
        }
        return false;

    }

    public EntityScheduleOfZones getPresentScheduleAsMBOWhileOffenderLocatedInsideZone() {
        List<EntityZones> zonesOffenderLocatedInsideList = DatabaseAccess.getInstance().tableZones.getAllZonesThatOffenderLocatedInside();
        for (EntityZones recordZone : zonesOffenderLocatedInsideList) {
            EntityScheduleOfZones currentSchedule = GetCurrentSchedule(TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO);
            if (currentSchedule != null && currentSchedule.ZoneId == recordZone.ZoneId) {
                return currentSchedule;
            }
        }
        return null;

    }

    public EntityScheduleOfZones getFutureScheduleAsMBOWhileOffenderLocatedInsideZone() {
        List<EntityZones> zonesOffenderLocatedInsideList = DatabaseAccess.getInstance().tableZones.getAllZonesThatOffenderLocatedInside();
        for (EntityZones recordZones : zonesOffenderLocatedInsideList) {
            EntityScheduleOfZones nextMBOAppointment = getNextAppointmentByZoneIDAndAppointmentType(recordZones.ZoneId, TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO);
            if (nextMBOAppointment != null && nextMBOAppointment.ZoneId == recordZones.ZoneId) {
                return nextMBOAppointment;
            }
        }
        return null;

    }

}
