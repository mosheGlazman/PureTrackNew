package com.supercom.puretrack.data.source.local.table_managers;

import static com.supercom.puretrack.data.source.local.table.TableOffenderDetails.COLUMN_OFF_ENABLE_MESSAGE_RESPONSE;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.supercom.puretrack.data.source.local.local_managers.hardware.BluetoothManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.FingerprintManager;
import com.supercom.puretrack.data.source.local.table.DatabaseTable;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.EventsAlarmsType;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.knox.KnoxSettingsModel;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.model.database.objects.AccelerometerConfig;
import com.supercom.puretrack.model.database.objects.BatteryThreshold;
import com.supercom.puretrack.model.database.objects.HomeAddressSettings;
import com.supercom.puretrack.model.database.objects.VoipSettings;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.Enrollment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class TableOffenderDetailsManager extends BaseTableManager {
    public static final String TAG = "DBOffenderDetailsManager";

    public interface OFFENDER_DETAILS_CONS {
        String DETAILS_OFF_ID = TableOffenderDetails.COLUMN_OFF_ID;
        String DETAILS_OFF_SN = TableOffenderDetails.COLUMN_OFF_SN;
        String DETAILS_OFF_TAG_ID = TableOffenderDetails.COLUMN_OFF_TAG_ID;
        String DETAILS_OFF_TAG_RFID = TableOffenderDetails.COLUMN_OFF_TAG_RF_ID;
        String DETAILS_OFF_TAG_ENCRYPTION = TableOffenderDetails.COLUMN_OFF_TAG_ENCRYPTION;
        String DETAILS_OFF_SCHEDULE_GRACE = TableOffenderDetails.COLUMN_OFF_SCHEDULE_GRACE;
        String DETAILS_OFF_FIRST_NAME = TableOffenderDetails.COLUMN_OFF_FIRST_NAME;
        String DETAILS_OFF_LAST_NAME = TableOffenderDetails.COLUMN_OFF_LAST_NAME;
        String DETAILS_OFF_ADDRESS = TableOffenderDetails.COLUMN_OFF_ADDRESS;
        String DETAILS_OFF_OFFICER_NAME = TableOffenderDetails.COLUMN_OFF_OFFICER_NAME;
        String DETAILS_OFF_AGENCY_NAME = TableOffenderDetails.COLUMN_OFF_AGENCY_NAME;
        String DETAILS_OFF_PICTURE_PATH = TableOffenderDetails.COLUMN_OFF_PICTURE_PATH;
        String DETAILS_OFF_PRIMARY_PHONE = TableOffenderDetails.COLUMN_OFF_PRIMARY_PHONE;
        String DETAILS_OFF_SECONDARY_PHONE = TableOffenderDetails.COLUMN_OFF_SECONDARY_PHONE;

        String DETAILS_OFF_BEACON_NAME = TableOffenderDetails.COLUMN_OFF_BEACON_NAME;
        String DETAILS_OFF_BEACON_ID = TableOffenderDetails.COLUMN_OFF_BEACON_ID;
        String DETAILS_OFF_BEACON_ZONE_ID = TableOffenderDetails.COLUMN_OFF_BEACON_ZONE_ID;
        String DETAILS_OFF_BEACON_ZONE_VERSION = TableOffenderDetails.COLUMN_OFF_BEACON_ZONE_VERSION;
        String DETAILS_OFF_BEACON_ENCRYPTION = TableOffenderDetails.COLUMN_OFF_BEACON_ENCRYPTION;
        String DETAILS_OFF_BEACON_RANGE = TableOffenderDetails.COLUMN_OFF_BEACON_RANGE;
        String DETAILS_OFF_GOOD_POINT_THRESHOLD = TableOffenderDetails.COLUMN_OFF_GOOD_POINT_THRESHOLD;
        String DETAILS_OFF_BAD_POINT_THRESHOLD = TableOffenderDetails.COLUMN_OFF_BAD_POINT_THRESHOLD;
        String DETAILS_OFF_ENROLLMENT_SCREEN_WIZARD = TableOffenderDetails.COLUMN_OFF_ENROLLMENT_SCREEN_WIZARD;
        String DETAILS_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI = TableOffenderDetails.COLUMN_OFF_DEVICE_ELAPSED_REAL_TIME_IN_MILLI;
        String DETAILS_OFF_BIOMETRIC_TIMEOUT = TableOffenderDetails.COLUMN_OFF_BIOMETRIC_TIMEOUT;
        String DETAILS_OFF_BIOMETRIC_MIN_BETWEEN = TableOffenderDetails.COLUMN_OFF_BIOMETRIC_MIN_BETWEEN;
        String COLUMN_OFF_DEVICE_CONFIG_VER_NUMBER = TableOffenderDetails.COLUMN_OFF_DEVICE_CONFIG_VER_NUMBER;
        String COLUMN_OFF_CONFIG_VER_NUMBER = TableOffenderDetails.COLUMN_OFF_CONFIG_VER_NUMBER;
        String DETAILS_OFF_MESSAGE_TIMEOUT = TableOffenderDetails.COLUMN_OFF_MESSAGE_TIMEOUT;
        String DETAILS_OFF_MESSAGE_EXPIRE = TableOffenderDetails.COLUMN_OFF_MESSAGE_EXPIRE;
        String DETAILS_OFF_ENABLE_MESSAGE_RESPONSE = COLUMN_OFF_ENABLE_MESSAGE_RESPONSE;
        String DETAILS_OFF_OFFICER_NUM_ON = TableOffenderDetails.COLUMN_OFF_OFFICER_NUM_ON;
        String DETAILS_OFF_AGENCY_NUM_ON = TableOffenderDetails.COLUMN_OFF_AGENCY_NUM_ON;
        String DETAILS_OFF_TAG_TYPE = TableOffenderDetails.COLUMN_OFF_TAG_TYPE;

        String DEVICE_CONFIG_DEVELOPER_MODE_ENABLE = TableOffenderDetails.DEVICE_CONFIG_DEVELOPER_MODE_ENABLE;
        String DEVICE_CONFIG_SERVER_URL = TableOffenderDetails.DEVICE_CONFIG_SERVER_URL;
        String DEVICE_CONFIG_SERVER_PASS = TableOffenderDetails.DEVICE_CONFIG_SERVER_PASS;
        String DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL = TableOffenderDetails.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL;
        String DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW = TableOffenderDetails.DEVICE_CONFIG_NETWORK_CYCLE_INTERVAL_LOW;
        String DEVICE_CONFIG_GPS_CYCLE_INTERVAL = TableOffenderDetails.DEVICE_CONFIG_GPS_CYCLE_INTERVAL;
        String DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL = TableOffenderDetails.DEVICE_CONFIG_GPS_CYCLE_BEACON_INTERVAL;
        String DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER = TableOffenderDetails.DEVICE_CONFIG_BAD_GPS_ACCURACY_COUNTER;
        String DEVICE_CONFIG_PHONE_EMERGENCY = TableOffenderDetails.DEVICE_CONFIG_PHONE_EMERGENCY;
        String DEVICE_CONFIG_TIME_ZONE = TableOffenderDetails.DEVICE_CONFIG_TIME_ZONE;
        String DEVICE_CONFIG_DST_OFFSET = TableOffenderDetails.DEVICE_CONFIG_DST_OFFSET;
        String DEVICE_CONFIG_FUTURE_DST = TableOffenderDetails.DEVICE_CONFIG_FUTURE_DST;
        String DEVICE_CONFIG_USE_CLIENT_CERT = TableOffenderDetails.DEVICE_CONFIG_USE_CLIENT_CERT;
        String DEVICE_CONFIG_PINCODE_ENABLE = TableOffenderDetails.DEVICE_CONFIG_PINCODE_ENABLE;
        String DEVICE_CONFIG_PINCODE_ATTEMPTS = TableOffenderDetails.DEVICE_CONFIG_PINCODE_ATTEMPTS;
        String DEVICE_CONFIG_PINCODE_PIN = TableOffenderDetails.DEVICE_CONFIG_PINCODE_PIN;
        String DEVICE_CONFIG_PINCODE_LOCK_TIME = TableOffenderDetails.DEVICE_CONFIG_PINCODE_LOCKTIME;
        String DEVICE_CONFIG_GUEST_TAG_ENABLED = TableOffenderDetails.DEVICE_CONFIG_GUEST_TAG_ENABLED;
        String DEVICE_CONFIG_GUEST_TAG_TIME = TableOffenderDetails.DEVICE_CONFIG_GUEST_TAG_TIME;

        String DEVICE_CONFIG_FACTORY_RESET_ENABLE = TableOffenderDetails.DEVICE_CONFIG_FACTORY_RESET_ENABLE;
        String DEVICE_CONFIG_FACTORY_RESET_TIMEOUT = TableOffenderDetails.DEVICE_CONFIG_FACTORY_RESET_TIMEOUT;
        String DEVICE_CONFIG_SCHEDULE_SETTINGS_NUM_OF_DAYS = TableOffenderDetails.DEVICE_CONFIG_SCHEDULE_SETTINGS_NUMBER_OF_DAYS;

        String OFFENDER_CONFIG_INCOMING_CALLS_WHITE_LIST = TableOffenderDetails.OFFENDER_CONFIG_INCOMING_CALLS_WHITE_LIST;
        String OFFENDER_CONFIG_APPS_LIST = TableOffenderDetails.OFFENDER_CONFIG_APPS_LIST;
        String OFFENDER_CONFIG_PHONE_OFFICER = TableOffenderDetails.OFFENDER_CONFIG_PHONE_OFFICER;
        String OFFENDER_CONFIG_PHONE_AGENCY = TableOffenderDetails.OFFENDER_CONFIG_PHONE_AGENCY;
        String OFFENDER_CONFIG_RSSI_HOME_RANGE = TableOffenderDetails.OFFENDER_CONFIG_RSSI_HOME_RANGE;
        String OFFENDER_CONFIG_RSSI_OUTSIDE_RANGE = TableOffenderDetails.OFFENDER_CONFIG_RSSI_OUTSIDE_RANGE;
        String OFFENDER_CONFIG_SCHEDULE_EXPIRE = TableOffenderDetails.OFFENDER_CONFIG_SCHEDULE_EXPIRE;
        String OFFENDER_CONFIG_TIME_SENSITIVITY_INSIDE_BEACON = TableOffenderDetails.OFFENDER_CONFIG_TIME_SENSITIVITY_INSIDE_BEACON;
        String OFFENDER_CONFIG_TIME_SENSITIVITY_OUTSIDE_BEACON = TableOffenderDetails.OFFENDER_CONFIG_TIME_SENSITIVITY_OUTSIDE_BEACON;
        String OFFENDER_CONFIG_TAG_PROXIMITY_GRACE_TIME = TableOffenderDetails.OFFENDER_CONFIG_TAG_PROXIMITY_GRACE_TIME;
        String OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME = TableOffenderDetails.OFFENDER_CONFIG_BEACON_OUTSIDE_RANGE_GRACE_TIME;
        String OFFENDER_CONFIG_PHONES_ACTIVE = TableOffenderDetails.OFFENDER_CONFIG_PHONES_ACTIVE;
        String OFFENDER_CONFIG_ALLOWED_SPEED = TableOffenderDetails.OFFENDER_CONFIG_ALLOWED_SPEED;
        String OFFENDER_CONFIG_LOCATION_VALIDITY = TableOffenderDetails.OFFENDER_CONFIG_LOCATION_VALIDITY;
        String OFFENDER_CONFIG_SATELLITE_NUM = TableOffenderDetails.OFFENDER_CONFIG_SATELLITE_NUMBER;

        String OFFENDER_CONFIG_TAG_SETTINGS_IR_ON = TableOffenderDetails.OFFENDER_CONFIG_TAG_SETTINGS_IR_ON;
        String OFFENDER_CONFIG_TAG_SETTINGS_IR_OFF = TableOffenderDetails.OFFENDER_CONFIG_TAG_SETTINGS_IR_OFF;
        String OFFENDER_CONFIG_TAG_SETTINGS_CASE_OPEN = TableOffenderDetails.OFFENDER_CONFIG_TAG_SETTINGS_CASE_OPEN;
        String OFFENDER_CONFIG_TAG_SETTINGS_LOW_BATTERY = TableOffenderDetails.OFFENDER_CONFIG_TAG_SETTINGS_LOW_BATTERY;

        String OFFENDER_TAG_ADDRESS = TableOffenderDetails.OFFENDER_CONFIG_TAG_ADDRESS;
        String OFFENDER_BEACON_ADDRESS = TableOffenderDetails.OFFENDER_CONFIG_BEACON_ADDRESS;
        String OFFENDER_TAG_HB_COUNTER = TableOffenderDetails.OFFENDER_CONFIG_TAG_HB_COUNTER;
        String OFFENDER_TAG_ADV_COUNTER = TableOffenderDetails.OFFENDER_CONFIG_TAG_ADV_COUNTER;
        String OFFENDER_TAG_HB_INTERVAL = TableOffenderDetails.OFFENDER_CONFIG_TAG_HB_INTERVAL;
        String OFFENDER_TAG_HB_VIBRATE_TIMEOUT = TableOffenderDetails.OFFENDER_CONFIG_TAG_HB_VIBRATE_TIMEOUT;
        String OFFENDER_TAG_HB_ENABLE_FROM_SERVER = TableOffenderDetails.OFFENDER_CONFIG_TAG_HB_ENABLE_FROM_SERVER;
        String OFFENDER_TAG_CONFIGURATIONS = TableOffenderDetails.OFFENDER_CONFIG_TAG_CONFIGURATIONS;


        String OFFENDER_BLE_DEBUG_INFO_ENABLE = TableOffenderDetails.OFFENDER_CONFIG_BLE_DEBUG_INFO_ENABLE;
        String OFFENDER_IS_BATTERY_INDICATION_ENABLED = TableOffenderDetails.OFFENDER_IS_BATTERY_INDICATION_ENABLED;
        String OFFENDER_IS_DEVICE_CASE_TAMPER_ENABLED = TableOffenderDetails.OFFENDER_IS_DEVICE_CASE_TAMPER_ENABLED;

        String LAUNCHER_CONFIG_SETTINGS_PASSWORD = TableOffenderDetails.LAUNCHER_CONFIG_SETTINGS_PASSWORD;
        String DEVICE_CONFIG_DEVICE_LOCK = TableOffenderDetails.DEVICE_CONFIG_DEVICE_LOCK;

        String EVENTS_ALARMS = TableOffenderDetails.OFFENDER_CONFIG_EVENTS_ALARMS;
        String LOCATION_TYPES = TableOffenderDetails.OFFENDER_CONFIG_LOCATION_TYPES;
        String PM_COM_PROFILE = TableOffenderDetails.OFFENDER_CONFIG_PM_COM_PROFILE;
        String PROFILE_EVENTS = TableOffenderDetails.OFFENDER_CONFIG_PROFILE_EVENTS;
        String HOME_ADDRESS_SETTINGS = TableOffenderDetails.OFFENDER_CONFIG_HOME_ADDRESS_SETTINGS;
        String HOME_LAT = TableOffenderDetails.OFFENDER_CONFIG_HOME_LAT;
        String HOME_LONG = TableOffenderDetails.OFFENDER_CONFIG_HOME_LONG;
        String DIALER_BLOCKER = TableOffenderDetails.OFFENDER_CONFIG_DIALER_BLOCKER;
        String OFFENDER_CONFIG_IS_EMERGENCY_ENABLED = TableOffenderDetails.OFFENDER_CONFIG_IS_EMERGENCY_ENABLED;
        String CELLULAR_APN = TableOffenderDetails.DEVICE_CONFIG_CELLULAR_APN;
        String OFFENDER_PURE_TRACK_CASE_TAMPER_VALIDITY = TableOffenderDetails.OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_VALIDITY;
        String OFFENDER_PURE_TRACK_CASE_CLOSED_THRESHOLD = TableOffenderDetails.OFFENDER_CONFIG_PURE_TRACK_CASE_CLOSED_THRESHOLD;
        String OFFENDER_PURE_TRACK_CASE_TAMPER_CALIBRATION = TableOffenderDetails.OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_CALIBRATION;
        String OFFENDER_PURE_TRACK_CASE_TAMPER_X_MAGNET_THRESHOLD = TableOffenderDetails.OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_X_MAGNET_THRESHOLD;
        String OFFENDER_PURE_TRACK_CASE_TAMPER_Y_MAGNET_THRESHOLD = TableOffenderDetails.OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Y_MAGNET_THRESHOLD;
        String OFFENDER_PURE_TRACK_CASE_TAMPER_Z_MAGNET_THRESHOLD = TableOffenderDetails.OFFENDER_CONFIG_PURE_TRACK_CASE_TAMPER_Z_MAGNET_THRESHOLD;
        String OFFENDER_PURE_TRACK_CASE_TAMPER_RECALIBRATION_ENABLED = TableOffenderDetails.OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_ENABLED;
        String OFFENDER_PURE_TRACK_CASE_TAMPER_RECALIBRATION_TIMER_IN_MINUTES = TableOffenderDetails.OFFENDER_CONFIG_PURE_TRACK_CASE_RECALIBRATION_TIMER_IN_MINUTES;
        String COMM_NETWORK_TEST = TableOffenderDetails.OFFENDER_CONFIG_COMM_NETWORK_TEST;
        String KNOX_SETTINGS = TableOffenderDetails.OFFENDER_CONFIG_KNOX_SETTINGS;

        String LOCATION_WEIGHTED_AVERAGE = TableOffenderDetails.OFFENDER_CONFIG_LOCATION_WEIGHTED_AVERAGE;
        String LOCATION_SMOOTHING = TableOffenderDetails.OFFENDER_CONFIG_LOCATION_SMOOTHING;
        String LOCATION_SMOOTHING_ACTIVATION = TableOffenderDetails.OFFENDER_CONFIG_LOCATION_SMOOTHING_ACTIVATION;
        String LOCATION_SERVICE_INTERVAL = TableOffenderDetails.OFFENDER_CONFIG_LOCATION_SERVICE_INTERVAL;
        String LOCATION_AVERAGE_TIME_FRAME = TableOffenderDetails.OFFENDER_CONFIG_LOCATION_AVERAGE_TIME_FRAME;
        String LOCATION_SERVICE_CALC_TYPE = TableOffenderDetails.OFFENDER_CONFIG_LOCATION_SERVICE_CALC_TYPE;

        String BACKGROUND_APP_WHITE_LIST = TableOffenderDetails.OFFENDER_CONFIG_BACKGROUND_APP_WHITE_LIST;
        String OFFICER_MODE_TIMEOUT = TableOffenderDetails.OFFENDER_CONFIG_OFFICER_MODE_TIMEOUT;
        String VOIP_SETTINGS = TableOffenderDetails.OFFENDER_CONFIG_VOIP_SETTINGS;

        String APP_LANGUAGE = TableOffenderDetails.OFFENDER_CONFIG_APP_LANGUAGE;
        String CUSTOM_CALL_INTERFACE = TableOffenderDetails.OFFENDER_CONFIG_CUSTOM_CALL_INTERFACE;
        String IGNORE_SSL_CERT = TableOffenderDetails.OFFENDER_CONFIG_IGNORE_SSL_CERT;


        String LBS_ENABLE = TableOffenderDetails.OFFENDER_CONFIG_LBS_ENABLE;
        String START_LBS_THRESHOLD_NORMAL = TableOffenderDetails.OFFENDER_CONFIG_START_LBS_THRESHOLD_NORMAL;
        String LBS_INTERVAL_NORMAL = TableOffenderDetails.OFFENDER_CONFIG_LBS_INTERVAL_NORMAL;
        String START_LBS_THRESHOLD_IN_VIOLATION = TableOffenderDetails.OFFENDER_CONFIG_START_LBS_THRESHOLD_IN_VIOLATION;
        String LBS_INTERVAL_IN_VIOLATION = TableOffenderDetails.OFFENDER_CONFIG_LBS_INTERVAL_IN_VIOLATION;
        String LBS_STOP_VALIDITY = TableOffenderDetails.OFFENDER_CONFIG_LBS_STOP_VALIDITY;

        String BATTERY_THRESHOLD = TableOffenderDetails.OFFENDER_CONFIG_BATTERY_THRESHOLD;
        String OFFENDER_MAP_URL = TableOffenderDetails.OFFENDER_CONFIG_MAP_URL;
        String OFFENDER_DEBUG_INFO_CONFIG = TableOffenderDetails.OFFENDER_CONFIG_DEBUG_INFO;
        String OFFENDER_DEVICE_INFO_CYCLES = TableOffenderDetails.OFFENDER_CONFIG_DEVICE_INFO_CYCLES;
        String OFFENDER_TURN_ON_SCREEN_MOTION = TableOffenderDetails.OFFENDER_CONFIG_TURN_ON_SCREEN_MOTION;
        String OFFENDER_EVENTS_ALLOWED_WHILE_IN_SUSPEND_SCHEDULE = TableOffenderDetails.EVENTS_ALLOWED_WHILE_IN_SUSPEND_SCHEDULE;
        String ACCELEROMETER_SETTINGS = TableOffenderDetails.OFFENDER_ACCELEROMETER_SETTINGS;
        String DEVICE_CONFIG_GPS_INT_NO_MOTION = TableOffenderDetails.OFFENDER_CONFIG_GPS_INT_NO_MOTION;


        String DEVICE_CONFIG_ZONE_DRIFT_ENABLE = TableOffenderDetails.OFF_CONFIG_ZONE_DRIFT_ENABLE;
        String DEVICE_CONFIG_ZONE_DRIFT_LOCATIONS = TableOffenderDetails.OFF_CONFIG_ZONE_DRIFT_LOCATIONS;
        String DEVICE_CONFIG_ZONE_DRIFT_GPS_INT = TableOffenderDetails.OFF_ZONE_DRIFT_GPS_INT;
        String DEVICE_CONFIG_ZONE_DRIFT_DURATION = TableOffenderDetails.OFF_CONFIG_ZONE_DRIFT_DURATION;

        String DEVICE_CONFIG_PURECOM_AS_HOME_UNIT = TableOffenderDetails.DEVICE_CONFIG_PURECOM_AS_HOME_UNIT;
    }

    // Accelerometer params


    private int debugInfoConfig = 0;

    private static final TableOffenderDetailsManager INSTANCE = new TableOffenderDetailsManager();

    private TableOffenderDetailsManager() {

    }

    public static TableOffenderDetailsManager sharedInstance() {
        return INSTANCE;
    }

    @Override
    protected DatabaseTable getTable() {
        return DatabaseAccess.getInstance().tableOffenderDetails;
    }

    @Override
    protected EnumDatabaseTables getEnumDBTable() {
        return EnumDatabaseTables.TABLE_OFFENDER_DETAILS;
    }

    public ArrayList<String> getEnrollmentScreensToShow() {
        String enrollmentScreensJson = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_ENROLLMENT_SCREEN_WIZARD);
        ArrayList<String> enrollmentScreens = new ArrayList<>();
        try {
            JSONArray enrollmentScreensArray = new JSONArray(enrollmentScreensJson);
            if (enrollmentScreensArray.length() > 0) {
                JSONArray namesArray = enrollmentScreensArray.getJSONObject(0).names();
                for (int i = 0; i < namesArray.length(); i++) {
                    String enrollmentScreenKey = namesArray.getString(i);
                    boolean shouldShowScreen = enrollmentScreensArray.getJSONObject(0).getInt(enrollmentScreenKey) == 1;
                    if (!shouldShowScreen) continue;
                    if (enrollmentScreenKey.equals(Enrollment.TAG_SETUP_STEP)) {
                        String tagRfId = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_TAG_RFID);
                        boolean hasBeacon = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BEACON_ID) != 0;
                        if (!tagRfId.equals(BluetoothManager.NO_TAG) || hasBeacon) {
                            enrollmentScreens.add(enrollmentScreenKey);
                        }
                    } else if (enrollmentScreenKey.equals(Enrollment.OFFENDER_FINGER_ENROLLMENT_STEP)) {
                        if (new FingerprintManager(App.getContext(), null).isFeatureEnabled()) {
                            enrollmentScreens.add(enrollmentScreenKey);
                        } else {
                            String messageToUpload = "Device not support fingerprint";
                            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                                    messageToUpload, DebugInfoModuleId.Network.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
                        }
                    } else {
                        enrollmentScreens.add(enrollmentScreenKey);
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return enrollmentScreens;
    }

    public AccelerometerConfig getAccelerometerSettings() {
        String accelerometerSettingsJsonString = getStringValueByColumnName(OFFENDER_DETAILS_CONS.ACCELEROMETER_SETTINGS);
        List<AccelerometerConfig> settingsList = null;
        Gson gson = new Gson();
        try {
            settingsList = gson.fromJson(accelerometerSettingsJsonString, new TypeToken<List<AccelerometerConfig>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (settingsList != null && !settingsList.isEmpty()) {
            return settingsList.get(0);
        } else {
            return new AccelerometerConfig();
        }
    }

    public int getEventsAlarmType(int eventType) {
        String eventsAlarmListJsonString = TableOffenderDetailsManager.sharedInstance().
                getStringValueByColumnName(OFFENDER_DETAILS_CONS.EVENTS_ALARMS);
        try {
            JSONArray eventsAlramJsonArray = new JSONArray(eventsAlarmListJsonString);
            if (eventsAlramJsonArray.length() > 0) {
                JSONArray namesArray = eventsAlramJsonArray.getJSONObject(0).names();
                for (int i = 0; i < namesArray.length(); i++) {
                    //get key
                    String eventTypeKey = namesArray.getString(i);
                    if (String.valueOf(eventType).equals(eventTypeKey)) {
                        //get value
                        return eventsAlramJsonArray.getJSONObject(0).getInt(eventTypeKey);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return EventsAlarmsType.SILENT;
    }

    public boolean getIsGuestTagEnabled() {
        return getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_GUEST_TAG_ENABLED) > 0;
    }

    public HomeAddressSettings getHomeAddressSettingsObject() {

        String homeAddressSettingsJsonString = getStringValueByColumnName(OFFENDER_DETAILS_CONS.HOME_ADDRESS_SETTINGS);
        List<HomeAddressSettings> homeAddressSettingsList = null;
        Gson gson = new Gson();
        try {
            homeAddressSettingsList = gson.fromJson(homeAddressSettingsJsonString, new TypeToken<List<HomeAddressSettings>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (homeAddressSettingsList != null && !homeAddressSettingsList.isEmpty()) {
            return homeAddressSettingsList.get(0);
        } else {
            return new HomeAddressSettings();
        }
    }


    public KnoxSettingsModel getCurKNOXSettingsConfiguration() {

        String KNOXSettingsJsonString = getStringValueByColumnName(OFFENDER_DETAILS_CONS.KNOX_SETTINGS);
        List<KnoxSettingsModel> KnoxSettingsModelList = null;
        Gson gson = new Gson();
        try {
            KnoxSettingsModelList = gson.fromJson(KNOXSettingsJsonString, new TypeToken<List<KnoxSettingsModel>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (KnoxSettingsModelList != null && !KnoxSettingsModelList.isEmpty()) {
            return KnoxSettingsModelList.get(0);
        } else {
            return new KnoxSettingsModel();
        }
    }

    public boolean isOffenderMessageResponseEnabled() {
        return getIntValueByColumnName(COLUMN_OFF_ENABLE_MESSAGE_RESPONSE) > 0;
    }

    public VoipSettings getVoipSettingsObject() {
        String voipSettingsJsonString = getStringValueByColumnName(OFFENDER_DETAILS_CONS.VOIP_SETTINGS);
        List<VoipSettings> voipSettingsList = null;
        Gson gson = new Gson();

        try {
            voipSettingsList = gson.fromJson(voipSettingsJsonString, new TypeToken<List<VoipSettings>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (voipSettingsList != null && !voipSettingsList.isEmpty()) {
            return voipSettingsList.get(0);
        } else {
            return new VoipSettings();
        }
    }

    public boolean isDeviceLocked() {
        return getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_DEVICE_LOCK) == 1;
    }

    public BatteryThreshold getBatteryThresholdConfiguration() {

        String batteryThresholdString = getStringValueByColumnName(OFFENDER_DETAILS_CONS.BATTERY_THRESHOLD);
        List<BatteryThreshold> batteryThresholdList = null;
        Gson gson = new Gson();
        try {
            batteryThresholdList = gson.fromJson(batteryThresholdString, new TypeToken<List<BatteryThreshold>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (batteryThresholdList != null && !batteryThresholdList.isEmpty()) {
            return batteryThresholdList.get(0);
        } else {
            return new BatteryThreshold();
        }
    }

    public void setDebugInfoConfig(int NewConfig) {
        debugInfoConfig = NewConfig;
    }

    public int getDebugInfoConfig() {
        return debugInfoConfig;
    }
}
