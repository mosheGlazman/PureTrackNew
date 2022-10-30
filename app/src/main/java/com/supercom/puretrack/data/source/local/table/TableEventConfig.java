package com.supercom.puretrack.data.source.local.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.supercom.puretrack.database.DatabaseColumn;
import com.supercom.puretrack.model.database.enums.EnumDatabaseColumnType;
import com.supercom.puretrack.model.database.entities.DatabaseEntity;
import com.supercom.puretrack.model.database.entities.EntityEventConfig;
import com.supercom.puretrack.util.constants.database_defaults.DefaultEventConfigValues;

public class TableEventConfig extends DatabaseTable {
    public interface EventTypes {

        int eventTagTamperBatteryLow = 7;    // Battery Tamper
        int eventTagTamperCaseOpen = 9;    // Case Tamper
        int eventTagTamperStrapOpen = 10;    // Strap Tamper Closed
        int eventTagFrauTamperOpen = 11;
        int eventTagTamperBatteryNormal = 13;    // Battery Tamper
        int eventTagTamperCaseClose = 14;    // Case Tamper Closed
        int eventTagTamperStrapClose = 15;    // Strap Tamper Closed
        int eventTagFrauTamperClose = 16;
        int eventGuestTagEntered = 18;
        int eventGuestTagLeft = 19;
        int eventMonitoringStarted = 23;
        int deviceCaseTamperOpen = 32;
        int deviceCaseTamperClosed = 38;
        int biometricTestPassed = 40;
        int biometricTestFailed = 41;
        int ScheduleViolationClosed = 55;
        int eventMessageAck = 60;
        int eventMessageTimeOut = 61;
        int deviceSoftwareUpgradeSuccessful = 63;
        int SyncSuccessful = 64;
        int SyncFailed = 65;
        int eventDeviceBatteryHigh = 68;
        int eventDeviceBatteryMedium = 69;
        int eventDeviceBatteryLow = 70;
        int eventDeviceBatteryCritical = 71;
        int biometricTestTimeOut = 73;
        int offenderEnrolmentPerformed = 75;
        int softwareUpgradeFailed = 81;
        int tagEncryptionError = 90;
        int tagEncryptionRecovered = 91;
        int tagSetupSuccess = 92;
        int SimCardInserted = 93;
        int SimCardRemoved = 94;
        int simCardReplaced = 114;
        int noFingerprintRegistered = 129;
        int tagNoMotion = 132;
        int tagMotion = 133;
        int eventProximityOpen = 1000; // Proximity_Tamper
        int eventProximityClose = 1001; // Proximity_Tamper_Closed
        int eventPowerOff = 1002; // Device_Shut_Down
        int eventPowerOn = 1003; // Device_Start_Up
        int eventEnteredExclusionZone = 1005; // Entered_Exclusion_Zone
        int eventExitedExclusionZone = 1006; // Exited_Exclusion_Zone
        int eventEnteredInclusionZone = 1007; // Entered_Inclusion_Zone
        int eventExitedInclusionZone = 1008; // Exited_Inclusion_Zone
        int eventEnteredPureBeaconZone = 1014; //  not used (using enter inclusion zone)
        int eventLeftPureBeaconZone = 1015; //  not used (using left inclusion zone)
        int eventBeaconTamperCaseOpen = 1016; // 'PureBeacon Case Tamper',
        int eventBeaconTamperCaseClose = 1017; // 'PureBeacon Case Tamper Closed',
        int eventBeaconTamperProximityOpen = 1018; // 'PureBeacon Proximity Tamper',
        int eventBeaconTamperProximityClose = 1019; // 'PureBeacon Proximity Tamper Closed'
        int PresentInInclusionZoneMustLeave = 1020;
        int EnteredInclusionZoneDuringCurfew = 1021;
        int ExitedInclusionZoneAfterViolation = 1022;
        int ExitedInclusionZoneDuringCurfew = 1023;
        int EnteredInclusionZoneAfterViolation = 1024;
        int AppointmentEndedInsideViolationCleared = 1025;
        int EnteredExclusionZoneDuringCurfew = 1026;
        int ExitedExclusionZoneAfterViolation = 1027;
        int OutsideInclusionZoneMustEnter = 1028;
        int PresentInExclusionZoneMustLeave = 1029;
        int OnACCharger = 1030;
        int OffACCharger = 1031;
        int AppointmentEndedOutsideViolationCleared = 1041;
        int PowerOnAfterSuddenShutDown = 1042; // PowerOnAfterSuddenShutDown
        int OnUSBCharger = 1043;
        int OffUSBCharger = 1044;
        int offenderLocationUnavailable = 1045;
        int offenderLocationRestored = 1046;
        int eventBeaconMotionTamperOpen = 1047;
        int eventBeaconMotionTamperClose = 1048;
        int settingsMenuLoginPerformed = 1049;
        int settingsMenuLoginFailure = 1050;
        int sysSmsReceived = 1051;
        int beaconEncryptionError = 1052;
        int beaconEncryptionRecovered = 1053;
        int deviceBatteryFull = 1054;
        int pendingEnrolment = 1056;
        int startProfile = 1057;
        int endProfile = 1058;
        int enteredBufferOfInclusionZone = 1059;
        int presentInBufferOfInclusionZone = 1060;
        int tagSetupFailed = 1061;
        int beaconBatteryTamper = 1062;
        int beaconBatteryTamperClosed = 1063;
        int enteredBufferOfExclusionZone = 1064;
        int presentInBufferOfExclusionZone = 1065;
        int exitedBufferOfInclusionZone = 1066;
        int exitedBufferOfExclusionZone = 1067;
        int sysSmsConditionsNotMet = 1068;
        int eventBeaconFrauTamperOpen = 1069;
        int eventBeaconFrauTamperClose = 1070;
        int mobileDataDisabled = 1071;
        int mobileDataRestored = 1072;
        int enteredHomeRadius = 1073;
        int leftHomeRadius = 1074;
        int offenderFingerprintScanned = 1083;
        int tag_beaconVerified = 1084;
        int knoxActivatedOnDevice = 1085;
        int deviceLocationVerified = 1086;
        int eventConnectionUnavailableDeviceRestart = 1087; // Device Restart
        int gpsFraudLocation = 1090;
        int gpsFraudLocationClosed = 1091;
        int flightModeEnabled = 1092;
        int flightModeDisabled = 1093;
        int eventDeviceStartupAfterRestart = 1094; // Device startup after restart
        int applicationInitializedMobileDataRestart = 1095;
        int eventGpsProximityViolationOpen = 2004;
        int eventGpsProximityViolationClose = 2005;
        int offenderDeclinedUpgrade = 2008;
        int softwareUpgradeTimeOut = 2009;
        int eventGpsProximityWarningOpen = 2011;
        int eventGpsProximityWarningClose = 2012;
        int lockAfterPincodeAttemptsStarted = 2013;    // lock the device Pincode Attempts - start
        int lockAfterPincodeAttemptsEnded = 2014;    // device lock ended event
        int eventNewOffenderMessage = 2015;
        int eventSecureBootIssue = 2016;
        int photoCanceledByTheOffender = 2028;
        int photoTest = 2029;
        int deviceShieldingOpen = 2030;
        int deviceShieldingClosed = 2031;
        int deviceJammingTamper = 2032;
        int deviceJammingClosed = 2033;
        int deviceDiagnosticReport = 2034;
        int lbsLocationRequested = 3014;    // start requesting LBS location from PureMonitor
        int lbsLocationStopRequesting = 3046;    // GPS is available again


    }


