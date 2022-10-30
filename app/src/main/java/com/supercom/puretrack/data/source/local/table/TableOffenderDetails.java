package com.supercom.puretrack.data.source.local.table;

import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderDetailsValues.*;
import static com.supercom.puretrack.util.constants.network.ServerUrls.SERVER_URL_AES_KEY_BYTES;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityOffenderDetails;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.util.constants.network.ServerUrls;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.encryption.AESUtils;
import com.supercom.puretrack.util.encryption.ScramblingTextUtils;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;


public class TableOffenderDetails extends DatabaseTable {



    public interface OffenderBeaconZoneStatus {
        int INSIDE_PURECOM_ZONE = 2;
        int INSIDE_BEACON_ZONE = 1;
        int OUTSIDE_BEACON_ZONE = 0;
    }

    public static final int DEFAULT_CUSTOM_CALL_INTERFACE = 0;

    // Table name
    public static final String TABLE_OFF_DETAILS = "OffenderDetails";
    // Column names
    public static final String COLUMN_OFF_ID = "Id";
    public static final String COLUMN_OFF_SN = "SN";
    public static final String COLUMN_OFF_TAG_ID = "TagId"; //not used
    public static final String COLUMN_OFF_TAG_RF_ID = "TagRfId";
    public static final String COLUMN_OFF_TAG_ENCRYPTION = "TagEncryption";
    public static final String COLUMN_OFF_PROG_START = "ProgStart"; //not used
    public static final String COLUMN_OFF_PROG_END = "ProgEnd"; //not used
    public static final String COLUMN_OFF_PROXIMITY_RANGE = "ProxRange"; //not used
    public static final String COLUMN_OFF_SCHEDULE_GRACE = "SchedGrace";
    public static final String COLUMN_OFF_ADDRESS = "HomeAddress";
    public static final String COLUMN_OFF_FIRST_NAME = "FirstName";
    public static final String COLUMN_OFF_MID_NAME = "MidName"; //not used
    public static final String COLUMN_OFF_LAST_NAME = "LastName";
    public static final String COLUMN_OFF_PICTURE_PATH = "PicPath";
    public static final String COLUMN_OFF_PRIMARY_PHONE = "PrimaryPhone";
    public static final String COLUMN_OFF_SECONDARY_PHONE = "SecondaryPhone";

    public static final String COLUMN_OFF_OFFICER_NAME = "OfficerName";
    public static final String COLUMN_OFF_AGENCY_NAME = "AgencyName";
    public static final String COLUMN_OFF_BEACON_NAME = "BeaconName";
    public static final String COLUMN_OFF_BEACON_ID = "BeaconId";
    public static final String COLUMN_OFF_BEACON_ZONE_ID = "BeaconZoneId";
    public static final String COLUMN_OFF_BEACON_ZONE_VERSION = "BeaconZoneVersion";
    public static final String COLUMN_OFF_BEACON_ENCRYPTION = "BeaconEncryption";
    public static final String COLUMN_OFF_BEACON_RANGE = "BeaconRange";

    public static final String COLUMN_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI = "DeviceElapsedRealTimeInMilli";
    public static final String COLUMN_OFF_BIOMETRIC_TIMEOUT = "BiometricTimeout";
    public static final String COLUMN_OFF_BIOMETRIC_MIN_BETWEEN = "BiometricMinBetween";

    public static final String COLUMN_OFF_DEVICE_CONFIG_VER_NUMBER = "DeviceConfigVersionNumber";
    public static final String COLUMN_OFF_CONFIG_VER_NUMBER = "ConfigVersionNumber";
    public static final String COLUMN_OFF_MESSAGE_TIMEOUT = "MessageTimeout";
    public static final String COLUMN_OFF_MESSAGE_EXPIRE = "MessageExpire";
    public static final String COLUMN_OFF_ENABLE_MESSAGE_RESPONSE = "EnableMessageResponse";
    public static final String COLUMN_OFF_OFFICER_NUM_ON = "OfficerNumOn";
    public static final String COLUMN_OFF_AGENCY_NUM_ON = "AgencyNumOn";
    public static final String COLUMN_OFF_TAG_TYPE = "TagType";
    public static final String COLUMN_OFF_GOOD_POINT_THRESHOLD = "GoodPointThreshold";
    public static final String COLUMN_OFF_BAD_POINT_THRESHOLD = "BadPointThreshold";
    public static final String COLUMN_OFF_ENROLLMENT_SCREEN_WIZARD = "enrollmentScreenWizard";

    public static final String DEVICE_CONFIG_DEVELOPER_MODE_ENABLE = "developerModeEnable";
    public static final String DEVICE_CONFIG_SERVER_URL = "ServerURL";
    public static final String DEVICE_CONFIG_SERVER_PASS = "ServerPass";
    public static final String DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL = "NetCycleInterval";
    public static final String DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_INSIDE_BEACON = "NetCycleIntervalInsideBeacon"; //not used
    public static final String DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW = "NetCycleIntervalLow";
    public static final String DEVICE_CONFIG_PHONE_EMERGENCY = "PhoneEmergency";
    public static final String DEVICE_CONFIG_TIME_ZONE = "TimeZone";
    public static final String DEVICE_CONFIG_DST_OFFSET = "DstOffset";
    public static final String DEVICE_CONFIG_FUTURE_DST = "FutureDst";
    public static final String DEVICE_CONFIG_USE_CLIENT_CERT = "UseClientCert";
    public static final String DEVICE_CONFIG_PINCODE_ENABLE = "PincodeEnable";
    public static final String DEVICE_CONFIG_PINCODE_ATTEMPTS = "PincodeAttempts";
    public static final String DEVICE_CONFIG_PINCODE_PIN = "PincodePin";
    public static final String DEVICE_CONFIG_PINCODE_LOCKTIME = "PincodeLocktime";
    public static final String DEVICE_CONFIG_GUEST_TAG_ENABLED = "guestTagEnabled";
    public static final String DEVICE_CONFIG_GUEST_TAG_TIME = "guestTagTime";

    public static final String DEVICE_CONFIG_FACTORY_RESET_ENABLE = "FactoryResetEnable";
    public static final String DEVICE_CONFIG_FACTORY_RESET_TIMEOUT = "FactoryResetTimeout";

    public static final String DEVICE_CONFIG_SCHEDULE_SETTINGS_NUMBER_OF_DAYS = "ScheduleSettingsNumOfDays";
    public static final String DEVICE_CONFIG_GPS_CYCLE_INTERVAL = "GpsCycleInterval";
    public static final String DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL = "GpsCycleBeaconInterval";
    public static final String DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER = "BadGpsAccuracyCounter";

