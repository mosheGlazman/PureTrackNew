package com.supercom.puretrack.data.source.local.table;

import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_BEACON_STATUS_BATTERY;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_CURRENT_COMM_NETWORK_TEST_STATUS;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_CURRENT_PM_COM_PROFILE;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_FAILED_HANDLE_REQUESTS_LIST;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_INITIATED_FLIGHT_MODE_END;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_IS_CYCLE_FINISHED_SUCCESSFULLY;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_IS_MOBILE_DATA_ENABLED;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_IS_OFFENDER_ACTIVATED;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_LAST_OFFENDER_REQUEST_STATUS;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_LAST_SYNC_RESPONSE_FROM_SERVER_JSON;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_OFFENDER_IN_PURECOM_ZONE;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_SIM_ICCID;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_START_NETWORK_STATUS_COUNTER;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_TAG_MOTION;
import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_TAG_MOTION_INDEX;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityOffenderStatus;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.util.application.App;

import java.util.concurrent.TimeUnit;


public class TableOffenderStatus extends DatabaseTable {

    // Table name
    private static final String TABLE_OFF_STATUS = "OffenderStatus";
    // Column names
    public static final String COLUMN_OFF_ID = "OffId";
    public static final String COLUMN_OFF_TAG_ID = "OffTagId";
    public static final String COLUMN_OFF_STATUS = "OffVioStat"; // 1-Normal, 2-In Vio, 3-ALARM
    public static final String COLUMN_OFF_TAG_BATTERY_LEVEL = "OffTagBatt";
    public static final String COLUMN_OFF_IS_IN_RANGE = "OffIsInRange";
    public static final String COLUMN_OFF_IS_IN_SCHEDULE = "OffIsInSchedule";
    public static final String COLUMN_OFF_SYNC_VERSION_TYPE = "OffSyncVersionType";
    public static final String COLUMN_OFF_SYNC_VERSION_NUMBER = "OffSyncVersionNumber";
    public static final String COLUMN_OFF_TAG_STATUS_CASE = "OffTagStatCase";
    public static final String COLUMN_OFF_TAG_STATUS_STRAP = "OffTagStatStrap";
    public static final String COLUMN_OFF_TAG_STATUS_BATTERY = "OffTagStatBattery";
    public static final String COLUMN_OFF_TAG_STATUS_MOTION = "OffTagStatMotion";
    public static final String COLUMN_OFF_TIME_ZONE = "OffTimeZone";
    public static final String COLUMN_OFF_SPEED = "OffSpeed";
    public static final String COLUMN_OFF_LAST_TAG_RECEIVE = "OffLastTagRx";
    public static final String COLUMN_OFF_IN_BEACON_ZONE = "OffInBeaconZone";
    public static final String COLUMN_OFF_BEACON_STATUS_CASE = "OffBeaconStatCase";
    public static final String COLUMN_OFF_BEACON_STATUS_PROX = "OffBeaconStatProx";
    public static final String COLUMN_OFF_BEACON_STATUS_MOTION = "OffBeaconStatMotion";
    public static final String COLUMN_OFF_BEACON_STATUS_BATTERY = "offBeaconStatusBattery";
    public static final String COLUMN_OFF_BEACON_STATUS_CASE_INDEX = "OffBeaconStatCaseIndex";
    public static final String COLUMN_OFF_BEACON_STATUS_MOTION_INDEX = "OffBeaconStatMotionIndex";
    public static final String COLUMN_OFF_BEACON_STATUS_PROX_INDEX = "OffBeaconStatProxIndex";
    public static final String COLUMN_OFF_BEACON_HAS_OPEN_EVENT = "OffBeaconHasOpenEvent";
    public static final String COLUMN_OFF_TAG_STATUS_CASE_INDEX = "OffTagStatCaseIndex";
    public static final String COLUMN_OFF_TAG_STATUS_STRAP_INDEX = "OffTagStatStrapIndex";