    public interface EventCategory {
        int OPEN_EVENT = 1;
        int CLOSE_EVENT = 0;
    }

    public interface ViolationCategoryTypes {
        int ENTER_INCLUSION = 1;

        int ENTER_EXCLUSION = 2;

        int VIOLATION_INSIDE_INCLUSION = 3;

        int VIOLATION_OUTSIDE_INCLUSION = 4;

        int VIOLATION_INSIDE_EXCLUSION = 5;

        int VIOLATION_CHARGING_AC = 6;

        int VIOLATION_TAG_STRAP_TAMPER = 7;

        int VIOLATION_TAG_PROXIMITY = 8;

        int VIOLATION_TAG_CASE_TAMPER = 9;

        int VIOLATION_SIM_CARD = 10;

        int VIOLATION_BEACON_TAMPER_CASE = 11;

        int VIOLATION_BEACON_TAMPER_PROXIMITY = 12;

        int VIOLATION_BEACON_MOTION_TAMPER = 22;

        int VIOLATION_BEACON_TAMPER_ZONE = 13;

        int VIOLATION_TAG_BATTERY_TAMPER = 14;

        int VIOLATION_CHARGING_USB = 15;

        int VIOLATION_SUDDEN_SHUT_DOWN = 16;

        int MONITORING_STARTED = 17;