    public static final String OFFENDER_CONFIG_INCOMING_CALLS_WHITE_LIST = "IncomingCallsWhiteList";
    public static final String OFFENDER_CONFIG_APPS_LIST = "AppsList";
    public static final String OFFENDER_CONFIG_PHONE_OFFICER = "PhoneOfficer";
    public static final String OFFENDER_CONFIG_PHONE_AGENCY = "PhoneAgency";
    public static final String OFFENDER_CONFIG_RSSI_HOME_RANGE = "RSSIHomeRange";
    public static final String OFFENDER_CONFIG_RSSI_OUTSIDE_RANGE = "RSSIOutRange";
    public static final String OFFENDER_CONFIG_SCHEDULE_EXPIRE = "SchedExpiree";
    public static final String OFFENDER_CONFIG_IS_CONFIG_REQ_HANDLE_SUCCESS = "IsConfigReqHandleSuccess";
    public static final String OFFENDER_CONFIG_TIME_SENSITIVITY_INSIDE_BEACON = "TimeSensitivityInsideBeacon";
    public static final String OFFENDER_CONFIG_TIME_SENSITIVITY_OUTSIDE_BEACON = "TimeSensitivityOutsideBeacon";
    public static final String OFFENDER_CONFIG_TAG_PROXIMITY_GRACE_TIME = "TagProximityGraceTime";
    public static final String OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME = "BeaconProximityGraceTime";
    public static final String OFFENDER_CONFIG_PHONES_ACTIVE = "phoneActive";
    public static final String OFFENDER_CONFIG_ALLOWED_SPEED = "allowedSpeed";
    public static final String OFFENDER_CONFIG_LOCATION_VALIDITY = "LocationValidity";
    public static final String OFFENDER_CONFIG_SATELLITE_NUMBER = "SatelliteNumber";

    public static final String OFFENDER_CONFIG_TAG_SETTINGS_TX_INTERVAL = "TagSettingsTxinterval";
    public static final String OFFENDER_CONFIG_TAG_SETTINGS_IR_ON = "TagSettingsIrOn";
    public static final String OFFENDER_CONFIG_TAG_SETTINGS_IR_OFF = "TagSettingsIrOff";
    public static final String OFFENDER_CONFIG_TAG_SETTINGS_CASE_OPEN = "TagSettingsCaseOpen";
    public static final String OFFENDER_CONFIG_TAG_SETTINGS_LOW_BATTERY = "TagSettingsLowBatt";

    public static final String OFFENDER_CONFIG_IS_TAG_HEARTBEAT_ENABLED = "IsTagHeartBeatEnabled";
    public static final String OFFENDER_CONFIG_TAG_ADDRESS = "TagAddress";
    public static final String OFFENDER_CONFIG_BEACON_ADDRESS = "beaconAddress";
    public static final String OFFENDER_CONFIG_TAG_IS_USING_CONNECT_AS_HEARTBEAT = "IsUsingConnectAsHeartbeat";
    public static final String OFFENDER_CONFIG_TAG_HB_COUNTER = "tagHbCounter";
    public static final String OFFENDER_CONFIG_TAG_ADV_COUNTER = "tagADVCounter";
    public static final String OFFENDER_CONFIG_TAG_HB_INTERVAL = "tagHBInterval";
    public static final String OFFENDER_CONFIG_TAG_HB_VIBRATE_TIMEOUT = "TagHeartBeatTimeoutToVibrate";
    public static final String OFFENDER_CONFIG_TAG_HB_ENABLE_FROM_SERVER = "tagHBEnableFromServer";
    public static final String OFFENDER_CONFIG_TAG_CONFIGURATIONS = "tagConfigurations";
    public static final String OFFENDER_ACCELEROMETER_SETTINGS = "accelerometerSettings";


    public static final String OFFENDER_CONFIG_BLE_DEBUG_INFO_ENABLE = "bleDebugInfoEnable";
    public static final String OFFENDER_IS_BATTERY_INDICATION_ENABLED = "offenderIsBatteryIndicationEnabled";
    public static final String OFFENDER_IS_DEVICE_CASE_TAMPER_ENABLED = "offenderIsDeviceCaseTamperEenabled";

    public static final String LAUNCHER_CONFIG_SETTINGS_PASSWORD = "LauncherSettingsPassword";
    public static final String DEVICE_CONFIG_DEVICE_LOCK = "DeviceLock";

    public static final String OFFENDER_CONFIG_EVENTS_ALARMS = "EventsAlarms";
    public static final String OFFENDER_CONFIG_LOCATION_TYPES = "LocationTypes";

    public static final String OFFENDER_CONFIG_PM_COM_PROFILE = "PMComProfiles";
    public static final String OFFENDER_CONFIG_PROFILE_EVENTS = "ProfileEvents";

    @Override
    public int GetColumnNum() {
        return super.GetColumnNum();
    }

    public static final String OFFENDER_CONFIG_HOME_ADDRESS_SETTINGS = "HomeAddressSettings";
    public static final String OFFENDER_CONFIG_HOME_LAT = "HomeLat";
    public static final String OFFENDER_CONFIG_HOME_LONG = "HomeLong";

    public static final String OFFENDER_CONFIG_DIALER_BLOCKER = "DialerBlocker";
    public static final String OFFENDER_CONFIG_IS_EMERGENCY_ENABLED = "IsEmergencyEnabled";
    public static final String DEVICE_CONFIG_CELLULAR_APN = "CellularApn";
    public static final String OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_VALIDITY = "offenderDeviceCaseTamperValidity";
    public static final String OFFENDER_CONFIG_PURE_TRACK_CASE_CLOSED_THRESHOLD = "offenderDeviceCaseClosedThreshold";
    public static final String OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_CALIBRATION = "offenderDeviceCaseTamperCalibration";
    public static final String OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_X_MAGNET_THRESHOLD = "offenderDeviceCaseTamperXMagnetThreshold";
    public static final String OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Y_MAGNET_THRESHOLD = "offenderDeviceCaseTamperYMagnetThreshold";
    public static final String OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Z_MAGNET_THRESHOLD = "offenderDeviceCaseTamperZMagnetThreshold";
    public static final String OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_ENABLED = "offenderDeviceCaseTamperRecalibrationEnabled";
    public static final String OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_TIMER_IN_MINUTES = "offenderDeviceCaseTamperRecalibrationTimerInMinutes";

    public static final String OFFENDER_CONFIG_COMM_NETWORK_TEST = "commNetworkTest";

    public static final String OFFENDER_CONFIG_KNOX_SETTINGS = "KNOXSettings";

    public static final String OFFENDER_CONFIG_LOCATION_WEIGHTED_AVERAGE = "LocationWeightedAverage";
    public static final String OFFENDER_CONFIG_LOCATION_SMOOTHING = "LocationSmoothing";
    public static final String OFFENDER_CONFIG_LOCATION_SMOOTHING_ACTIVATION = "LocationSmoothingActivation";

    public static final String OFFENDER_CONFIG_LOCATION_SERVICE_INTERVAL = "LocationServiceInterval";
    public static final String OFFENDER_CONFIG_LOCATION_AVERAGE_TIME_FRAME = "LocationAverageTimeFrame";
    public static final String OFFENDER_CONFIG_LOCATION_SERVICE_CALC_TYPE = "LocationServiceCalcType";// 1=best location, 2=average location

    public static final String OFFENDER_CONFIG_BACKGROUND_APP_WHITE_LIST = "backgroundAppWhiteList";
    public static final String OFFENDER_CONFIG_OFFICER_MODE_TIMEOUT = "officerModeTimeout";
    public static final String OFFENDER_CONFIG_VOIP_SETTINGS = "VoipSettings";

    public static final String OFFENDER_CONFIG_APP_LANGUAGE = "AppLanguage";
    public static final String OFFENDER_CONFIG_CUSTOM_CALL_INTERFACE = "customCalIinterface";
    public static final String OFFENDER_CONFIG_IGNORE_SSL_CERT = "ignoreSslCert";

    public static final String OFFENDER_CONFIG_BATTERY_THRESHOLD = "batteryThreshold";

