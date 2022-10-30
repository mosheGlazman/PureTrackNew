package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.model.database.entities.EntityZones;
import com.supercom.puretrack.util.general.LoggingUtil;

import java.util.ArrayList;
import java.util.List;

public class TableZones extends DatabaseTable {
    // Table name
    private static final String TABLE_ZONES = "Zones";
    // Column names
    public static final String COLUMN_ZON_ID = "ZonRecId";
    private static final String COLUMN_ZON_OFF_ID = "ZonOffId";
    private static final String COLUMN_ZON_NAME = "ZonName";
    private static final String COLUMN_ZON_TYPE_ID = "ZonTypeId";
    private static final String COLUMN_ZON_SHAPE_TYPE = "ZonShapeType";
    private static final String COLUMN_ZON_POINTS_JSON_STR = "ZonPointsJsonStr";
    private static final String COLUMN_ZON_LATITUDE = "ZonLatitude";
    private static final String COLUMN_ZON_LONGITUDE = "ZonLongitude";
    private static final String COLUMN_ZON_RADIUS = "ZonRadius";
    private static final String COLUMN_ZON_SAMPLE_RATE = "ZonSampleRate";
    private static final String COLUMN_ZON_NOTE = "ZonNote";
    private static final String COLUMN_ZON_IS_INTO = "IsInto";
    private static final String COLUMN_ZON_INC_CNT = "InclusionZoneCnt";
    private static final String COLUMN_ZON_INC_CNT_BUFF = "InclusionZoneCntBuff";
    private static final String COLUMN_ZON_EXC_CNT = "ExclusionZoneCnt";
    private static final String COLUMN_ZON_EXC_CNT_BUFF = "ExclusionZoneCntBuff";
    private static final String COLUMN_ZON_DEFAULT_APPOINTMENT_TYPE_ID = "defaultAppointmentTypeId";
    private static final String COLUMN_ZON_VERSION = "columnZonVersion";
    public static final String COLUMN_ZON_SCHEDULE_VERSION = "columnZonScheduleVersion";
    public static final String COLUMN_ZON_BUFFER = "columnZonBuffer";
    private static final String COLUMN_ZON_IS_INTO_BUFFER = "IsIntoBuffer";