        int BIOMETRIC_FAILED = 18;

        int SYNC = 19;

        int BATTERY = 20;

        int GPS_SIGNAL = 21;

        int UPGRADE_VERSION_FAILED = 24;

        int SETTINGS_MENU_LOGIN = 23;

        int SYSTEM_SMS_MESSAGE = 25;

        int TAG_ENCRYPTION = 27;

        int DEVICE_BATTERY_FULL = 28;

        int TAG_CONFIGURATION = 29;

        int ACTIVATED = 30;

        int START_PROFILE = 31;

        int BEACON_BATTERY_TAMPER = 32;

        int POWER = 33;

        int MESSAGE = 34;

        int UPGRADE_VERSION_SUCCEEDED = 35;

        int BIOMETRIC_SUCCEEDED = 36;

        int MOBILE_DATA = 38;

        int BEACON_ENCRYPTION = 39;

        int GPS_PROXIMITY_VIOLATION = 40;

        int GPS_PROXIMITY_WARNING = 41;

        int BUFFER_ZONE = 42;

        int HOME_RADIUS = 44;

        int ENROLLMENT = 45;

        int GPS_FRAUD_LOCATION = 46;

        int SIM_CARD_REPLACE = 47;

        int FLIGHT_MODE_STATE = 48;

        int INITIALIZED_RESTART = 49;

        int MOBILE_DATA_INITIALIZED_RESTART = 50;

        int LBS_REQUESTED = 51;

        int PINCODE_ATTEMPTS = 52;

        int GUEST_TAGS = 53;

        int PHOTO_TEST_CANCELED_BY_OFFENDER = 54;

        int PHOTO_TEST = 55;

        int DEVICE_SHIELDING = 56;

        int TAG_NO_MOTION = 57;

        int DEVICE_JAMMING = 58;

        int DEVICE_DIAGNOSTIC_REPORT = 59;

        int DEVICE_CASE_TAMPER = 60;
    }

    public interface ViolationSeverityTypes {
        int NORMAL = 1;
        int VIOLATION = 2;
        int ALARM = 3;
    }

    public interface ActionType {
        int NO_ACTION = 0;
        int EARLY_NETWORK_CYCLE = 1;
    }

    public interface Restrictions {
        int NORMAL = 0;
        int BEACON_ONLY = 1;
        int NOT_BEACON = 2;
    }

    public interface EventsAlarmsType {
        int SILENT = 0;
        int DEVICE_SETTINGS = 1;
        int TAG_PROXIMITY = 2;
    }