    public static final String OFFENDER_CONFIG_LBS_ENABLE = "LbsEnable";
    public static final String OFFENDER_CONFIG_START_LBS_THRESHOLD_NORMAL = "LbsThresholdNormal";
    public static final String OFFENDER_CONFIG_LBS_INTERVAL_NORMAL = "LbsIntervalNormal";
    public static final String OFFENDER_CONFIG_START_LBS_THRESHOLD_IN_VIOLATION = "LbsThresholdViolation";
    public static final String OFFENDER_CONFIG_LBS_INTERVAL_IN_VIOLATION = "LbsIntervalViolation";
    public static final String OFFENDER_CONFIG_LBS_STOP_VALIDITY = "LbsStopValidity";

    public static final String OFFENDER_CONFIG_MAP_URL = "OffenderMapUrl";
    public static final String OFFENDER_CONFIG_DEBUG_INFO = "DebugInfoConfig";
    public static final String OFFENDER_CONFIG_DEVICE_INFO_CYCLES = "DeviceInfoCycles";
    public static final String OFFENDER_CONFIG_TURN_ON_SCREEN_MOTION = "TurnOnScreen";
    public static final String EVENTS_ALLOWED_WHILE_IN_SUSPEND_SCHEDULE = "EventsAllowedWhileInSuspend";
    public static final String OFFENDER_CONFIG_GPS_INT_NO_MOTION = "GpsIntervalNoMotion";

    public static final String OFF_CONFIG_ZONE_DRIFT_ENABLE = "ZoneDriftEnable";
    public static final String OFF_ZONE_DRIFT_GPS_INT = "ZoneDriftInterval";
    public static final String OFF_CONFIG_ZONE_DRIFT_DURATION = "ZoneDriftDuration";
    public static final String OFF_CONFIG_ZONE_DRIFT_LOCATIONS = "ZoneDriftLocations";

    public static final String DEVICE_CONFIG_PURECOM_AS_HOME_UNIT = "PureComAsHomeUnit";


    private EntityOffenderDetails OffDetailsRec;    // Record for quick access