    /**
     * Constructor: update table's name, build columns
     */
    public TableZones() {

        this.tableName = TABLE_ZONES;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER_PK));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_OFF_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_TYPE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_SHAPE_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_POINTS_JSON_STR, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_LATITUDE, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_LONGITUDE, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_RADIUS, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_SAMPLE_RATE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_NOTE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_IS_INTO, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_INC_CNT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_INC_CNT_BUFF, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_EXC_CNT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_EXC_CNT_BUFF, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_DEFAULT_APPOINTMENT_TYPE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_SCHEDULE_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_BUFFER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ZON_IS_INTO_BUFFER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     * Add event Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityZones ZonRec = (EntityZones) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_ZON_ID, ZonRec.ZoneId);
        values.put(COLUMN_ZON_NAME, ZonRec.ZoneName);
        values.put(COLUMN_ZON_OFF_ID, ZonRec.OffenderId);
        values.put(COLUMN_ZON_TYPE_ID, ZonRec.TypeId);
        values.put(COLUMN_ZON_SHAPE_TYPE, ZonRec.ShapeType);
        values.put(COLUMN_ZON_POINTS_JSON_STR, ZonRec.PointsJsonStr);
        values.put(COLUMN_ZON_LATITUDE, ZonRec.Latitude);
        values.put(COLUMN_ZON_LONGITUDE, ZonRec.Longitude);
        values.put(COLUMN_ZON_RADIUS, ZonRec.Radius);
        values.put(COLUMN_ZON_SAMPLE_RATE, ZonRec.SampleRate);
        values.put(COLUMN_ZON_NOTE, ZonRec.Note);
        values.put(COLUMN_ZON_IS_INTO, ZonRec.isIntoExclusionZoneState);
        values.put(COLUMN_ZON_INC_CNT, ZonRec.EnteringZoneCnt);
        values.put(COLUMN_ZON_INC_CNT_BUFF, ZonRec.InclusionZoneCntBuffer);
        values.put(COLUMN_ZON_EXC_CNT, ZonRec.ExitingZoneCnt);
        values.put(COLUMN_ZON_EXC_CNT_BUFF, ZonRec.ExclusionZoneCntBuffer);
        values.put(COLUMN_ZON_DEFAULT_APPOINTMENT_TYPE_ID, ZonRec.defaultAppointmentTypeId);
        values.put(COLUMN_ZON_VERSION, ZonRec.zoneVersion);
        values.put(COLUMN_ZON_SCHEDULE_VERSION, ZonRec.scheduleVersionOfZone);
        values.put(COLUMN_ZON_BUFFER, ZonRec.bufferZone);
        values.put(COLUMN_ZON_IS_INTO_BUFFER, ZonRec.isIntoBufferZoneState);

        LoggingUtil.fileLogZonesUpdate("\nAddRecord() -> TABLE_ZONES : "
                + "\n   ZoneId = " + ZonRec.ZoneId
                + "\n   TypeId (In/Excl) = " + ZonRec.TypeId
                + "\n   ShapeType = " + ZonRec.ShapeType
                + "\n   IsInto = " + ZonRec.isIntoExclusionZoneState
                + "\n   defaultAppointmentTypeId = " + ZonRec.defaultAppointmentTypeId
                + "\n   zoneVersion = " + ZonRec.zoneVersion);
        // Insert to database
        return database.insert(tableName, null, values);
    }

    @Override
    public long insertWithOnConflict(SQLiteDatabase db, DatabaseEntity record) {
        EntityZones ZonRec = (EntityZones) record;

        ContentValues values = new ContentValues();
        values.put(COLUMN_ZON_ID, ZonRec.ZoneId);
        values.put(COLUMN_ZON_NAME, ZonRec.ZoneName);
        values.put(COLUMN_ZON_OFF_ID, ZonRec.OffenderId);
        values.put(COLUMN_ZON_TYPE_ID, ZonRec.TypeId);
        values.put(COLUMN_ZON_SHAPE_TYPE, ZonRec.ShapeType);
        values.put(COLUMN_ZON_POINTS_JSON_STR, ZonRec.PointsJsonStr);
        values.put(COLUMN_ZON_LATITUDE, ZonRec.Latitude);
        values.put(COLUMN_ZON_LONGITUDE, ZonRec.Longitude);
        values.put(COLUMN_ZON_RADIUS, ZonRec.Radius);
        values.put(COLUMN_ZON_SAMPLE_RATE, ZonRec.SampleRate);
        values.put(COLUMN_ZON_NOTE, ZonRec.Note);
        values.put(COLUMN_ZON_IS_INTO, ZonRec.isIntoExclusionZoneState);
        values.put(COLUMN_ZON_INC_CNT, ZonRec.EnteringZoneCnt);
        values.put(COLUMN_ZON_INC_CNT_BUFF, ZonRec.InclusionZoneCntBuffer);
        values.put(COLUMN_ZON_EXC_CNT, ZonRec.ExitingZoneCnt);
        values.put(COLUMN_ZON_EXC_CNT_BUFF, ZonRec.ExclusionZoneCntBuffer);
        values.put(COLUMN_ZON_DEFAULT_APPOINTMENT_TYPE_ID, ZonRec.defaultAppointmentTypeId);
        values.put(COLUMN_ZON_VERSION, ZonRec.zoneVersion);
        values.put(COLUMN_ZON_SCHEDULE_VERSION, ZonRec.scheduleVersionOfZone);
        values.put(COLUMN_ZON_BUFFER, ZonRec.bufferZone);
        values.put(COLUMN_ZON_IS_INTO_BUFFER, ZonRec.isIntoBufferZoneState);

        return db.insertWithOnConflict(TABLE_ZONES, COLUMN_ZON_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
    }


    @Override
    public void AddDefaultData(SQLiteDatabase db) {
    }

    private EntityZones GetRecFromQueryCursor(Cursor QueryCursor) {
        return new EntityZones(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_ZON_NAME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_OFF_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_TYPE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_SHAPE_TYPE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_ZON_POINTS_JSON_STR)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_ZON_LATITUDE)),
                QueryCursor.getDouble(QueryCursor.getColumnIndex(COLUMN_ZON_LONGITUDE)),
                QueryCursor.getFloat(QueryCursor.getColumnIndex(COLUMN_ZON_RADIUS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_SAMPLE_RATE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_ZON_NOTE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_IS_INTO)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_INC_CNT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_INC_CNT_BUFF)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_EXC_CNT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_EXC_CNT_BUFF)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_DEFAULT_APPOINTMENT_TYPE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_VERSION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_SCHEDULE_VERSION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_BUFFER)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ZON_IS_INTO_BUFFER))
        );
    }

    public List<EntityZones> getAllZones() {
        List<EntityZones> zoneRecordList = new ArrayList<EntityZones>();
        Cursor QueryCursor = databaseReference.query(TABLE_ZONES,        // Table name
                columnNamesArray,        // Columns names
                null,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                null);                // Limit...

        while (QueryCursor.moveToNext()) {
            zoneRecordList.add(GetRecFromQueryCursor(QueryCursor));
        }
        QueryCursor.close();

        return zoneRecordList;
    }

    public int deleteZoneByZoneId(long zoneId) {
        String Where = String.format("%s = %d", COLUMN_ZON_ID, zoneId);
        return databaseReference.delete(tableName, Where, null);
    }

    /**
     * @return all the zones that were deleted and haven't handled by application yet
     */
    public List<EntityZones> getAllZonesThatShouldBeDeleted() {
        List<EntityZones> zoneRecordList = new ArrayList<>();
        String Where = String.format("(%s < %d)", COLUMN_ZON_VERSION, DatabaseAccess.getInstance().tableOffStatus.Get().OffZoneVersion);
        Cursor QueryCursor = databaseReference.query(tableName,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                null);                // Limit...

        while (QueryCursor.moveToNext()) {
            zoneRecordList.add(GetRecFromQueryCursor(QueryCursor));
        }
        QueryCursor.close();

        return zoneRecordList;
    }

    public EntityZones getZoneWithDefaultScheduleAsMustBeIn() {
        List<EntityZones> zoneRecordList = getAllZones();
        for (EntityZones recordZoneItem : zoneRecordList) {
            if (recordZoneItem.defaultAppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI) {
                return recordZoneItem;
            }
        }
        return null;
    }


    public EntityZones getZoneRecordByZoneId(int zoneId) {
        EntityZones zoneRecord = null;
        String Where = String.format("(%s = %d)", COLUMN_ZON_ID, zoneId);
        Cursor QueryCursor = databaseReference.query(tableName,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...

        if (QueryCursor.getCount() > 0) {
            QueryCursor.moveToFirst();
            zoneRecord = GetRecFromQueryCursor(QueryCursor);
        }
        QueryCursor.close();

        return zoneRecord;
    }


    public Cursor getZonesCursor() {
        // Select query
        return databaseReference.query(TABLE_ZONES,            // Table name
                columnNamesArray,        // give me all Col, we can ask for some of the col..
                null,
                null,//new String [] {String.valueOf(Before), String.valueOf(After)},
                null,                // Group by...
                null,                // Having...
                COLUMN_ZON_IS_INTO + " DESC",            // Order by insert order
                null);
    }

    public EntityZones getZoneRecFromCursor(Cursor QueryCursor) {
        return GetRecFromQueryCursor(QueryCursor);
    }

    public void UpdateZonesTableIsInto(int ZoneId, int IsInto) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ZON_IS_INTO, IsInto);
        String[] zoneIdStr = new String[]{String.valueOf(ZoneId)};
        databaseReference.update(tableName, values, COLUMN_ZON_ID + " = ?", zoneIdStr);
    }

    public void UpdateZonesTableIsIntoBuffer(int ZoneId, int IsInBuffer) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ZON_IS_INTO_BUFFER, IsInBuffer);
        String[] zoneIdStr = new String[]{String.valueOf(ZoneId)};
        databaseReference.update(tableName, values, COLUMN_ZON_ID + " = ?", zoneIdStr);
    }

    public void UpdateZonesTableIncCnt(int ZoneId, int IncCnt) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ZON_INC_CNT, IncCnt);
        databaseReference.update(tableName, values, COLUMN_ZON_ID + " = ?", new String[]{String.valueOf(ZoneId)});
    }

    public void UpdateZonesTableExcCnt(int ZoneId, int ExcCnt) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ZON_EXC_CNT, ExcCnt);
        databaseReference.update(tableName, values, COLUMN_ZON_ID + " = ?", new String[]{String.valueOf(ZoneId)});
    }

    public String getZoneInfoToShow(EntityScheduleOfZones recordScheduleOfZonesItem) {
        EntityZones zoneRecordByZoneId = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(recordScheduleOfZonesItem.ZoneId);
        String zoneInfoToShow;
        if (zoneRecordByZoneId != null && !zoneRecordByZoneId.ZoneName.isEmpty()) {
            zoneInfoToShow = zoneRecordByZoneId.ZoneName;
        } else {
            zoneInfoToShow = String.valueOf(recordScheduleOfZonesItem.ZoneId);
        }
        return zoneInfoToShow;
    }

    public EntityZones getExclusionZoneWithoutCurrentAppointmentWhereOffenderInside() {
        List<EntityZones> zonesOffenderLocatedInsideList = DatabaseAccess.getInstance().tableZones.getAllZonesThatOffenderLocatedInside();
        for (EntityZones recordZoneItem : zonesOffenderLocatedInsideList) {
            if (recordZoneItem.defaultAppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO) {
                EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.
                        getCurrentScheduleOfZone(recordZoneItem.ZoneId);

                // is inside exclusion zone and no current appointment found inside
                if (currentScheduleOfZone == null) {
                    return recordZoneItem;
                }
            }
        }

        return null;
    }

    public EntityZones getExclusionZoneWithoutCurrentAppointmentWhereOffenderInsideBuffer() {
        List<EntityZones> zonesOffenderLocatedInsideList = DatabaseAccess.getInstance().tableZones.getAllZonesThatOffenderLocatedInsideBuffer();
        for (EntityZones recordZoneItem : zonesOffenderLocatedInsideList) {
            if (recordZoneItem.defaultAppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO) {
                EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.
                        getCurrentScheduleOfZone(recordZoneItem.ZoneId);

                // is inside exclusion zone and no current appointment found inside
                if (currentScheduleOfZone == null) {
                    return recordZoneItem;
                }
            }
        }

        return null;
    }

    public List<EntityZones> getAllZonesThatOffenderLocatedInside() {
        List<EntityZones> zonesThatOffenderLocatedInsideList = new ArrayList<>();
        List<EntityZones> allZones = getAllZones();

        for (EntityZones recordZoneItem : allZones) {

            //checks if offender located in regular zone
            if (recordZoneItem.isIntoExclusionZoneState == TableZonesManager.ZONE_INSIDE) {
                zonesThatOffenderLocatedInsideList.add(recordZoneItem);
            } else {

                //checks if offender located in beacon zone
                boolean isInBeaconZone = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                        (OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE) == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE;
                long beaconZoneId = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                        (OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID);
                if (recordZoneItem.ZoneId == beaconZoneId && isInBeaconZone) {
                    zonesThatOffenderLocatedInsideList.add(recordZoneItem);
                }
            }
        }
        return zonesThatOffenderLocatedInsideList;
    }

    public List<EntityZones> getAllZonesThatOffenderLocatedInsideBuffer() {
        List<EntityZones> zonesThatOffenderLocatedInsideBufferList = new ArrayList<>();
        List<EntityZones> allZones = getAllZones();

        for (EntityZones recordZoneItem : allZones) {

            //checks if offender located in regular zone
            if (recordZoneItem.isIntoBufferZoneState == TableZonesManager.ZONE_INSIDE) {
                zonesThatOffenderLocatedInsideBufferList.add(recordZoneItem);
            } else {

                //checks if offender located in beacon zone
                boolean isInBeaconZone = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                        (OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE) == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE;
                long beaconZoneId = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                        (OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ZONE_ID);
                if (recordZoneItem.ZoneId == beaconZoneId && isInBeaconZone) {
                    zonesThatOffenderLocatedInsideBufferList.add(recordZoneItem);
                }
            }
        }
        return zonesThatOffenderLocatedInsideBufferList;
    }
}
