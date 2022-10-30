package com.supercom.puretrack.util.constants.database_defaults;



import android.app.enterprise.knoxcustom.CustomDeviceManager;

import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.model.database.objects.BatteryThreshold;
import com.supercom.puretrack.util.constants.Enrollment;

import java.util.concurrent.TimeUnit;

public class DefaultOffenderDetailsValues {

    public static final String DEFAULT_SERVER_PASS = "100000";
    public static final int DEFAULT_DEVELOPER_MODE_ENABLE = 1;
    public static final int DEFAULT_BIOMETRIC_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
    public static final int DEFAULT_BIOMETRIC_MIN_BETWEEN = (int) TimeUnit.SECONDS.toMillis(300);
    public static final int DEFAULT_MESSAGE_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(120);
    public static final int DEFAULT_MESSAGE_EXPIRE = (int) TimeUnit.HOURS.toMillis(72);
    public static final int DEFAULT_ENABLE_MESSAGE_RESPONSE = 0;
    public static final int DEFAULT_GPS_POLLING_INTERVAL = 60;
    public static final int DEFAULT_GPS_POLLING_BEACON_INTERVAL = (int) TimeUnit.MINUTES.toSeconds(30);
    public static final int DEFAULT_BAD_GPS_ACCURACY_COUNTER = 10;
    public static final int DEFAULT_NETWORK_CYCLE_INTERVAL_OUTSIDE_BEACON = (int) TimeUnit.MINUTES.toSeconds(2);
    public static final int DEFAULT_NETWORK_CYCLE_INTERVAL_INSIDE_BEACON = (int) TimeUnit.MINUTES.toSeconds(30);
    public static final int DEFAULT_NETWORK_CYCLE_INTERVAL_LOW = (int) TimeUnit.MINUTES.toSeconds(30);
    public static final int DEFAULT_TIME_SENSITIVITY_INSIDE_BEACON = 120;
    public static final int DEFAULT_TIME_SENSITIVITY_OUTSIDE_BEACON = 60;
    public static final int DEFAULT_LOCATION_GOOD_POINT_THRESHOLD = 50;
    public static final int DEFAULT_LOCATION_BAD_POINT_THRESHOLD = 300;
    public static final int DEFAULT_TAG_PROXIMITY_GRACE_TIME = (int) TimeUnit.SECONDS.toMillis(15);
    public static final int DEFAULT_BEACON_OUTSIDE_RANGE_GRACE_TIME = 50;
    public static final int DEFAULT_RSSI_HOME_RANGE = -105;
    public static final int DEFAULT_RSSI_OUTSIDE_RANGE = -95;
    public static final int DEFAULT_TAG_HB_INTERVAL = 15;
    public static final int DEFAULT_TAG_HB_VIBRATION_TIMEOUT = 100;
    public static final int DEFAULT_TAG_HB_ENABLE_FROM_SERVER = 1;
    public static final int DEFAULT_PHONES_ACTIVE = 0;
    public static final int DEFAULT_ALLOWED_SPEED = 500;
    public static final int DEFAULT_LOCATION_VALIDITY = 2;
    public static final int DEFAULT_SATELLITE_NUMBER = 5;
    public static final String DEFAULT_BEACON_ENCRYPTION = "";
    public static final int DEFAULT_BEACON_RANGE = -105;
    public static final int DEFAULT_SCHEDULE_GRACE = 600;
    public static final int DEFAULT_TIME_ZONE = 0;
    public static final int DEFAULT_OFFICER_NUM_ON = 1;
    public static final int DEFAULT_AGENCY_NUM_ON = 1;
    public static final int DEFAULT_TAG_TYPE = 0;
    public static final int DEFAULT_EMERGENCY_ENABLED = 0;
    public static final String DEFAULT_ALLOWED_EVENTS_WHILE_IN_SUSPEND = "7,9,10,13,14,15,1016,1017,1018,1019,1047,1048,1049,1062,1063,1050,1057,1058";
    public static final String DEFAULT_ACCELEROMETER_SETTINGS_DEFAULT = "[{\"Enabled\":1,\"MotionWinSamples\":100,\"MotionWinPercentage\":30,\"MotionThreshold\":0.2,\"StaticWinSamples\":3000,\"StaticWinPercentage\":95,\"staticThreshold\":0.2,\"motion_sample_time\":5,\"motion_window_time\":90,\"motion_window_level\":35}]";
    public static final int DEFAULT_DEVICE_CASE_TAMPER_VALIDITY = 40;
    public static final int DEFAULT_DEVICE_CASE_TAMPER_CALIBRATION = 4;
    public static final int DEFAULT_DEVICE_CASE_TAMPER_X_MAGNET_THRESHOLD = 3;
    public static final int DEFAULT_DEVICE_CASE_TAMPER_Y_MAGNET_THRESHOLD = 3;
    public static final int DEFAULT_DEVICE_CASE_TAMPER_Z_MAGNET_THRESHOLD = 3;
    public static final int DEFAULT_DEVICE_CASE_TAMPER_RECALIBRATION_ENABLED = 0;
    public static final int DEFAULT_DEVICE_CASE_TAMPER_RECALIBRATION_TIMER_IN_MINUTES = 1440;
    public static final int DEFAULT_CASE_CLOSED_THRESHOLD = 999;