    /**
     * Constructor: update table's name, build columns
     */
    public TableOffenderDetails() {

        this.tableName = TABLE_OFF_DETAILS;
        this.columnNumber = 0;
        // Add columns
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SN, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_RF_ID, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_ENCRYPTION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_PROG_START, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_PROG_END, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_PROXIMITY_RANGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SCHEDULE_GRACE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_ADDRESS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_FIRST_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_MID_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_LAST_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_PICTURE_PATH, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_PRIMARY_PHONE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_SECONDARY_PHONE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_OFFICER_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_AGENCY_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_NAME, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_ID, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_ZONE_ID, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_ZONE_VERSION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_ENCRYPTION, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BEACON_RANGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_GOOD_POINT_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BAD_POINT_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_ENROLLMENT_SCREEN_WIZARD, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        this.AddColumn(new DatabaseColumn(COLUMN_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BIOMETRIC_TIMEOUT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_BIOMETRIC_MIN_BETWEEN, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_DEVICE_CONFIG_VER_NUMBER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_CONFIG_VER_NUMBER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_MESSAGE_TIMEOUT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_MESSAGE_EXPIRE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_ENABLE_MESSAGE_RESPONSE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_OFFICER_NUM_ON, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_AGENCY_NUM_ON, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_OFF_TAG_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_DEVELOPER_MODE_ENABLE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_SERVER_URL, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_SERVER_PASS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_INSIDE_BEACON, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_PHONE_EMERGENCY, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_TIME_ZONE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_DST_OFFSET, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_FUTURE_DST, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_USE_CLIENT_CERT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_PINCODE_ENABLE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_PINCODE_ATTEMPTS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_PINCODE_PIN, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_PINCODE_LOCKTIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_FACTORY_RESET_ENABLE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_FACTORY_RESET_TIMEOUT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_SCHEDULE_SETTINGS_NUMBER_OF_DAYS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_GPS_CYCLE_INTERVAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_INCOMING_CALLS_WHITE_LIST, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_APPS_LIST, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PHONE_OFFICER, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PHONE_AGENCY, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_RSSI_HOME_RANGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_RSSI_OUTSIDE_RANGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_SCHEDULE_EXPIRE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_IS_CONFIG_REQ_HANDLE_SUCCESS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TIME_SENSITIVITY_INSIDE_BEACON, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TIME_SENSITIVITY_OUTSIDE_BEACON, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_PROXIMITY_GRACE_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PHONES_ACTIVE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_ALLOWED_SPEED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LOCATION_VALIDITY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_SATELLITE_NUMBER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_SETTINGS_TX_INTERVAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_SETTINGS_IR_ON, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_SETTINGS_IR_OFF, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_SETTINGS_CASE_OPEN, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_SETTINGS_LOW_BATTERY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_IS_TAG_HEARTBEAT_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_ADDRESS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_BEACON_ADDRESS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_IS_USING_CONNECT_AS_HEARTBEAT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_HB_COUNTER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_ADV_COUNTER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_HB_INTERVAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_HB_VIBRATE_TIMEOUT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_HB_ENABLE_FROM_SERVER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TAG_CONFIGURATIONS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_BLE_DEBUG_INFO_ENABLE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_IS_BATTERY_INDICATION_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_IS_DEVICE_CASE_TAMPER_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(LAUNCHER_CONFIG_SETTINGS_PASSWORD, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_DEVICE_LOCK, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_EVENTS_ALARMS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LOCATION_TYPES, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PM_COM_PROFILE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PROFILE_EVENTS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_HOME_ADDRESS_SETTINGS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_HOME_LAT, EnumDatabaseColumnType.COLUMN_TYPE_REAL));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_HOME_LONG, EnumDatabaseColumnType.COLUMN_TYPE_REAL));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_DIALER_BLOCKER, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_IS_EMERGENCY_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_CELLULAR_APN, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_VALIDITY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_CALIBRATION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PURE_TRACK_CASE_CLOSED_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_X_MAGNET_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Y_MAGNET_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Z_MAGNET_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_GUEST_TAG_ENABLED, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_GUEST_TAG_TIME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_TIMER_IN_MINUTES, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_COMM_NETWORK_TEST, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_KNOX_SETTINGS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LOCATION_WEIGHTED_AVERAGE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LOCATION_SMOOTHING, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LOCATION_SMOOTHING_ACTIVATION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LOCATION_SERVICE_INTERVAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LOCATION_AVERAGE_TIME_FRAME, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LOCATION_SERVICE_CALC_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_BACKGROUND_APP_WHITE_LIST, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_OFFICER_MODE_TIMEOUT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_VOIP_SETTINGS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_APP_LANGUAGE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_CUSTOM_CALL_INTERFACE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_IGNORE_SSL_CERT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_BATTERY_THRESHOLD, EnumDatabaseColumnType.COLUMN_TYPE_STRING));

        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LBS_ENABLE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_START_LBS_THRESHOLD_NORMAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_START_LBS_THRESHOLD_IN_VIOLATION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LBS_INTERVAL_NORMAL, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LBS_INTERVAL_IN_VIOLATION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_LBS_STOP_VALIDITY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_MAP_URL, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_DEBUG_INFO, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_DEVICE_INFO_CYCLES, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_TURN_ON_SCREEN_MOTION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(EVENTS_ALLOWED_WHILE_IN_SUSPEND_SCHEDULE, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_ACCELEROMETER_SETTINGS, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(OFFENDER_CONFIG_GPS_INT_NO_MOTION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(OFF_CONFIG_ZONE_DRIFT_ENABLE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFF_ZONE_DRIFT_GPS_INT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFF_CONFIG_ZONE_DRIFT_DURATION, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(OFF_CONFIG_ZONE_DRIFT_LOCATIONS, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        this.AddColumn(new DatabaseColumn(DEVICE_CONFIG_PURECOM_AS_HOME_UNIT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));


        // Build a string array of column names (useful for some queries)
        BuildColumnNameArray();
    }

    /**
     * Add offender details Record
     */
    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityOffenderDetails OffRec = (EntityOffenderDetails) databaseEntity;

        ContentValues values = new ContentValues();

        values.put(COLUMN_OFF_ID, OffRec.offenderId);
        values.put(COLUMN_OFF_SN, OffRec.offenderSerialNumber);
        values.put(COLUMN_OFF_TAG_ID, OffRec.tagId);
        values.put(COLUMN_OFF_TAG_RF_ID, OffRec.tagRfId);
        values.put(COLUMN_OFF_TAG_ENCRYPTION, OffRec.tagEncryption);
        values.put(COLUMN_OFF_PROG_START, OffRec.programStart);
        values.put(COLUMN_OFF_PROG_END, OffRec.programEnd);
        values.put(COLUMN_OFF_PROXIMITY_RANGE, OffRec.proximityRange);
        values.put(COLUMN_OFF_SCHEDULE_GRACE, OffRec.scheduleGrace);
        values.put(COLUMN_OFF_ADDRESS, OffRec.homeAddress);
        values.put(COLUMN_OFF_FIRST_NAME, OffRec.firstName);
        values.put(COLUMN_OFF_MID_NAME, OffRec.middleName);
        values.put(COLUMN_OFF_LAST_NAME, OffRec.lastName);
        values.put(COLUMN_OFF_PICTURE_PATH, OffRec.picPath);
        values.put(COLUMN_OFF_PRIMARY_PHONE, OffRec.primaryPhone);
        values.put(COLUMN_OFF_SECONDARY_PHONE, OffRec.secondaryPhone);

        values.put(COLUMN_OFF_OFFICER_NAME, OffRec.officerName);
        values.put(COLUMN_OFF_AGENCY_NAME, OffRec.agencyName);

        values.put(COLUMN_OFF_BEACON_NAME, OffRec.beaconName);
        values.put(COLUMN_OFF_BEACON_ID, OffRec.beaconId);
        values.put(COLUMN_OFF_BEACON_ZONE_ID, OffRec.beaconZoneId);
        values.put(COLUMN_OFF_BEACON_ZONE_VERSION, OffRec.beaconZoneVersion);

        values.put(COLUMN_OFF_BEACON_ENCRYPTION, OffRec.beaconEncryption);
        values.put(COLUMN_OFF_BEACON_RANGE, OffRec.beaconRange);
        values.put(COLUMN_OFF_GOOD_POINT_THRESHOLD, OffRec.locationGoodPointThreshold);
        values.put(COLUMN_OFF_BAD_POINT_THRESHOLD, OffRec.locationBadPointThreshold);
        values.put(COLUMN_OFF_ENROLLMENT_SCREEN_WIZARD, OffRec.enrollmentScreenWizard);

        values.put(COLUMN_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI, OffRec.deviceElapsedRealTimeInMilli);
        values.put(COLUMN_OFF_BIOMETRIC_TIMEOUT, OffRec.offenderBiometricTimeout);
        values.put(COLUMN_OFF_BIOMETRIC_MIN_BETWEEN, OffRec.offenderBiometricMinBetween);
        values.put(COLUMN_OFF_DEVICE_CONFIG_VER_NUMBER, OffRec.offenderDeviceConfigVersionNumber);
        values.put(COLUMN_OFF_CONFIG_VER_NUMBER, OffRec.offenderConfigVersionNumber);
        values.put(COLUMN_OFF_MESSAGE_TIMEOUT, OffRec.offenderMessageTimeout);
        values.put(COLUMN_OFF_MESSAGE_EXPIRE, OffRec.offenderMessageExpire);
        values.put(COLUMN_OFF_ENABLE_MESSAGE_RESPONSE, OffRec.offenderMessageExpire);
        values.put(COLUMN_OFF_OFFICER_NUM_ON, OffRec.OffOfficerNumON);
        values.put(COLUMN_OFF_AGENCY_NUM_ON, OffRec.OffAgencyNumON);
        values.put(COLUMN_OFF_TAG_TYPE, OffRec.OffTagType);

        values.put(DEVICE_CONFIG_DEVELOPER_MODE_ENABLE, OffRec.OffDeveloperModeEnable);
        values.put(DEVICE_CONFIG_SERVER_URL, OffRec.ServerUrl);
        values.put(DEVICE_CONFIG_SERVER_PASS, OffRec.ServerPass);
        values.put(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL, OffRec.DeviceConfigNetCycleInterval);
        values.put(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_INSIDE_BEACON, OffRec.DeviceConfigNetCycleIntervalInsideBeacon);
        values.put(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW, OffRec.DeviceConfigNetCycleIntervalLow);
        values.put(DEVICE_CONFIG_PHONE_EMERGENCY, OffRec.DeviceConfigPhoneEmergency);
        values.put(DEVICE_CONFIG_TIME_ZONE, OffRec.DeviceConfigTimeZone);
        values.put(DEVICE_CONFIG_DST_OFFSET, OffRec.DeviceConfigDSTOffset);
        values.put(DEVICE_CONFIG_FUTURE_DST, OffRec.DeviceConfigFutureDST);
        values.put(DEVICE_CONFIG_USE_CLIENT_CERT, OffRec.UserClientCert);
        values.put(DEVICE_CONFIG_PINCODE_ENABLE, OffRec.DeviceConfigPincodeEnable);
        values.put(DEVICE_CONFIG_PINCODE_ATTEMPTS, OffRec.DeviceConfigPincodeAttempts);
        values.put(DEVICE_CONFIG_PINCODE_PIN, OffRec.DeviceConfigPincodePin);
        values.put(DEVICE_CONFIG_PINCODE_LOCKTIME, OffRec.DeviceConfigPincodeLockTime);
        values.put(DEVICE_CONFIG_FACTORY_RESET_ENABLE, OffRec.DeviceConfigFactoryReserEnable);
        values.put(DEVICE_CONFIG_FACTORY_RESET_TIMEOUT, OffRec.DeviceConfigFactoryResetTimeout);
        values.put(DEVICE_CONFIG_SCHEDULE_SETTINGS_NUMBER_OF_DAYS, OffRec.DeviceConfigScheduleSettingsNumberOfDays);

        values.put(DEVICE_CONFIG_GPS_CYCLE_INTERVAL, OffRec.DeviceConfigGpsCycleInterval);
        values.put(DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL, OffRec.DeviceConfigGpsCycleBeaconInterval);
        values.put(DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER, OffRec.DeviceConfigBadGpsAccuracyCounter);

        values.put(OFFENDER_CONFIG_INCOMING_CALLS_WHITE_LIST, OffRec.DeviceConfigIncomingCallsWhiteList);
        values.put(OFFENDER_CONFIG_APPS_LIST, OffRec.DeviceConfigAppsList);
        values.put(OFFENDER_CONFIG_PHONE_OFFICER, OffRec.DeviceConfigPhoneOfficer);
        values.put(OFFENDER_CONFIG_PHONE_AGENCY, OffRec.DeviceConfigPhoneAgency);
        values.put(OFFENDER_CONFIG_RSSI_HOME_RANGE, OffRec.OffenderConfigRssiHomeRange);
        values.put(OFFENDER_CONFIG_RSSI_OUTSIDE_RANGE, OffRec.OffenderConfigRssiOutsideRange);
        values.put(OFFENDER_CONFIG_SCHEDULE_EXPIRE, OffRec.offenderConfigSchedExpiree);
        values.put(OFFENDER_CONFIG_IS_CONFIG_REQ_HANDLE_SUCCESS, OffRec.offenderConfigIsReqHandleSuccess);
        values.put(OFFENDER_CONFIG_TIME_SENSITIVITY_INSIDE_BEACON, OffRec.offenderConfigTimeSensitivityInsideBeacon);
        values.put(OFFENDER_CONFIG_TIME_SENSITIVITY_OUTSIDE_BEACON, OffRec.offenderConfigTimeSensitivityOutsideBeacon);
        values.put(OFFENDER_CONFIG_TAG_PROXIMITY_GRACE_TIME, OffRec.offenderConfigTagProximityGraceTime);
        values.put(OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME, OffRec.offenderConfigBeaconOutsideRangeGraceTime);
        values.put(OFFENDER_CONFIG_PHONES_ACTIVE, OffRec.offenderConfigPhonesActive);
        values.put(OFFENDER_CONFIG_ALLOWED_SPEED, OffRec.offenderConfigAllowedSpeed);
        values.put(OFFENDER_CONFIG_LOCATION_VALIDITY, OffRec.offenderConfigLocationValidity);
        values.put(OFFENDER_CONFIG_SATELLITE_NUMBER, OffRec.offenderConfigSatelliteNumber);

        values.put(OFFENDER_CONFIG_TAG_SETTINGS_TX_INTERVAL, OffRec.offenderConfigTagSettingsTxinterval);
        values.put(OFFENDER_CONFIG_TAG_SETTINGS_IR_ON, OffRec.offenderConfigTagSettingsIrOn);
        values.put(OFFENDER_CONFIG_TAG_SETTINGS_IR_OFF, OffRec.offenderConfigTagSettingsIrOff);
        values.put(OFFENDER_CONFIG_TAG_SETTINGS_CASE_OPEN, OffRec.offenderConfigTagSettingsCaseOpen);
        values.put(OFFENDER_CONFIG_TAG_SETTINGS_LOW_BATTERY, OffRec.offenderConfigTagSettingsLowBatt);
        values.put(OFFENDER_CONFIG_IS_TAG_HEARTBEAT_ENABLED, OffRec.offenderConfigIsTagHeartBeatEnabled);
        values.put(OFFENDER_CONFIG_TAG_ADDRESS, OffRec.offenderConfigTagAddress);
        values.put(OFFENDER_CONFIG_BEACON_ADDRESS, OffRec.offenderConfigBeaconAddress);
        values.put(OFFENDER_CONFIG_TAG_IS_USING_CONNECT_AS_HEARTBEAT, OffRec.offIsUsingConnectAsHeartbeat);
        values.put(OFFENDER_CONFIG_TAG_HB_COUNTER, OffRec.offenderConfigTagHbCounter);
        values.put(OFFENDER_CONFIG_TAG_ADV_COUNTER, OffRec.offenderConfigTagADVCounter);
        values.put(OFFENDER_CONFIG_TAG_HB_INTERVAL, OffRec.offenderConfigTagHbInterval);
        values.put(OFFENDER_CONFIG_TAG_HB_VIBRATE_TIMEOUT, OffRec.offenderConfigTagHBVibrationTimeOut);
        values.put(OFFENDER_CONFIG_TAG_HB_ENABLE_FROM_SERVER, OffRec.offenderConfigTagHbEnableFromServer);
        values.put(OFFENDER_CONFIG_TAG_CONFIGURATIONS, OffRec.offenderConfigTagConfigurations);

        values.put(OFFENDER_CONFIG_BLE_DEBUG_INFO_ENABLE, OffRec.offenderBleDebugInfoEnable);
        values.put(OFFENDER_IS_BATTERY_INDICATION_ENABLED, OffRec.offenderIsBatteryIndicationEnabled);
        values.put(OFFENDER_IS_DEVICE_CASE_TAMPER_ENABLED, OffRec.offenderIsDeviceCaseTamperEenabled);

        values.put(LAUNCHER_CONFIG_SETTINGS_PASSWORD, OffRec.launcherConfigSettingsPassword);
        values.put(DEVICE_CONFIG_DEVICE_LOCK, OffRec.DeviceConfigDeviceLock);

        values.put(OFFENDER_CONFIG_EVENTS_ALARMS, OffRec.eventsAlramsJson);
        values.put(OFFENDER_CONFIG_LOCATION_TYPES, OffRec.locationTypes);
        values.put(OFFENDER_CONFIG_PM_COM_PROFILE, OffRec.pmComProfileJson);
        values.put(OFFENDER_CONFIG_PROFILE_EVENTS, OffRec.profileEventsJson);
        values.put(OFFENDER_CONFIG_HOME_ADDRESS_SETTINGS, OffRec.homeAddressSettingsJson);
        values.put(OFFENDER_CONFIG_HOME_LAT, OffRec.homeLat);
        values.put(OFFENDER_CONFIG_HOME_LONG, OffRec.homeLong);

        values.put(OFFENDER_CONFIG_DIALER_BLOCKER, OffRec.dialerBlocker);
        values.put(OFFENDER_CONFIG_IS_EMERGENCY_ENABLED, OffRec.offenderIsEmergencyEnabled);
        values.put(DEVICE_CONFIG_CELLULAR_APN, OffRec.DeviceConfigCellularApn);
        values.put(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_VALIDITY, OffRec.offenderDeviceCaseTamperValidity);
        values.put(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_X_MAGNET_THRESHOLD, OffRec.offenderDeviceCaseTamperXMagnetThreshold);
        values.put(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Y_MAGNET_THRESHOLD, OffRec.offenderDeviceCaseTamperYMagnetThreshold);
        values.put(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Z_MAGNET_THRESHOLD, OffRec.offenderDeviceCaseTamperYMagnetThreshold);
        values.put(OFFENDER_CONFIG_COMM_NETWORK_TEST, OffRec.commNetworkTest);

        values.put(OFFENDER_CONFIG_KNOX_SETTINGS, OffRec.offenderConfigKnoxSettingsJson);

        values.put(OFFENDER_CONFIG_LOCATION_WEIGHTED_AVERAGE, OffRec.locationWeightedAverage);
        values.put(OFFENDER_CONFIG_LOCATION_SMOOTHING, OffRec.locationSmoothing);
        values.put(OFFENDER_CONFIG_LOCATION_SMOOTHING_ACTIVATION, OffRec.locationSmoothingActivation);
        values.put(OFFENDER_CONFIG_LOCATION_SERVICE_INTERVAL, OffRec.locationServiceInterval);
        values.put(OFFENDER_CONFIG_LOCATION_AVERAGE_TIME_FRAME, OffRec.locationAverageTimeFrame);
        values.put(OFFENDER_CONFIG_LOCATION_SERVICE_CALC_TYPE, OffRec.locationServiceCalcType);

        values.put(OFFENDER_CONFIG_BACKGROUND_APP_WHITE_LIST, OffRec.backgroundAppWhiteList);
        values.put(OFFENDER_CONFIG_OFFICER_MODE_TIMEOUT, OffRec.officerModeTimeout);
        values.put(OFFENDER_CONFIG_VOIP_SETTINGS, OffRec.voipSettings);

        values.put(OFFENDER_CONFIG_APP_LANGUAGE, OffRec.appLanguage);
        values.put(OFFENDER_CONFIG_CUSTOM_CALL_INTERFACE, OffRec.customCallInterface);
        values.put(OFFENDER_CONFIG_IGNORE_SSL_CERT, OffRec.ignoreSslCert);

        values.put(OFFENDER_CONFIG_BATTERY_THRESHOLD, OffRec.batteryThreshold);

        values.put(OFFENDER_CONFIG_LBS_ENABLE, OffRec.lbsEnable);
        values.put(OFFENDER_CONFIG_START_LBS_THRESHOLD_NORMAL, OffRec.lbsThresholdNormal);
        values.put(OFFENDER_CONFIG_START_LBS_THRESHOLD_IN_VIOLATION, OffRec.lbsThresholdViolation);
        values.put(OFFENDER_CONFIG_LBS_INTERVAL_NORMAL, OffRec.lbsIntervalNormal);
        values.put(OFFENDER_CONFIG_LBS_INTERVAL_IN_VIOLATION, OffRec.lbsIntervalViolation);
        values.put(OFFENDER_CONFIG_LBS_STOP_VALIDITY, OffRec.lbsStopValidity);
        values.put(OFFENDER_CONFIG_MAP_URL, OffRec.OffenderMapUrl);
        values.put(OFFENDER_CONFIG_DEBUG_INFO, OffRec.debugInfoConfig);
        values.put(OFFENDER_CONFIG_DEVICE_INFO_CYCLES, OffRec.deviceInfoCycles);
        values.put(OFFENDER_CONFIG_TURN_ON_SCREEN_MOTION, OffRec.turnOnScreen);
        values.put(EVENTS_ALLOWED_WHILE_IN_SUSPEND_SCHEDULE, OffRec.allowedEventsWhileSuspend);
        values.put(OFFENDER_ACCELEROMETER_SETTINGS, OffRec.accelerometerSettings);
        values.put(OFFENDER_CONFIG_GPS_INT_NO_MOTION, OffRec.gpsIntervalNoMotion);

        values.put(OFF_CONFIG_ZONE_DRIFT_ENABLE, OffRec.zoneDriftEnable);
        values.put(OFF_ZONE_DRIFT_GPS_INT, OffRec.zoneDriftInterval);
        values.put(OFF_CONFIG_ZONE_DRIFT_DURATION, OffRec.zoneDriftDuration);
        values.put(OFF_CONFIG_ZONE_DRIFT_LOCATIONS, OffRec.zoneDriftLocations);
        values.put(DEVICE_CONFIG_GUEST_TAG_ENABLED, OffRec.guestTagEnabled);
        values.put(DEVICE_CONFIG_GUEST_TAG_TIME, OffRec.guestTagTime);

        values.put(DEVICE_CONFIG_PURECOM_AS_HOME_UNIT, OffRec.guestTagTime);


        // Update local copy
        OffDetailsRec = OffRec;
        // Insert the record
        return database.insert(TABLE_OFF_DETAILS, null, values);
    }

    private EntityOffenderDetails GetRecFromQueryCursor(Cursor QueryCursor) {
        EntityOffenderDetails OffRec = new EntityOffenderDetails(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_SN)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_RF_ID)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_ENCRYPTION)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_OFF_PROG_START)),
                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_OFF_PROG_END)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_PROXIMITY_RANGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_SCHEDULE_GRACE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_ADDRESS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_FIRST_NAME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_MID_NAME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_LAST_NAME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_PICTURE_PATH)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_PRIMARY_PHONE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_SECONDARY_PHONE)),

                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_OFFICER_NAME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_AGENCY_NAME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_NAME)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_ZONE_ID)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_ZONE_VERSION)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_ENCRYPTION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BEACON_RANGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_GOOD_POINT_THRESHOLD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BAD_POINT_THRESHOLD)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_OFF_ENROLLMENT_SCREEN_WIZARD)),

                QueryCursor.getLong(QueryCursor.getColumnIndex(COLUMN_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BIOMETRIC_TIMEOUT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_BIOMETRIC_MIN_BETWEEN)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_DEVICE_CONFIG_VER_NUMBER)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_CONFIG_VER_NUMBER)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_MESSAGE_TIMEOUT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_MESSAGE_EXPIRE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_ENABLE_MESSAGE_RESPONSE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_OFFICER_NUM_ON)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_AGENCY_NUM_ON)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_OFF_TAG_TYPE)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_DEVELOPER_MODE_ENABLE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(DEVICE_CONFIG_SERVER_URL)),
                QueryCursor.getString(QueryCursor.getColumnIndex(DEVICE_CONFIG_SERVER_PASS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_INSIDE_BEACON)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW)),
                QueryCursor.getString(QueryCursor.getColumnIndex(DEVICE_CONFIG_PHONE_EMERGENCY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_TIME_ZONE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_DST_OFFSET)),
                QueryCursor.getString(QueryCursor.getColumnIndex(DEVICE_CONFIG_FUTURE_DST)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_USE_CLIENT_CERT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_PINCODE_ENABLE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_PINCODE_ATTEMPTS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(DEVICE_CONFIG_PINCODE_PIN)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_PINCODE_LOCKTIME)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_FACTORY_RESET_ENABLE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_FACTORY_RESET_TIMEOUT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_SCHEDULE_SETTINGS_NUMBER_OF_DAYS)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_GPS_CYCLE_INTERVAL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER)),


                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_INCOMING_CALLS_WHITE_LIST)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_APPS_LIST)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PHONE_OFFICER)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PHONE_AGENCY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_RSSI_HOME_RANGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_RSSI_OUTSIDE_RANGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_SCHEDULE_EXPIRE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_IS_CONFIG_REQ_HANDLE_SUCCESS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TIME_SENSITIVITY_INSIDE_BEACON)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TIME_SENSITIVITY_OUTSIDE_BEACON)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_PROXIMITY_GRACE_TIME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PHONES_ACTIVE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_ALLOWED_SPEED)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LOCATION_VALIDITY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_SATELLITE_NUMBER)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_SETTINGS_TX_INTERVAL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_SETTINGS_IR_ON)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_SETTINGS_IR_OFF)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_SETTINGS_CASE_OPEN)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_SETTINGS_LOW_BATTERY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_IS_TAG_HEARTBEAT_ENABLED)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_ADDRESS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_BEACON_ADDRESS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_IS_USING_CONNECT_AS_HEARTBEAT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_HB_COUNTER)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_ADV_COUNTER)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_HB_INTERVAL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_HB_VIBRATE_TIMEOUT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_HB_ENABLE_FROM_SERVER)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TAG_CONFIGURATIONS)),


                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_BLE_DEBUG_INFO_ENABLE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_IS_BATTERY_INDICATION_ENABLED)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_IS_DEVICE_CASE_TAMPER_ENABLED)),

                QueryCursor.getString(QueryCursor.getColumnIndex(LAUNCHER_CONFIG_SETTINGS_PASSWORD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_DEVICE_LOCK)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_EVENTS_ALARMS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LOCATION_TYPES)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PM_COM_PROFILE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PROFILE_EVENTS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_HOME_ADDRESS_SETTINGS)),
                QueryCursor.getFloat(QueryCursor.getColumnIndex(OFFENDER_CONFIG_HOME_LAT)),
                QueryCursor.getFloat(QueryCursor.getColumnIndex(OFFENDER_CONFIG_HOME_LONG)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_DIALER_BLOCKER)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_IS_EMERGENCY_ENABLED)),
                QueryCursor.getString(QueryCursor.getColumnIndex(DEVICE_CONFIG_CELLULAR_APN)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_VALIDITY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PURE_TRACK_CASE_CLOSED_THRESHOLD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_CALIBRATION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_X_MAGNET_THRESHOLD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Y_MAGNET_THRESHOLD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Z_MAGNET_THRESHOLD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_ENABLED)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_TIMER_IN_MINUTES)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_COMM_NETWORK_TEST)),

                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_KNOX_SETTINGS)),

                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LOCATION_WEIGHTED_AVERAGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LOCATION_SMOOTHING)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LOCATION_SMOOTHING_ACTIVATION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LOCATION_SERVICE_INTERVAL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LOCATION_AVERAGE_TIME_FRAME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LOCATION_SERVICE_CALC_TYPE)),

                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_BACKGROUND_APP_WHITE_LIST)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_OFFICER_MODE_TIMEOUT)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_VOIP_SETTINGS)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_APP_LANGUAGE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_CUSTOM_CALL_INTERFACE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_IGNORE_SSL_CERT)),

                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_BATTERY_THRESHOLD)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LBS_ENABLE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_START_LBS_THRESHOLD_NORMAL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LBS_INTERVAL_NORMAL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_START_LBS_THRESHOLD_IN_VIOLATION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LBS_INTERVAL_IN_VIOLATION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_LBS_STOP_VALIDITY)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_CONFIG_MAP_URL)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_DEBUG_INFO)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_DEVICE_INFO_CYCLES)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_TURN_ON_SCREEN_MOTION)),
                QueryCursor.getString(QueryCursor.getColumnIndex(EVENTS_ALLOWED_WHILE_IN_SUSPEND_SCHEDULE)),
                QueryCursor.getString(QueryCursor.getColumnIndex(OFFENDER_ACCELEROMETER_SETTINGS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFFENDER_CONFIG_GPS_INT_NO_MOTION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFF_CONFIG_ZONE_DRIFT_ENABLE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFF_ZONE_DRIFT_GPS_INT)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFF_CONFIG_ZONE_DRIFT_DURATION)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(OFF_CONFIG_ZONE_DRIFT_LOCATIONS)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_GUEST_TAG_ENABLED)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_GUEST_TAG_TIME)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(DEVICE_CONFIG_PURECOM_AS_HOME_UNIT))
        );
        // Add fields which are not covered by the constructor
        // Add SQLite rowid
        OffRec.SetRowId(QueryCursor.getLong(QueryCursor.getColumnIndex("rowid")));

        return OffRec;
    }

    @Override
    public void AddDefaultData(SQLiteDatabase db) {
        addRecord(db, createDefaultValues());
    }

    public EntityOffenderDetails GetRecord(SQLiteDatabase db) {
        // Select query
        Cursor QueryCursor = db.query(TABLE_OFF_DETAILS, columnNamesArray,
                null, null, null, null, null);

        EntityOffenderDetails recordOffenderDetails;

        // Move to 1st row
        if (QueryCursor.getCount() == 0) {
            Log.i(TAG, "OffenderDetails Table is empty, will create default values !");
            LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "] OffenderDetails Table is empty, will create default values !", false);
            recordOffenderDetails = createDefaultValues();
        } else {
            QueryCursor.moveToFirst();
            recordOffenderDetails = GetRecFromQueryCursor(QueryCursor);
        }

        QueryCursor.close();

        return recordOffenderDetails;
    }


    private EntityOffenderDetails createDefaultValues() {
        return new EntityOffenderDetails(0, "", -1, "-1", "", 0, 0, 0, DEFAULT_SCHEDULE_GRACE, "", "", "", "", "", "", "", "", "", "", "", 0, 0,
                DEFAULT_BEACON_ENCRYPTION, DEFAULT_BEACON_RANGE, DEFAULT_LOCATION_GOOD_POINT_THRESHOLD, DEFAULT_LOCATION_BAD_POINT_THRESHOLD, DEFAULT_ENROLLMENT_SCREEN_WIZARD,
                android.os.SystemClock.elapsedRealtime(), DEFAULT_BIOMETRIC_TIMEOUT, DEFAULT_BIOMETRIC_MIN_BETWEEN, 0, 1, DEFAULT_MESSAGE_TIMEOUT, DEFAULT_MESSAGE_EXPIRE, DEFAULT_ENABLE_MESSAGE_RESPONSE, DEFAULT_OFFICER_NUM_ON,
                DEFAULT_AGENCY_NUM_ON, DEFAULT_TAG_TYPE, DEFAULT_DEVELOPER_MODE_ENABLE, ServerUrls.getInstance().getDefaultUrl(), DEFAULT_SERVER_PASS, DEFAULT_NETWORK_CYCLE_INTERVAL_OUTSIDE_BEACON, DEFAULT_NETWORK_CYCLE_INTERVAL_INSIDE_BEACON,
                DEFAULT_NETWORK_CYCLE_INTERVAL_LOW, "", DEFAULT_TIME_ZONE, DEFAULT_DST_OFFSET, DEFAULT_FUTURE_DST, DEFAULT_USE_CLIENT_CERT, DEFAULT_PINCODE_ENABLE, DEFAULT_PINCODE_ATTEMPTS, DEFAULT_PINCODE_PIN, DEFAULT_PINCODE_LOCKTIME, DEFAULT_FACTORY_RESET_ENABLE,
                DEFAULT_FACTORY_RESET_TIMEOUT, DEFAULT_SCHEDULE_SETTINGS_NUMBER_OF_DAYS, DEFAULT_GPS_POLLING_INTERVAL, DEFAULT_GPS_POLLING_BEACON_INTERVAL, DEFAULT_BAD_GPS_ACCURACY_COUNTER,
                "", "", "", "", DEFAULT_RSSI_HOME_RANGE, DEFAULT_RSSI_OUTSIDE_RANGE,
                0, 0, DEFAULT_TIME_SENSITIVITY_INSIDE_BEACON, DEFAULT_TIME_SENSITIVITY_OUTSIDE_BEACON, DEFAULT_TAG_PROXIMITY_GRACE_TIME,
                DEFAULT_BEACON_OUTSIDE_RANGE_GRACE_TIME, DEFAULT_PHONES_ACTIVE, DEFAULT_ALLOWED_SPEED, DEFAULT_LOCATION_VALIDITY, DEFAULT_SATELLITE_NUMBER, DEFAULT_TAG_SETTINGS_TX_INTERVAL,
                0, 0, 0, 0, 0,
                "", "", 1, 0, 0,
                DEFAULT_TAG_HB_INTERVAL,DEFAULT_TAG_HB_VIBRATION_TIMEOUT, DEFAULT_TAG_HB_ENABLE_FROM_SERVER, DEFAULT_TAG_CONFIGURATIONS_DEFAULT, DEFAULT_BLE_LOG_ENABLE, DEFAULT_IS_BATTERY_INDICATION_ENABLED,
                DEFAULT_IS_DEVICE_CASE_TAMPER_ENABLED, DEFAULT_SETTING_PASSWORD, DEFAULT_IS_DEVICE_LOCKED, DEFAULT_EVENTS_ALARMS_JSON, DEFAULT_LOCATION_TYPES, DEFAULT_PM_COM_PROFILE, DEFAULT_PROFILE_EVENTS,
                DEFAULT_HOME_ADDRESS_SETTINGS, DEFAULT_HOME_LAT, DEFAULT_HOME_LONG, DEFAULT_DIALER_BLOCKER, DEFAULT_EMERGENCY_ENABLED, "",
                DEFAULT_DEVICE_CASE_TAMPER_VALIDITY, DEFAULT_CASE_CLOSED_THRESHOLD, DEFAULT_DEVICE_CASE_TAMPER_CALIBRATION, DEFAULT_DEVICE_CASE_TAMPER_X_MAGNET_THRESHOLD,
                DEFAULT_DEVICE_CASE_TAMPER_Y_MAGNET_THRESHOLD, DEFAULT_DEVICE_CASE_TAMPER_Z_MAGNET_THRESHOLD, DEFAULT_DEVICE_CASE_TAMPER_RECALIBRATION_ENABLED,
                DEFAULT_DEVICE_CASE_TAMPER_RECALIBRATION_TIMER_IN_MINUTES, DEFAULT_COMM_NETWORK_TEST, DEFAULT_KNOX_SETTINGS_JSON, DEFAULT_LOCATION_WEIGHTED_AVERAGE,
                DEFAULT_LOCATION_SMOOTHING, DEFAULT_LOCATION_SMOOTHING_ACTIVATION, DEFAULT_LOCATION_SERVICE_INTERVAL_DEFAULT, DEFAULT_LOCATION_AVG_TIME_FRAME_DEFAULT,
                2, DEFAULT_BACKGROUND_APP_WHITE_LIST, DEFAULT_OFFICER_MODE_TIMEOUT, DEFAULT_VOIP_SETTINGS_JSON, DEFAULT_APP_LANGUAGE, DEFAULT_CUSTOM_CALL_INTERFACE,
                DEFAULT_IGNORE_SSL_CERT,DEFAULT_BATTERY_THRESHOLD, DEFAULT_LBS_ENABLE, DEFAULT_LBS_THRESHOLD_NORMAL, DEFAULT_LBS_THRESHOLD_VIOLATION,
                DEFAULT_LBS_INTERVAL_NORMAL, DEFAULT_LBS_INTERVAL_VIOLATION, DEFAULT_LBS_STOP_VALIDITY, DEFAULT_MAP_URL, 0, DEFAULT_DEVICE_INFO_CYCLES, 1,
                DEFAULT_ALLOWED_EVENTS_WHILE_IN_SUSPEND, DEFAULT_ACCELEROMETER_SETTINGS_DEFAULT, 1800, 1, 30,
                600, 3, DEFAULT_GUEST_TAG_ENABLED, DEFAULT_GUEST_TAG_TIME, DEFAULT_PURECOM_AS_HOME_UNIT);
    }

    @Override
    public void LoadData(SQLiteDatabase db) {
        OffDetailsRec = this.GetRecord(db);
    }

    public EntityOffenderDetails getRecordOffDetails() {
        return OffDetailsRec;
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        switch (oldVersion) {
            case 152:
                database.execSQL("alter table " + tableName + " add column " + OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Z_MAGNET_THRESHOLD
                        + " integer default " + DEFAULT_DEVICE_CASE_TAMPER_Z_MAGNET_THRESHOLD + ";");
                database.execSQL("alter table " + tableName + " add column " + OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_ENABLED
                        + " integer default " + DEFAULT_DEVICE_CASE_TAMPER_RECALIBRATION_ENABLED + ";");
                database.execSQL("alter table " + tableName + " add column " + OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_TIMER_IN_MINUTES
                        + " integer default " + DEFAULT_DEVICE_CASE_TAMPER_RECALIBRATION_TIMER_IN_MINUTES + ";");
                break;
            case 153:
                database.execSQL("alter table " + tableName + " add column " + OFFENDER_CONFIG_PURE_TRACK_CASE_CLOSED_THRESHOLD
                        + " integer default " + DEFAULT_CASE_CLOSED_THRESHOLD + ";");

                break;
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
                database.execSQL("alter table " + tableName + " add column " + DEVICE_CONFIG_GUEST_TAG_ENABLED
                        + " integer default " + DEFAULT_GUEST_TAG_ENABLED + ";");
                database.execSQL("alter table " + tableName + " add column " + DEVICE_CONFIG_GUEST_TAG_TIME
                        + " integer default " + DEFAULT_GUEST_TAG_TIME + ";");
                break;
            case 169:
                database.execSQL("update " + tableName + " set " + OFFENDER_CONFIG_DIALER_BLOCKER + " = 0");
                break;

            case 170:
            case 171:
            case 172:
            case 176:
                database.execSQL("alter table " + tableName + " add column " + DEVICE_CONFIG_PURECOM_AS_HOME_UNIT +
                        " integer default " + DEFAULT_PURECOM_AS_HOME_UNIT);
                break;
            case 183:
                database.execSQL("update " + tableName + " set " + DEVICE_CONFIG_GUEST_TAG_ENABLED + " = 0");
                break;
            case 193:
                try {
                    Cursor c = database.rawQuery("SELECT " + DEVICE_CONFIG_SERVER_PASS + " FROM " + tableName, null);
                    if (c.moveToFirst()) {
                        String currentPassword = ScramblingTextUtils.unscramble(AESUtils.decrypt(SERVER_URL_AES_KEY_BYTES, c.getString(0)));
                        ContentValues cv = new ContentValues();
                        cv.put(DEVICE_CONFIG_SERVER_PASS, AESUtils.encrypt(SERVER_URL_AES_KEY_BYTES, ScramblingTextUtils.scramble(currentPassword)));
                        database.update(tableName, cv, null, null);
                    }


                    //
                    // set Use Server Certificate to default = 1
                    database.execSQL("update " + tableName + " set " + DEVICE_CONFIG_USE_CLIENT_CERT + " = 1");
                } catch (GeneralSecurityException e) {
                    Log.e(TAG, "Updating " + DEVICE_CONFIG_SERVER_PASS + " process has been failed!");
                }
                break;
            case 196:
            case 197:
                database.execSQL("alter table " + tableName + " add column " + OFFENDER_CONFIG_TAG_HB_VIBRATE_TIMEOUT +
                        " integer default " + DEFAULT_TAG_HB_VIBRATION_TIMEOUT);
                break;
            default:
                break;
        }
    }


    public ArrayList<String> getAllowedIncomingList() {
        String deviceConfigIncomingCallsWhiteList = DatabaseAccess.getInstance().tableOffenderDetails
                .getRecordOffDetails().DeviceConfigIncomingCallsWhiteList;
        ArrayList<String> incomingCallsWhiteListArrayList = new ArrayList<String>();
        try {
            if (!deviceConfigIncomingCallsWhiteList.isEmpty()) {
                JSONObject jsonObject = new JSONObject(deviceConfigIncomingCallsWhiteList);
                JSONArray incomingCallsWhiteListJsonArray = jsonObject.getJSONArray("phones");
                for (int i = 0; i < incomingCallsWhiteListJsonArray.length(); i++) {
                    incomingCallsWhiteListArrayList.add(incomingCallsWhiteListJsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return incomingCallsWhiteListArrayList;
    }

    public boolean isNumberInWhiteList(String phoneNumber) {
        ArrayList<String> incomingCallsWhiteListArrayList = getAllowedIncomingList();
        String FilteredNum = "";

        for (int i = 0; i < incomingCallsWhiteListArrayList.size(); i++) {
            if (incomingCallsWhiteListArrayList.get(i).length() > 0) {
                FilteredNum = incomingCallsWhiteListArrayList.get(i).replace("*", "");
                if (phoneNumber.matches(".*" + FilteredNum + ".*") == true) {
                    return true;
                }
            }
        }

        return false;
    }


}