    // Table name
    private static final String TABLE_EVENT_CONFIG = "EventConfig";
    // Column names
    public static final String COLUMN_EVENT_TYPE = "EventType";
    private static final String COLUMN_IS_OPEN_EVENT = "IsOpenEvent";
    private static final String COLUMN_EVENT_DESCR = "EventDescr";
    private static final String COLUMN_VIOLATION_CATEGORY = "ViolationCategory";
    private static final String COLUMN_VIOLATION_SEVERITY = "ViolationSeverity";
    private static final String COLUMN_ACTION_TYPE = "columnActiontype";

    private EntityEventConfig _recEventConfig;

    public TableEventConfig() {
        this.tableName = TABLE_EVENT_CONFIG;
        this.columnNumber = 0;

        this.AddColumn(new DatabaseColumn(COLUMN_EVENT_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_IS_OPEN_EVENT, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_EVENT_DESCR, EnumDatabaseColumnType.COLUMN_TYPE_STRING));
        this.AddColumn(new DatabaseColumn(COLUMN_VIOLATION_CATEGORY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_VIOLATION_SEVERITY, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));
        this.AddColumn(new DatabaseColumn(COLUMN_ACTION_TYPE, EnumDatabaseColumnType.COLUMN_TYPE_INTEGER));

        // Build a string array of column names (useful for some queries)
//		BuildColumnNameArray();
        buildColumnNameArrayWithoutRowId();
    }

    @Override
    public long addRecord(SQLiteDatabase database, DatabaseEntity databaseEntity) {
        EntityEventConfig recEventConfig = (EntityEventConfig) databaseEntity;

        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_TYPE, recEventConfig.EventType);
        values.put(COLUMN_IS_OPEN_EVENT, recEventConfig.IsOpenEvent);
        values.put(COLUMN_EVENT_DESCR, recEventConfig.EventDescr);
        values.put(COLUMN_VIOLATION_CATEGORY, recEventConfig.ViolationCategory);
        values.put(COLUMN_VIOLATION_SEVERITY, recEventConfig.ViolationSeverity);
        values.put(COLUMN_ACTION_TYPE, recEventConfig.actionType);