    public static final String DEFAULT_ENROLLMENT_SCREEN_WIZARD = "[{\""
            + "LoginPass" + "\":" + 0
            + "," + "\"" + Enrollment.KNOX_SETUP_STEP + "\":" + 1
            + "," + "\"" + Enrollment.OFFENDER_DETAILS_STEP + "\":" + 1
            + "," + "\"" + Enrollment.TAG_SETUP_STEP + "\":" + 1
            + "," + "\"" + Enrollment.OFFENDER_FINGER_ENROLLMENT_STEP + "\":" + 0
            + "," + "\"" + Enrollment.OFFICER_FINGER_ENROLLMENT_STEP + "\":" + 0
            + "," + "\"" + Enrollment.WI_FI_STEP + "\":" + 0
            + "," + "\"" + Enrollment.LOCATION_VALIDATION_STEP + "\":" + 1
            + "}]";

    public static final int DEFAULT_TAG_SETTINGS_TX_INTERVAL = (int) TimeUnit.SECONDS.toMillis(15);
    public static final String DEFAULT_TAG_CONFIGURATIONS_DEFAULT = "''";
    public static final int DEFAULT_BLE_LOG_ENABLE = 0;
    public static final int DEFAULT_IS_BATTERY_INDICATION_ENABLED = 0;
    public static final int DEFAULT_IS_DEVICE_CASE_TAMPER_ENABLED = 0;
    public static final String DEFAULT_SETTING_PASSWORD = "super1234";
    public static final int DEFAULT_IS_DEVICE_LOCKED = 1;
    public static final String DEFAULT_EVENTS_ALARMS_JSON = "[{\""
            + TableEventConfig.EventTypes.eventProximityOpen + "\":" + TableEventConfig.EventsAlarmsType.TAG_PROXIMITY
            + "," + "\"" + "2002" + "\":" + TableEventConfig.EventsAlarmsType.TAG_PROXIMITY
            + "," + "\"" + TableEventConfig.EventTypes.eventGpsProximityViolationOpen + "\":" + TableEventConfig.EventsAlarmsType.TAG_PROXIMITY
            + "," + "\"" + TableEventConfig.EventTypes.eventDeviceBatteryCritical + "\":" + TableEventConfig.EventsAlarmsType.DEVICE_SETTINGS
            + "}]";
    public static final String DEFAULT_LOCATION_TYPES = "All";
    public static final String DEFAULT_PM_COM_PROFILE = "["
            + "{\""
            + "ID" + "\":" + 1
            + "," + "\"" + "CommInterval" + "\":" + 20
            + "," + "\"" + "LocationInterval" + "\":" + 20
            + "," + "\"" + "MinDuration" + "\":" + 0
            + "," + "\"" + "MaxDuration" + "\":" + 3600
            + "},"
            + "{\""
            + "ID" + "\":" + 2
            + "," + "\"" + "CommInterval" + "\":" + 1800
            + "," + "\"" + "LocationInterval" + "\":" + 60
            + "," + "\"" + "MinDuration" + "\":" + 0
            + "," + "\"" + "MaxDuration" + "\":" + 3600
            + "}"
            + "]";
    public static final String DEFAULT_PROFILE_EVENTS = "[{\""
            + "EventID" + "\":" + TableEventConfig.EventTypes.eventProximityOpen + "," + "\"" + "ProfileID" + "\":" + 1 + "," + "\"" + "Restrictions" + "\":" + TableEventConfig.Restrictions.NORMAL
            + "},{" + "\"" + "EventID" + "\":" + TableEventConfig.EventTypes.EnteredInclusionZoneDuringCurfew + "," + "\"" + "ProfileID" + "\":" + 1 + "," + "\"" + "Restrictions" + "\":" + TableEventConfig.Restrictions.NORMAL
            + "},{" + "\"" + "EventID" + "\":" + TableEventConfig.EventTypes.ExitedInclusionZoneDuringCurfew + "," + "\"" + "ProfileID" + "\":" + 1 + "," + "\"" + "Restrictions" + "\":" + TableEventConfig.Restrictions.NORMAL
            + "},{" + "\"" + "EventID" + "\":" + TableEventConfig.EventTypes.EnteredExclusionZoneDuringCurfew + "," + "\"" + "ProfileID" + "\":" + 1 + "," + "\"" + "Restrictions" + "\":" + TableEventConfig.Restrictions.NORMAL
            + "},{" + "\"" + "EventID" + "\":" + TableEventConfig.EventTypes.OutsideInclusionZoneMustEnter + "," + "\"" + "ProfileID" + "\":" + 1 + "," + "\"" + "Restrictions" + "\":" + TableEventConfig.Restrictions.NORMAL
            + "},{" + "\"" + "EventID" + "\":" + TableEventConfig.EventTypes.PresentInExclusionZoneMustLeave + "," + "\"" + "ProfileID" + "\":" + 1 + "," + "\"" + "Restrictions" + "\":" + TableEventConfig.Restrictions.NORMAL
            + "},{" + "\"" + "EventID" + "\":" + TableEventConfig.EventTypes.enteredHomeRadius + "," + "\"" + "ProfileID" + "\":" + 2 + "," + "\"" + "Restrictions" + "\":" + TableEventConfig.Restrictions.NORMAL
            + "}]";