    public static final String COLUMN_OFF_STAT_DEVICE_BATTERY_STAT = "OffDeviceBatteryStat";
    public static final String COLUMN_OFF_STAT_DEVICE_BATTERY_PERCENTAGE = "OffDeviceBatteryPercentage";
    public static final String COLUMN_OFF_LAST_GPS_POINT = "OffenderLastGPSPpoint";
    public static final String COLUMN_OFF_ZONE_VERSION = "offZoneVersion";
    public static final String COLUMN_OFF_LAST_SCHEDULE_UPDATE = "offLastScheudleIpdate";
    public static final String COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER = "offScheduleOfZonesBiometricTestsCounter";
    public static final String COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_LAST_CHECK = "offScheduleOfZonesBiometricLastCheck";
    public static final String COLUMN_OFF_DEVICE_DOWNLOADED_VERSION = "offDeviceDownloadedVersion";
    public static final String COLUMN_OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME = "offDidOffenderGetValidAuthenticationForFirstTime";
    public static final String COLUMN_OFF_IS_OFFENDER_ACTIVATED = "offIsOffenderActivated";
    public static final String COLUMN_OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD = "offSmallestAccuracyPointAndAboveGoodThreshold";
    public static final String COLUMN_OFF_LAST_CREATED_EVENT_TYPE = "offLastCreatedEventType";
    public static final String COLUMN_OFF_LAST_OFFENDER_REQUEST_ID_TREATED = "offLastOffenderRequestIdTreated";
    public static final String COLUMN_OFF_LAST_OFFENDER_REQUEST_STATUS = "offLastOffenderRequestStatus";
    public static final String COLUMN_OFF_ACTIVATION_OFFENDER_REQUEST_ID_TREATED = "offActivationOffenderRequestIdTreated";
    public static final String COLUMN_OFF_LAST_SYNC_RESPONSE_FROM_SERVER_JSON = "offLastSyncResponseFromServerJson";
    public static final String COLUMN_OFF_CURRENT_PM_COM_PROFILE = "offCurrentPmComProfile";
    public static final String COLUMN_OFF_IS_MOBILE_DATA_ENABLED = "offIsMobileDataEnabled";
    public static final String COLUMN_OFF_TAG_TX_INDEX = "OffTagTxIndex";
    public static final String COLUMN_OFF_BEACON_TX_INDEX = "OffBeaconTxIndex";

    public static final String COLUMN_OFF_CURRENT_COMM_NETWORK_TEST_STATUS = "offCurrentCommNetworkTestStatus";
    public static final String COLUMN_OFF_START_NETWORK_STATUS_COUNTER = "offStartNetworkStatusCounter";

    public static final String COLUMN_OFF_FAILED_HANDLE_REQUESTS_LIST = "offFailedHandleRequestsList";
    public static final String COLUMN_OFF_STAT_DEVICE_TEMPERATURE = "OffDeviceTemperature";
    public static final String COLUMN_OFF_BEACON_BATTERY_LEVEL = "OffBeaconBatt";
    public static final String COLUMN_OFF_LAST_BEACON_RECEIVE = "OffLastBeaconRx";

    public static final String COLUMN_OFF_IS_CYCLE_FINISHED_SUCCESSFULLY = "isCycleFinishedSuccessfuly";

    public static final String COLUMN_OFF_SIM_ICCID = "offSimICCID";
    public static final String COLUMN_OFF_TIME_INITIATED_FLIGHT_MODE_END = "offTimeInitiatedFlightModeEnd";
    public static final String COLUMN_OFF_LAST_LOCATION_UTC_TIME = "offLastLocationUtcTime";
    public static final String COLUMN_OFF_LAST_LBS_LOCATION_UTC_TIME = "offLastLbsLocationUtcTime";
    public static final String COLUMN_DEVICE_STATUS_LOCKED_ATTEMPTS = "locked_status";

    public static final String COLUMN_DEVICE_STATUS_LAST_SUCCESSFULLY_COM = "last_success_com"; // holds the time of last comunication
    public static final String COLUMN_DEVICE_STATUS_OFFENDER_IN_PURECOM_ZONE = "OffenderInPureComZone";
    public static final String COLUMN_DEVICE_STATUS_TAG_MOTION = "TagMotion";

    public static final String OFF_TAG_STAT_MOTION_INDEX = "OffTagStatMotionIndex";

    private EntityOffenderStatus OffStatusRec;    // Record for quick access