        // Update local copy
        _recEventConfig = recEventConfig;
        // Insert the record
        return database.insert(TABLE_EVENT_CONFIG, null, values);
    }

    @Override
    public void AddDefaultData(SQLiteDatabase database) {

        for (EntityEventConfig eventConfigEntity : DefaultEventConfigValues.getDefaultEvents()) {
            addRecord(database, eventConfigEntity);
        }
    }


    private EntityEventConfig GetRecFromQueryCursor(Cursor QueryCursor) {
        return new EntityEventConfig(
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_EVENT_TYPE)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_IS_OPEN_EVENT)),
                QueryCursor.getString(QueryCursor.getColumnIndex(COLUMN_EVENT_DESCR)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_VIOLATION_CATEGORY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_VIOLATION_SEVERITY)),
                QueryCursor.getInt(QueryCursor.getColumnIndex(COLUMN_ACTION_TYPE))
        );
    }

    public EntityEventConfig GetRecord(SQLiteDatabase db) {
        Cursor QueryCursor = db.query(TABLE_EVENT_CONFIG, columnNamesArray, null, null, null, null, null);
        if (QueryCursor.getCount() == 0) {
            return null;
        } else {
            QueryCursor.moveToFirst();
        }

        return GetRecFromQueryCursor(QueryCursor);
    }

    public EntityEventConfig getRecordByEventType(int eventType) {
        EntityEventConfig recEventConfig = null;
        String Where = String.format("(%s = %d)", COLUMN_EVENT_TYPE, eventType);
        Cursor cursorQuery = databaseReference.query(TABLE_EVENT_CONFIG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...
        if (cursorQuery.getCount() > 0) {
            cursorQuery.moveToFirst();
            recEventConfig = GetRecFromQueryCursor(cursorQuery);
        }
        cursorQuery.close();

        return recEventConfig;
    }

    public boolean isCategoryHasCloseEvent(int categoryType) {
        String Where = String.format("(%s = %d) AND (%s = %d)",
                COLUMN_VIOLATION_CATEGORY, categoryType,
                COLUMN_IS_OPEN_EVENT, EventCategory.CLOSE_EVENT);
        Cursor cursorQuery = databaseReference.query(TABLE_EVENT_CONFIG,        // Table name
                columnNamesArray,        // Columns names
                Where,
                null,                // Group by...
                null,                // Having...
                null,            // Order by insert order
                "1");                // Limit...
        boolean isCatgeoryHasCloseEvent = cursorQuery.getCount() > 0;
        cursorQuery.close();

        return isCatgeoryHasCloseEvent;
    }

    @Override
    public void LoadData(SQLiteDatabase db) {
        _recEventConfig = this.GetRecord(db);
    }

    public void Update(EntityEventConfig Rec) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_TYPE, Rec.EventType);
        values.put(COLUMN_IS_OPEN_EVENT, Rec.IsOpenEvent);
        values.put(COLUMN_EVENT_DESCR, Rec.EventDescr);
        values.put(COLUMN_VIOLATION_CATEGORY, Rec.ViolationCategory);
        values.put(COLUMN_VIOLATION_SEVERITY, Rec.ViolationSeverity);
        values.put(COLUMN_ACTION_TYPE, Rec.actionType);

        databaseReference.update(TABLE_EVENT_CONFIG, values, null, null);
        // Load local copy
        LoadData(databaseReference);
    }

    public EntityEventConfig Get() {
        return _recEventConfig;
    }

    @Override
    public void alterTableAfterDatabaseUpgrade(SQLiteDatabase database, int oldVersion) {
        if (oldVersion >= 148 && oldVersion <= 161) {
            addRecord(database, new EntityEventConfig(
                    EventTypes.eventGuestTagEntered,
                    EventCategory.OPEN_EVENT,
                    "Guest tag detected",
                    ViolationCategoryTypes.GUEST_TAGS,
                    ViolationSeverityTypes.NORMAL,
                    ActionType.NO_ACTION
            ));

            addRecord(database, new EntityEventConfig(
                    EventTypes.eventGuestTagLeft,
                    EventCategory.CLOSE_EVENT,
                    "Guest tag left",
                    ViolationCategoryTypes.GUEST_TAGS,
                    ViolationSeverityTypes.NORMAL,
                    ActionType.NO_ACTION
            ));

            addRecord(database, new EntityEventConfig(
                    EventTypes.photoCanceledByTheOffender,
                    EventCategory.OPEN_EVENT,
                    "Photo canceled by offender",
                    ViolationCategoryTypes.PHOTO_TEST_CANCELED_BY_OFFENDER,
                    ViolationSeverityTypes.VIOLATION,
                    ActionType.EARLY_NETWORK_CYCLE
            ));

            addRecord(database, new EntityEventConfig(
                    EventTypes.photoTest,
                    EventCategory.OPEN_EVENT,
                    "Photo test",
                    ViolationCategoryTypes.PHOTO_TEST,
                    ViolationSeverityTypes.VIOLATION,
                    ActionType.EARLY_NETWORK_CYCLE
            ));
        }
        if (oldVersion >= 162 && oldVersion <= 167) {
            addRecord(database, new EntityEventConfig(
                    EventTypes.deviceShieldingOpen,
                    EventCategory.OPEN_EVENT,
                    "Device shielding open",
                    ViolationCategoryTypes.DEVICE_SHIELDING,
                    ViolationSeverityTypes.NORMAL,
                    ActionType.EARLY_NETWORK_CYCLE
            ));

            addRecord(database, new EntityEventConfig(
                    EventTypes.deviceShieldingClosed,
                    EventCategory.CLOSE_EVENT,
                    "Device shielding closed",
                    ViolationCategoryTypes.DEVICE_SHIELDING,
                    ViolationSeverityTypes.NORMAL,
                    ActionType.EARLY_NETWORK_CYCLE
            ));
        }
        if (oldVersion == 179) {
            addRecord(database, new EntityEventConfig(
                    EventTypes.tagNoMotion,
                    EventCategory.OPEN_EVENT,
                    "Tag no motion",
                    ViolationCategoryTypes.TAG_NO_MOTION,
                    ViolationSeverityTypes.VIOLATION,
                    ActionType.EARLY_NETWORK_CYCLE
            ));
            addRecord(database, new EntityEventConfig(
                    EventTypes.tagMotion,
                    EventCategory.CLOSE_EVENT,
                    "Tag motion",
                    ViolationCategoryTypes.TAG_NO_MOTION,
                    ViolationSeverityTypes.NORMAL,
                    ActionType.EARLY_NETWORK_CYCLE

            ));
        }
        switch (oldVersion) {
            case 186:
            case 187:
                addRecord(database, new EntityEventConfig(
                        EventTypes.deviceJammingTamper,
                        EventCategory.OPEN_EVENT,
                        "Jamming tamper",
                        ViolationCategoryTypes.DEVICE_JAMMING,
                        ViolationSeverityTypes.VIOLATION,
                        ActionType.NO_ACTION
                ));

                addRecord(database, new EntityEventConfig(
                        EventTypes.deviceJammingClosed,
                        EventCategory.CLOSE_EVENT,
                        "Jamming closed",
                        ViolationCategoryTypes.DEVICE_JAMMING,
                        ViolationSeverityTypes.VIOLATION,
                        ActionType.EARLY_NETWORK_CYCLE
                ));
                addRecord(database, new EntityEventConfig(
                        EventTypes.deviceDiagnosticReport,
                        EventCategory.CLOSE_EVENT,
                        "Self diagnostics event tamper",
                        ViolationCategoryTypes.DEVICE_DIAGNOSTIC_REPORT,
                        ViolationSeverityTypes.NORMAL,
                        ActionType.NO_ACTION
                ));
                break;
            case 191:
                addRecord(database, new EntityEventConfig(
                        EventTypes.deviceCaseTamperOpen,
                        EventCategory.OPEN_EVENT,
                        "Device Case Tamper Open",
                        ViolationCategoryTypes.DEVICE_CASE_TAMPER,
                        ViolationSeverityTypes.VIOLATION,
                        ActionType.EARLY_NETWORK_CYCLE
                ));
                addRecord(database, new EntityEventConfig(
                        EventTypes.deviceCaseTamperClosed,
                        EventCategory.CLOSE_EVENT,
                        "Device Case Tamper Closed",
                        ViolationCategoryTypes.DEVICE_CASE_TAMPER,
                        ViolationSeverityTypes.VIOLATION,
                        ActionType.EARLY_NETWORK_CYCLE
                ));
                break;
            case 192:
                database.execSQL("update " + tableName + " set " + COLUMN_VIOLATION_CATEGORY + " = " + ViolationCategoryTypes.DEVICE_CASE_TAMPER
                + " where " + COLUMN_EVENT_TYPE + " = " + EventTypes.deviceCaseTamperOpen);
                database.execSQL("update " + tableName + " set " + COLUMN_VIOLATION_CATEGORY + " = " + ViolationCategoryTypes.DEVICE_CASE_TAMPER
                + " where " + COLUMN_EVENT_TYPE + " = " + EventTypes.deviceCaseTamperClosed);
                break;
            case 199: // add lock screen event ended
                addRecord(database, new EntityEventConfig(
                        EventTypes.lockAfterPincodeAttemptsEnded,
                        EventCategory.CLOSE_EVENT,
                        "Lock screen attempts ended",
                        ViolationCategoryTypes.PINCODE_ATTEMPTS,
                        ViolationSeverityTypes.VIOLATION,
                        ActionType.NO_ACTION
                ));
        }
    }

}