    public static final String DEFAULT_HOME_ADDRESS_SETTINGS = "[{\""
            + "Enable" + "\":" + 1
            + "," + "\"" + "Radius" + "\":" + 100
            + "," + "\"" + "ProfileID" + "\":" + 2
            + "}]";
    public static final float DEFAULT_HOME_LAT = 0;
    public static final float DEFAULT_HOME_LONG = 0;
    public static final int DEFAULT_DIALER_BLOCKER = 0;
    public static final int DEFAULT_COMM_NETWORK_TEST = 1;
    public static final int DEFAULT_SCREEN_TIMEOUT = 15;
    public static final String DEFAULT_KNOX_SETTINGS_JSON = "[{\""
            + "Airplane" + "\":" + 1
            + "," + "\"" + "StatusBar" + "\":" + 1
            + "," + "\"" + "BluetoothState" + "\":" + 1
            + "," + "\"" + "AutomaticTime" + "\":" + 1
            + "," + "\"" + "Wifi" + "\":" + 0
            + "," + "\"" + "NFC" + "\":" + 0
            + "," + "\"" + "SVoice" + "\":" + 0
            + "," + "\"" + "Power" + "\":" + 0
            + "," + "\"" + "RecentApps" + "\":" + 0
            + "," + "\"" + "MobileData" + "\":" + 1
            + "," + "\"" + "DataRoaming" + "\":" + 1
            + "," + "\"" + "Local" + "\":" + "{\""
            + "LocaleLanguage" + "\":" + "\"\""
            + "," + "\"" + "LocaleCountry" + "\":" + "\"\"" + "}"
            + "," + "\"" + "Screentimout" + "\":" + DEFAULT_SCREEN_TIMEOUT
            + "," + "\"" + "Toast" + "\":" + 0
            + "," + "\"" + "USB" + "\":" + 0
            + "," + "\"" + "Autostart" + "\":" + 1
            + "," + "\"" + "ScreenOffOnHomeLongPress" + "\":" + 1
            + "," + "\"" + "NetworkType" + "\":" + CustomDeviceManager.NETWORK_TYPE_WCDMA_PREF
            + "," + "\"" + "TimeZone" + "\":" + "0"
            + "," + "\"" + "NotificationMessages" + "\":" + 3
            + "," + "\"" + "SafeMode" + "\":" + 0
            + "," + "\"" + "FactoryReset" + "\":" + 0
            + "," + "\"" + "OTAUpdate" + "\":" + 0
            + "," + "\"" + "SDCard" + "\":" + 0
            + "," + "\"" + "InstallApps" + "\":" + 1
            + "," + "\"" + "UninstallApps" + "\":" + 0
            + "," + "\"" + "MobileDataLimit" + "\":" + 0
            + "," + "\"" + "LockScreen" + "\":" + 0
            + "}]";