    /**
     * Constructor: update table's name, build columns
     */
    public TableOffenderStatus() {

        this.tableName = TABLE_OFF_STATUS;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_STATUS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_BATTERY_LEVEL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_IS_IN_RANGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_IS_IN_SCHEDULE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SYNC_VERSION_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SYNC_VERSION_NUMBER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_STATUS_CASE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_STATUS_STRAP, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_STATUS_BATTERY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_STATUS_MOTION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TIME_ZONE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SPEED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_TAG_RECEIVE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_IN_BEACON_ZONE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));            // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_STATUS_CASE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));        // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_STATUS_PROX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));        // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_STATUS_MOTION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));        // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_STATUS_BATTERY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));        // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_STATUS_CASE_INDEX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));    // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_STATUS_MOTION_INDEX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));    // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_STATUS_PROX_INDEX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));    // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_HAS_OPEN_EVENT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));    // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_STATUS_CASE_INDEX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));    // NEW!
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_STATUS_STRAP_INDEX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));    // NEW!

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_STAT_DEVICE_BATTERY_STAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_STAT_DEVICE_BATTERY_PERCENTAGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_GPS_POINT, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_ZONE_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_SCHEDULE_UPDATE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_LAST_CHECK, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_DEVICE_DOWNLOADED_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_IS_OFFENDER_ACTIVATED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_CREATED_EVENT_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_OFFENDER_REQUEST_ID_TREATED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_OFFENDER_REQUEST_STATUS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_ACTIVATION_OFFENDER_REQUEST_ID_TREATED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_SYNC_RESPONSE_FROM_SERVER_JSON, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_CURRENT_PM_COM_PROFILE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_IS_MOBILE_DATA_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_TX_INDEX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_TX_INDEX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_CURRENT_COMM_NETWORK_TEST_STATUS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_START_NETWORK_STATUS_COUNTER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_FAILED_HANDLE_REQUESTS_LIST, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_STAT_DEVICE_TEMPERATURE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_BATTERY_LEVEL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_BEACON_RECEIVE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_IS_CYCLE_FINISHED_SUCCESSFULLY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SIM_ICCID, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TIME_INITIATED_FLIGHT_MODE_END, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_LOCATION_UTC_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_LBS_LOCATION_UTC_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_LOCKED_ATTEMPTS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_LAST_SUCCESSFULLY_COM, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_OFFENDER_IN_PURECOM_ZONE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_DEVICE_STATUS_TAG_MOTION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFF_TAG_STAT_MOTION_INDEX, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));


        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     * Add offender details Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityOffenderStatus entityOffenderStatus = (EntityOffenderStatus) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_OFF_STATUS, entityOffenderStatus.OffVioStat);
        values.put(COLUMN_OFF_TAG_BATTERY_LEVEL, entityOffenderStatus.OffTagBatteryLevel);
        values.put(COLUMN_OFF_IS_IN_RANGE, entityOffenderStatus.OffIsInRange);
        values.put(COLUMN_OFF_TAG_STATUS_CASE, entityOffenderStatus.OffTagStatCase);
        values.put(COLUMN_OFF_TAG_STATUS_STRAP, entityOffenderStatus.OffTagStatStrap);
        values.put(COLUMN_OFF_TAG_STATUS_BATTERY, entityOffenderStatus.OffTagStatBattery);
        values.put(COLUMN_OFF_TAG_STATUS_MOTION, entityOffenderStatus.OffTagStatMotion);
        values.put(COLUMN_OFF_LAST_TAG_RECEIVE, entityOffenderStatus.OffTagLastReceive);

        values.put(COLUMN_OFF_IN_BEACON_ZONE, entityOffenderStatus.OffInBeaconZone);
        values.put(COLUMN_OFF_BEACON_STATUS_CASE, entityOffenderStatus.OffBeaconStatCase);
        values.put(COLUMN_OFF_BEACON_STATUS_PROX, entityOffenderStatus.OffBeaconStatProx);
        values.put(COLUMN_OFF_BEACON_STATUS_MOTION, entityOffenderStatus.OffBeaconStatMotion);
        values.put(COLUMN_OFF_BEACON_STATUS_BATTERY, entityOffenderStatus.OffBeaconStatBattery);
        values.put(COLUMN_OFF_BEACON_STATUS_CASE_INDEX, entityOffenderStatus.OffBeaconStatCaseIndex);
        values.put(COLUMN_OFF_BEACON_STATUS_MOTION_INDEX, entityOffenderStatus.OffBeaconStatMotionIndex);
        values.put(COLUMN_OFF_BEACON_STATUS_PROX_INDEX, entityOffenderStatus.OffBeaconStatProxIndex);
        values.put(COLUMN_OFF_BEACON_HAS_OPEN_EVENT, entityOffenderStatus.OffBeaconHasOpenEvent);
        values.put(COLUMN_OFF_TAG_STATUS_CASE_INDEX, entityOffenderStatus.OffTagStatCaseIndex);
        values.put(COLUMN_OFF_TAG_STATUS_STRAP_INDEX, entityOffenderStatus.OffTagStatStrapIndex);

        values.put(COLUMN_OFF_STAT_DEVICE_BATTERY_STAT, entityOffenderStatus.OffDeviceBatteryStat);
        values.put(COLUMN_OFF_STAT_DEVICE_BATTERY_PERCENTAGE, entityOffenderStatus.OffDeviceBatteryPercentage);

        values.put(COLUMN_OFF_LAST_GPS_POINT, entityOffenderStatus.LastGpsPointRecordJsonStr);
        values.put(COLUMN_OFF_ZONE_VERSION, entityOffenderStatus.OffZoneVersion);
        values.put(COLUMN_OFF_LAST_SCHEDULE_UPDATE, entityOffenderStatus.OffLastScheduleUpdate);

        values.put(COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER, entityOffenderStatus.offScheduleOfZonesBiometricTestsCounter);
        values.put(COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_LAST_CHECK, entityOffenderStatus.offScheduleOfZonesBiometricLastCheck);
        values.put(COLUMN_OFF_DEVICE_DOWNLOADED_VERSION, entityOffenderStatus.offDeviceDownloadedVersion);
        values.put(COLUMN_OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME, entityOffenderStatus.offDidOffenderGetValidAuthenticationForFirstTime);
        values.put(COLUMN_OFF_IS_OFFENDER_ACTIVATED, entityOffenderStatus.offIsOffenderActivated);
        values.put(COLUMN_OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD, entityOffenderStatus.offSmallestAccuracyPointAndAboveGoodThreshold);
        values.put(COLUMN_OFF_LAST_CREATED_EVENT_TYPE, entityOffenderStatus.offLastCreatedEventType);
        values.put(COLUMN_OFF_LAST_OFFENDER_REQUEST_ID_TREATED, entityOffenderStatus.offLastOffenderRequestIdStatus);
        values.put(COLUMN_OFF_LAST_OFFENDER_REQUEST_STATUS, entityOffenderStatus.offLastOffenderRequestStatus);
        values.put(COLUMN_OFF_ACTIVATION_OFFENDER_REQUEST_ID_TREATED, entityOffenderStatus.offActivationOffenderRequestIdTreated);
        values.put(COLUMN_OFF_LAST_SYNC_RESPONSE_FROM_SERVER_JSON, entityOffenderStatus.offLastSyncResponseFromServerJson);
        values.put(COLUMN_OFF_CURRENT_PM_COM_PROFILE, entityOffenderStatus.offCurrentPmComProfile);
        values.put(COLUMN_OFF_IS_MOBILE_DATA_ENABLED, entityOffenderStatus.isMobileDataEnabled);

        values.put(COLUMN_OFF_TAG_TX_INDEX, entityOffenderStatus.offTagTxIndex);
        values.put(COLUMN_OFF_BEACON_TX_INDEX, entityOffenderStatus.offBeaconTxIndex);
        values.put(COLUMN_OFF_CURRENT_COMM_NETWORK_TEST_STATUS, entityOffenderStatus.offCurrentCommNetworkTestStatus);
        values.put(COLUMN_OFF_START_NETWORK_STATUS_COUNTER, entityOffenderStatus.startNetworkStatusCounter);

        values.put(COLUMN_OFF_FAILED_HANDLE_REQUESTS_LIST, entityOffenderStatus.failedHandleRequestsJson);
        values.put(COLUMN_OFF_STAT_DEVICE_TEMPERATURE, entityOffenderStatus.OffDeviceTemperature);
        values.put(COLUMN_OFF_BEACON_BATTERY_LEVEL, entityOffenderStatus.OffBeaconBatteryLevel);
        values.put(COLUMN_OFF_LAST_BEACON_RECEIVE, entityOffenderStatus.OffBeaconLastReceive);
        values.put(COLUMN_OFF_IS_CYCLE_FINISHED_SUCCESSFULLY, entityOffenderStatus.isCycleFinishedSuccessfuly);
        values.put(COLUMN_OFF_SIM_ICCID, entityOffenderStatus.ofSimICCID);
        values.put(COLUMN_OFF_TIME_INITIATED_FLIGHT_MODE_END, entityOffenderStatus.timeInitiatedFlightModeEnd);
        values.put(COLUMN_OFF_LAST_LOCATION_UTC_TIME, entityOffenderStatus.lastLocationUtcTime);
        values.put(COLUMN_OFF_LAST_LBS_LOCATION_UTC_TIME, entityOffenderStatus.lastLocationUtcTime);
        values.put(COLUMN_DEVICE_STATUS_LOCKED_ATTEMPTS, entityOffenderStatus.locked_status);
        values.put(COLUMN_DEVICE_STATUS_LAST_SUCCESSFULLY_COM, entityOffenderStatus.locked_status);
        values.put(COLUMN_DEVICE_STATUS_OFFENDER_IN_PURECOM_ZONE, entityOffenderStatus.locked_status);
        values.put(COLUMN_DEVICE_STATUS_TAG_MOTION, entityOffenderStatus.tagMotion);
        values.put(OFF_TAG_STAT_MOTION_INDEX, entityOffenderStatus.tagStatMotionIndex);

        // Update local copy
        OffStatusRec = entityOffenderStatus;
        // Insert the record
        return database.insert(TABLE_OFF_STATUS, null, values);

    }

    /**
     * Get record from any query cursor
     */
    private EntityOffenderStatus GetRecFromQueryCursor(Cursor QueryCursor) {
        EntityOffenderStatus OffStatusRec = new EntityOffenderStatus(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_STATUS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_BATTERY_LEVEL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_IS_IN_RANGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_STATUS_CASE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_STATUS_STRAP)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_STATUS_BATTERY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_STATUS_MOTION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_TAG_RECEIVE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_IN_BEACON_ZONE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_STATUS_CASE)),        // NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_STATUS_PROX)),        // NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_STATUS_MOTION)),        // NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_STATUS_BATTERY)),        // NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_STATUS_CASE_INDEX)),// NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_STATUS_MOTION_INDEX)),// NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_STATUS_PROX_INDEX)),// NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_HAS_OPEN_EVENT)),// NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_STATUS_CASE_INDEX)),    // NEW!
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_STATUS_STRAP_INDEX)),    // NEW!

                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_STAT_DEVICE_BATTERY_STAT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_STAT_DEVICE_BATTERY_PERCENTAGE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_GPS_POINT)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_ZONE_VERSION)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_SCHEDULE_UPDATE)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_LAST_CHECK)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_DEVICE_DOWNLOADED_VERSION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_IS_OFFENDER_ACTIVATED)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_CREATED_EVENT_TYPE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_OFFENDER_REQUEST_ID_TREATED)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_OFFENDER_REQUEST_STATUS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_ACTIVATION_OFFENDER_REQUEST_ID_TREATED)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_SYNC_RESPONSE_FROM_SERVER_JSON)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_CURRENT_PM_COM_PROFILE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_IS_MOBILE_DATA_ENABLED)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_TX_INDEX)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_TX_INDEX)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_CURRENT_COMM_NETWORK_TEST_STATUS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_START_NETWORK_STATUS_COUNTER)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_FAILED_HANDLE_REQUESTS_LIST)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_STAT_DEVICE_TEMPERATURE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_BATTERY_LEVEL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_BEACON_RECEIVE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_IS_CYCLE_FINISHED_SUCCESSFULLY)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_SIM_ICCID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TIME_INITIATED_FLIGHT_MODE_END)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_LOCATION_UTC_TIME)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_LBS_LOCATION_UTC_TIME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_LOCKED_ATTEMPTS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_LAST_SUCCESSFULLY_COM)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_OFFENDER_IN_PURECOM_ZONE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_DEVICE_STATUS_TAG_MOTION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFF_TAG_STAT_MOTION_INDEX))

        );
        // Add fields which are not covered by the constructor
        // Add SQLite rowid
        OffStatusRec.SetRowId(QueryCursor.getLong(QueryCursor.getColumnIndex("rowid")));

        return OffStatusRec;
    }

    /**
     * Add default data record/s
     */
    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        addRecord(db, createDefaultValues());
    }

    /**
     * Get a record - TBD
     */
    public EntityOffenderStatus GetRecord(SQLiteDatabase db) {
        // Select query
        Cursor QueryCursor = db.query(TABLE_OFF_STATUS, columnNamesArray, null, null, null, null, null);
        // Move to 1st row

        EntityOffenderStatus OffRec;

        if (QueryCursor.getCount() == 0) {
            OffRec = createDefaultValues();
        } else {
            QueryCursor.moveToFirst();
            OffRec = GetRecFromQueryCursor(QueryCursor);
        }

        QueryCursor.close();

        return OffRec;
    }

    private EntityOffenderStatus createDefaultValues() {
        return new EntityOffenderStatus(1, 0, 1, 0, 0, 0, 0, 0,
                0, 0, 0, 0, DEFAULT_BEACON_STATUS_BATTERY, -1, -1,
                -1, 0, -1, -1, 0, 0, "",
                0, System.currentTimeMillis(), 0, 0, App.getInstalledVersionNumber(),
                DEFAULT_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME, DEFAULT_IS_OFFENDER_ACTIVATED, "", 0,
                0, DEFAULT_LAST_OFFENDER_REQUEST_STATUS, 0, DEFAULT_LAST_SYNC_RESPONSE_FROM_SERVER_JSON,
                DEFAULT_CURRENT_PM_COM_PROFILE, DEFAULT_IS_MOBILE_DATA_ENABLED, -1, -1, DEFAULT_CURRENT_COMM_NETWORK_TEST_STATUS,
                DEFAULT_START_NETWORK_STATUS_COUNTER, DEFAULT_FAILED_HANDLE_REQUESTS_LIST, 0, 0, 0,
                DEFAULT_IS_CYCLE_FINISHED_SUCCESSFULLY, DEFAULT_SIM_ICCID, DEFAULT_INITIATED_FLIGHT_MODE_END, 0, 0, 0,
                0, DEFAULT_OFFENDER_IN_PURECOM_ZONE, DEFAULT_TAG_MOTION, DEFAULT_TAG_MOTION_INDEX);
    }

    /**
     * Load data to local copy
     */
    @Override
    public void LoadData(SQLiteDatabase db) {
        OffStatusRec = this.GetRecord(db);
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {

        switch (oldVersion) {
            case 170:
            case 171:
            case 172:
            case 173:
            case 177:
                database.execSQL("alter table " + tableName + " add column " + COLUMN_DEVICE_STATUS_OFFENDER_IN_PURECOM_ZONE +
                        " integer default " + DEFAULT_OFFENDER_IN_PURECOM_ZONE);
                break;
            case 178:
                database.execSQL("alter table " + tableName + " add column " + COLUMN_DEVICE_STATUS_TAG_MOTION +
                        " integer default " + DEFAULT_TAG_MOTION);
                break;
            case 181:
            case 182:
                database.execSQL("alter table " + tableName + " add column " + OFF_TAG_STAT_MOTION_INDEX +
                        " integer default " + DEFAULT_TAG_MOTION_INDEX);
                break;
        }
    }


    /**
     * Load data to local copy
     */
    public EntityOffenderStatus Get() {
        return OffStatusRec;
    }

    public boolean shouldStartScheduleCycle() {

        long currentTime = System.currentTimeMillis();

        long lastTimeWeDidScheudleCycle = OffStatusRec.OffLastScheduleUpdate;

        int timeToDoScheduleCycle = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderConfigSchedExpiree;
        long timeToDoScheduleCycleInMills = TimeUnit.HOURS.toMillis(timeToDoScheduleCycle);

        return ((currentTime - lastTimeWeDidScheudleCycle) > timeToDoScheduleCycleInMills) && timeToDoScheduleCycleInMills > 0;
    }
}