    public static final int DEFAULT_DST_OFFSET = 3600;
    public static final String DEFAULT_FUTURE_DST = "[{}]";
    public static final int DEFAULT_USE_CLIENT_CERT = 1;
    public static final int DEFAULT_PINCODE_ENABLE = 0;
    public static final int DEFAULT_PINCODE_ATTEMPTS = 5;
    public static final String DEFAULT_PINCODE_PIN = "756453";
    public static final int DEFAULT_PINCODE_LOCKTIME = 100;
    public static final int DEFAULT_FACTORY_RESET_ENABLE = 0;
    public static final int DEFAULT_FACTORY_RESET_TIMEOUT = 5;
    public static final int DEFAULT_SCHEDULE_SETTINGS_NUMBER_OF_DAYS = 7;
    public static final int DEFAULT_LOCATION_WEIGHTED_AVERAGE = 50;
    public static final int DEFAULT_LOCATION_SMOOTHING = 0;
    public static final int DEFAULT_LOCATION_SMOOTHING_ACTIVATION = 1;
    public static final int DEFAULT_LOCATION_SERVICE_INTERVAL_DEFAULT = 10;
    public static final int DEFAULT_LOCATION_AVG_TIME_FRAME_DEFAULT = 15;
    public static final String DEFAULT_BACKGROUND_APP_WHITE_LIST = "";
    public static final int DEFAULT_OFFICER_MODE_TIMEOUT = 1200;
    public static final String DEFAULT_VOIP_SETTINGS_JSON = "[{\""
            + "Enable" + "\":" + 1
            + "," + "\"" + "OutgoingCalls" + "\":" + 1
            + "}]";
    public static final String DEFAULT_BATTERY_THRESHOLD = "[{\""
            + "Charger_Low" + "\":" + BatteryThreshold.BATTERY_CONST.BATTERY_LOW_CHARGE
            + "," + "\"" + "Charger_Medium" + "\":" + BatteryThreshold.BATTERY_CONST.BATTERY_MEDIUM_CHARGE
            + "," + "\"" + "Charger_High" + "\":" + BatteryThreshold.BATTERY_CONST.BATTERY_HIGH_CHARGE
            + "," + "\"" + "No_Charger_Critical" + "\":" + BatteryThreshold.BATTERY_CONST.BATTERY_CRITICAL_CONSUMPTION
            + "," + "\"" + "No_Charger_Low" + "\":" + BatteryThreshold.BATTERY_CONST.BATTERY_LOW_CONSUMPTION
            + "," + "\"" + "No_Charger_Medium" + "\":" + BatteryThreshold.BATTERY_CONST.BATTERY_MEDIUM_CONSUMPTION
            + "}]";
    public static final String DEFAULT_APP_LANGUAGE = "";
    public static final int DEFAULT_IGNORE_SSL_CERT = 0;
    public static final int DEFAULT_LBS_ENABLE = 0;
    public static final int DEFAULT_LBS_THRESHOLD_NORMAL = 1800;
    public static final int DEFAULT_LBS_THRESHOLD_VIOLATION = 300;
    public static final int DEFAULT_LBS_INTERVAL_NORMAL = 1800;
    public static final int DEFAULT_LBS_INTERVAL_VIOLATION = 1800;
    public static final int DEFAULT_LBS_STOP_VALIDITY = 1;
    public static final String DEFAULT_MAP_URL = "";
    public static final int DEFAULT_DEVICE_INFO_CYCLES = 12;
    public static int DEFAULT_GUEST_TAG_ENABLED = 0;
    public static int DEFAULT_GUEST_TAG_TIME = 180;
    public static int DEFAULT_PURECOM_AS_HOME_UNIT = 0;
}
