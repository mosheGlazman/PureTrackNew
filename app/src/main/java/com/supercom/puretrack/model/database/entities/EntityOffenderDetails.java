package com.supercom.puretrack.model.database.entities;

public class EntityOffenderDetails extends DatabaseEntity {
    public int offenderId;
    public String offenderSerialNumber;
    public int tagId;
    public String tagRfId;
    public String tagEncryption;
    public long programStart;
    public long programEnd;
    public int proximityRange;
    public int scheduleGrace;
    public String homeAddress;
    public String firstName;
    public String middleName;
    public String lastName;
    public String picPath;
    public String primaryPhone;
    public String secondaryPhone;
    public String officerName;
    public String agencyName;
    public String accelerometerSettings;

    public String beaconName;
    public String beaconId;
    public int beaconZoneId;
    public int beaconZoneVersion;
    public String beaconEncryption;
    public int beaconRange;
    public int locationGoodPointThreshold;
    public int locationBadPointThreshold;
    public String enrollmentScreenWizard;
    public long deviceElapsedRealTimeInMilli;
    public int offenderBiometricTimeout;
    public int offenderBiometricMinBetween;
    public int offenderDeviceConfigVersionNumber;
    public int offenderConfigVersionNumber;
    public int offenderMessageTimeout;
    public int offenderMessageExpire;
    public int OffEnableMessageResponse;
    public int OffOfficerNumON;
    public int OffAgencyNumON;
    public int OffTagType;
    public int OffDeveloperModeEnable;

    public String ServerUrl;
    public String ServerPass;
    public int DeviceConfigGpsCycleInterval;
    public int DeviceConfigGpsCycleBeaconInterval;
    public int DeviceConfigBadGpsAccuracyCounter;
    public int DeviceConfigNetCycleInterval;
    public int DeviceConfigNetCycleIntervalInsideBeacon;
    public int DeviceConfigNetCycleIntervalLow;
    public String DeviceConfigPhoneEmergency;
    public int DeviceConfigTimeZone;
    public int DeviceConfigDSTOffset;
    public String DeviceConfigFutureDST;
    public int UserClientCert;

    public int DeviceConfigPincodeEnable;
    public int DeviceConfigPincodeAttempts;
    public String DeviceConfigPincodePin;
    public int DeviceConfigPincodeLockTime;

    public int DeviceConfigFactoryReserEnable;
    public int DeviceConfigFactoryResetTimeout;

    public int DeviceConfigScheduleSettingsNumberOfDays;
    public String DeviceConfigIncomingCallsWhiteList;


    public String DeviceConfigAppsList;
    public String DeviceConfigPhoneOfficer;
    public String DeviceConfigPhoneAgency;
    public int OffenderConfigRssiHomeRange;
    public int OffenderConfigRssiOutsideRange;
    public int offenderConfigSchedExpiree;
    public int offenderConfigIsReqHandleSuccess;
    public int offenderConfigTimeSensitivityInsideBeacon;
    public int offenderConfigTimeSensitivityOutsideBeacon;
    public int offenderConfigTagProximityGraceTime;
    public int offenderConfigBeaconOutsideRangeGraceTime;
    public int offenderConfigPhonesActive;
    public int offenderConfigAllowedSpeed;
    public int offenderConfigLocationValidity;
    public int offenderConfigCaseClosedThreshold;
    public int offenderConfigSatelliteNumber;

    //Tag Settings
    public int offenderConfigTagSettingsTxinterval;
    public int offenderConfigTagSettingsIrOn;
    public int offenderConfigTagSettingsIrOff;
    public int offenderConfigTagSettingsCaseOpen;
    public int offenderConfigTagSettingsLowBatt;
    public int offenderConfigIsTagHeartBeatEnabled;
    public String offenderConfigTagAddress;
    public String offenderConfigBeaconAddress;
    public int offIsUsingConnectAsHeartbeat;
    public long offenderConfigTagHbCounter;
    public long offenderConfigTagADVCounter;
    public long offenderConfigTagHbInterval;
    public long offenderConfigTagHBVibrationTimeOut;
    public long offenderConfigTagHbEnableFromServer;
    public String offenderConfigTagConfigurations;

    public int offenderBleDebugInfoEnable;
    public int offenderIsBatteryIndicationEnabled;
    public int offenderIsDeviceCaseTamperEenabled;
    public String launcherConfigSettingsPassword;
    public int DeviceConfigDeviceLock;
    public String eventsAlramsJson;
    public String locationTypes;
    public String pmComProfileJson;
    public String profileEventsJson;
    public String homeAddressSettingsJson;
    public float homeLat;
    public float homeLong;
    public int dialerBlocker;
    public int offenderIsEmergencyEnabled;
    public String DeviceConfigCellularApn;
    public int offenderDeviceCaseTamperValidity;
    public int offenderDeviceCaseTamperCalibraion;
    public int offenderDeviceCaseTamperXMagnetThreshold;
    public int offenderDeviceCaseTamperYMagnetThreshold;
    public int offenderDeviceCaseTamperZMagnetThreshold;
    public int offenderDeviceCaseTamperRecalibrationEnabled;
    public int offenderDeviceCaseTamperRecalibrationTimerInMinutes;
    public int commNetworkTest;
    public String offenderConfigKnoxSettingsJson;

    public int locationWeightedAverage;
    public int locationSmoothing;
    public int locationSmoothingActivation;
    public int locationServiceInterval;
    public int locationAverageTimeFrame;
    public int locationServiceCalcType;
    public String backgroundAppWhiteList;
    public int officerModeTimeout;
    public String voipSettings;
    public String appLanguage;
    public int customCallInterface;
    public int ignoreSslCert;

    public String batteryThreshold;
    public int lbsEnable;
    public int lbsThresholdNormal;
    public int lbsThresholdViolation;
    public int lbsIntervalNormal;
    public int lbsIntervalViolation;
    public int lbsStopValidity;
    public String OffenderMapUrl;
    public int debugInfoConfig;
    public int deviceInfoCycles;
    public int turnOnScreen;
    public String allowedEventsWhileSuspend;
    public int gpsIntervalNoMotion;

    public int zoneDriftEnable;
    public int zoneDriftInterval;
    public int zoneDriftDuration;
    public int zoneDriftLocations;

    public int guestTagEnabled;
    public int guestTagTime;

    public int purecomAsHomeUnit;


    public EntityOffenderDetails(int offenderId,
                                 String offenderSerialNumber,
                                 int TagId,
                                 String TagRfId,
                                 String TagEncryption,
                                 long ProgramStart,
                                 long ProgramEnd,
                                 int proximityRange,
                                 int ScheduleGrace,
                                 String HomeAddress,
                                 String FirstName,
                                 String MiddleName,
                                 String LastName,
                                 String PicPath,
                                 String PrimaryPhone,
                                 String SecondaryPhone,
                                 String OfficerName,
                                 String AgencyName,

                                 String BeaconName,
                                 String BeaconId,
                                 int BeaconZoneId,
                                 int BeaconZoneVersion,
                                 String beaconEncryption,
                                 int beaconRange,
                                 int locationGoodPointThreshold,
                                 int locationBadPointThreshold,
                                 String enrollmentScreenWizard,
                                 long DeviceElapsedRealTimeInMilli,
                                 int offenderBiometricTimeout,
                                 int offenderBiometricMinBetween,
                                 int OffenderDeviceConfigVersionNumber,
                                 int OffenderConfigVersionNumber,
                                 int offenderMessageTimeout,
                                 int offenderMessageExpire,
                                 int EnableMessageResponse,
                                 int OffOfficerNumON,
                                 int OffAgencyNumON,
                                 int OffTagType,
                                 int OffDeveloperModeEnable,
                                 String ServerUrl,
                                 String ServerPass,
                                 int DeviceConfigNetCycleIntervalOutsideBeacon,
                                 int DeviceConfigNetCycleIntervalInsideBeacon,
                                 int DeviceConfigNetCycleIntervalLow,
                                 String DeviceConfigPhoneEmergency,
                                 int DeviceConfigTimeZone,
                                 int DeviceConfigDSTOffset,
                                 String DeviceConfigFutureDST,
                                 int UserClientCert,
                                 int DeviceConfigPincodeEnable,
                                 int DeviceConfigPincodeAttempts,
                                 String DeviceConfigPincodePin,
                                 int DeviceConfigPincodeLockTime,
                                 int DeviceConfigFactoryReserEnable,
                                 int DeviceConfigFactoryResetTimeout,
                                 int DeviceConfigScheduleSettingsNumberOfDays,
                                 int DeviceConfigGpsCycleInterval,
                                 int DeviceConfigGpsCycleBeaconInterval,
                                 int DeviceconfigBadGpsAccuracyCounter,

                                 String DeviceConfigBlockIncomingList,
                                 String DeviceConfigAppsList,
                                 String DeviceConfigPhoneOfficer,
                                 String DeviceConfigPhoneAgency,
                                 int OffenderConfigRssiHomeRange,
                                 int OffenderConfigRssiOutsideRange,
                                 int offenderConfigSchedExpiree,
                                 int offenderConfigIsReqHandleSuccess,
                                 int offenderConfigTimeSensitivityInsideBeacon,
                                 int offenderConfigTimeSensitivityOutsideBeacon,
                                 int offenderConfigTagProximityGraceTime,
                                 int offenderConfigBeaconProximityGraceTime,
                                 int offenderConfigPhonesActive,
                                 int offenderConfigAllowedSpeed,
                                 int offenderConfigLocationValidity,
                                 int offenderConfigSatelliteNumber,

                                 int offenderConfigTagSettingsTxinterval,
                                 int offenderConfigTagSettingsIrOn,
                                 int offenderConfigTagSettingsIrOff,
                                 int offenderConfigTagSettingsCaseOpen,
                                 int offenderConfigTagSettingsLowBatt,
                                 int offenderConfigIsTagHeartBeatEnabled,
                                 String offenderConfigTagAddress,
                                 String offenderConfigBeaconAddress,
                                 int offIsUsingConnectAsHeartbeat,
                                 long offenderConfigTagHbCounter,
                                 long offenderConfigTagADVCounter,
                                 long offenderConfigTagHBInterval,
                                 long offenderConfigTagHBVibrationTimeOut,
                                 long offenderConfigTagHbEnable,
                                 String offenderConfigTagConfigurations,

                                 int offenderBleLogEnable,
                                 int offenderIsBatteryIndicationEnabled,
                                 int offenderIsDeviceCaseTamperEenabled,

                                 String launcherConfigSettingsPassword,
                                 int DeviceConfigDeviceLock,

                                 String eventsAlramsJson,
                                 String locationTypes,
                                 String pmComProfile,
                                 String profileEventsJson,
                                 String homeAddressSettingsJson,
                                 float homeLat,
                                 float homeLong,
                                 int dialerBlocker,
                                 int offenderIsEmergencyEnabled,
                                 String DeviceConfigCellularApn,
                                 int offenderDeviceCaseTamperValidity,
                                 int offenderConfigCaseClosedThreshold,
                                 int offenderDeviceCaseTamperCalibraion,
                                 int offenderDeviceCaseTamperXMagnetThreshold,
                                 int offenderDeviceCaseTamperYMagnetThreshold,
                                 int offenderDeviceCaseTamperZMagnetThreshold,
                                 int offenderDeviceCaseTamperRecalibrationEnabled,
                                 int offenderDeviceCaseTamperRecalibrationTimerInMinutes,
                                 int commNetworkTest,
                                 String offenderConfigKnoxSettingsJson,
                                 int locationWeightedAverage,
                                 int locationSmoothing,
                                 int locationSmoothingActivation,
                                 int locationServiceInterval,
                                 int locationAverageTimeFrame,
                                 int locationServiceCalcType,
                                 String backgroundAppWhiteList,
                                 int officerModeTimeout,
                                 String voipSettings,
                                 String appLanguage,
                                 int customCallInterface,
                                 int ignoreSslCert,
                                 String batteryThreshold,
                                 int lbsEnable,
                                 int lbsThresholdNormal,
                                 int lbsThresholdViolation,
                                 int lbsIntervalNormal,
                                 int lbsIntervalViolation,
                                 int lbsStopValidity,
                                 String OffenderMapUrl,
                                 int debugInfoConfig,
                                 int deviceInfoCycles,
                                 int turnOnScreen,
                                 String allowedEventsWhileSuspend,
                                 String accelerometerSettings,
                                 int gpsIntervalNoMotion,
                                 int zoneDriftEnable,
                                 int zoneDriftInterval,
                                 int zoneDriftDuration,
                                 int zoneDriftLocations,
                                 int guestTagEnabled,
                                 int guestTagTime,
                                 int purecomAsHomeUnit) {
        this.offenderId = offenderId;
        this.offenderSerialNumber = offenderSerialNumber;
        this.tagId = TagId;
        this.tagRfId = TagRfId;
        this.tagEncryption = TagEncryption;
        this.programStart = ProgramStart;
        this.programEnd = ProgramEnd;
        this.proximityRange = proximityRange;
        this.scheduleGrace = ScheduleGrace;
        this.homeAddress = HomeAddress;
        this.firstName = FirstName;
        this.middleName = MiddleName;
        this.lastName = LastName;
        this.picPath = PicPath;
        this.primaryPhone = PrimaryPhone;
        this.secondaryPhone = SecondaryPhone;
        this.officerName = OfficerName;
        this.agencyName = AgencyName;

        this.beaconName = BeaconName;
        this.beaconId = BeaconId;
        this.beaconZoneId = BeaconZoneId;
        this.beaconZoneVersion = BeaconZoneVersion;
        this.beaconEncryption = beaconEncryption;
        this.beaconRange = beaconRange;

        this.locationGoodPointThreshold = locationGoodPointThreshold;
        this.locationBadPointThreshold = locationBadPointThreshold;
        this.enrollmentScreenWizard = enrollmentScreenWizard;
        this.deviceElapsedRealTimeInMilli = DeviceElapsedRealTimeInMilli;
        this.offenderBiometricTimeout = offenderBiometricTimeout;
        this.offenderBiometricMinBetween = offenderBiometricMinBetween;
        this.offenderDeviceConfigVersionNumber = OffenderDeviceConfigVersionNumber;
        this.offenderConfigVersionNumber = OffenderConfigVersionNumber;
        this.offenderMessageTimeout = offenderMessageTimeout;
        this.offenderMessageExpire = offenderMessageExpire;
        this.OffEnableMessageResponse = EnableMessageResponse;
        this.OffOfficerNumON = OffOfficerNumON;
        this.OffAgencyNumON = OffAgencyNumON;
        this.OffTagType = OffTagType;
        this.OffDeveloperModeEnable = OffDeveloperModeEnable;

        this.ServerUrl = ServerUrl;
        this.ServerPass = ServerPass;
        this.DeviceConfigNetCycleInterval = DeviceConfigNetCycleIntervalOutsideBeacon;
        this.DeviceConfigNetCycleIntervalInsideBeacon = DeviceConfigNetCycleIntervalInsideBeacon;
        this.DeviceConfigNetCycleIntervalLow = DeviceConfigNetCycleIntervalLow;
        this.DeviceConfigPhoneEmergency = DeviceConfigPhoneEmergency;
        this.DeviceConfigTimeZone = DeviceConfigTimeZone;
        this.DeviceConfigDSTOffset = DeviceConfigDSTOffset;
        this.DeviceConfigFutureDST = DeviceConfigFutureDST;
        this.UserClientCert = UserClientCert;
        this.DeviceConfigPincodeEnable = DeviceConfigPincodeEnable;
        this.DeviceConfigPincodeAttempts = DeviceConfigPincodeAttempts;
        this.DeviceConfigPincodePin = DeviceConfigPincodePin;
        this.DeviceConfigPincodeLockTime = DeviceConfigPincodeLockTime;

        this.DeviceConfigFactoryReserEnable = DeviceConfigFactoryReserEnable;
        this.DeviceConfigFactoryResetTimeout = DeviceConfigFactoryResetTimeout;

        this.DeviceConfigScheduleSettingsNumberOfDays = DeviceConfigScheduleSettingsNumberOfDays;
        this.DeviceConfigIncomingCallsWhiteList = DeviceConfigBlockIncomingList;
        this.DeviceConfigAppsList = DeviceConfigAppsList;
        this.DeviceConfigPhoneOfficer = DeviceConfigPhoneOfficer;
        this.DeviceConfigPhoneAgency = DeviceConfigPhoneAgency;
        this.DeviceConfigGpsCycleInterval = DeviceConfigGpsCycleInterval;
        this.DeviceConfigGpsCycleBeaconInterval = DeviceConfigGpsCycleBeaconInterval;
        this.DeviceConfigBadGpsAccuracyCounter = DeviceconfigBadGpsAccuracyCounter;
        this.OffenderConfigRssiHomeRange = OffenderConfigRssiHomeRange;
        this.OffenderConfigRssiOutsideRange = OffenderConfigRssiOutsideRange;
        this.offenderConfigSchedExpiree = offenderConfigSchedExpiree;
        this.offenderConfigIsReqHandleSuccess = offenderConfigIsReqHandleSuccess;
        this.offenderConfigTimeSensitivityInsideBeacon = offenderConfigTimeSensitivityInsideBeacon;
        this.offenderConfigTimeSensitivityOutsideBeacon = offenderConfigTimeSensitivityOutsideBeacon;
        this.offenderConfigTagProximityGraceTime = offenderConfigTagProximityGraceTime;
        this.offenderConfigBeaconOutsideRangeGraceTime = offenderConfigBeaconProximityGraceTime;
        this.offenderConfigPhonesActive = offenderConfigPhonesActive;
        this.offenderConfigAllowedSpeed = offenderConfigAllowedSpeed;
        this.offenderConfigLocationValidity = offenderConfigLocationValidity;
        this.offenderConfigSatelliteNumber = offenderConfigSatelliteNumber;

        this.offenderConfigTagSettingsTxinterval = offenderConfigTagSettingsTxinterval;
        this.offenderConfigTagSettingsIrOn = offenderConfigTagSettingsIrOn;
        this.offenderConfigTagSettingsIrOff = offenderConfigTagSettingsIrOff;
        this.offenderConfigTagSettingsCaseOpen = offenderConfigTagSettingsCaseOpen;
        this.offenderConfigTagSettingsLowBatt = offenderConfigTagSettingsLowBatt;

        this.offenderConfigIsTagHeartBeatEnabled = offenderConfigIsTagHeartBeatEnabled;
        this.offenderConfigTagAddress = offenderConfigTagAddress;
        this.offenderConfigBeaconAddress = offenderConfigBeaconAddress;
        this.offIsUsingConnectAsHeartbeat = offIsUsingConnectAsHeartbeat;
        this.offenderConfigTagHbCounter = offenderConfigTagHbCounter;
        this.offenderConfigTagADVCounter = offenderConfigTagADVCounter;
        this.offenderConfigTagHbInterval = offenderConfigTagHBInterval;
        this.offenderConfigTagHBVibrationTimeOut = offenderConfigTagHBVibrationTimeOut;
        this.offenderConfigTagHbEnableFromServer = offenderConfigTagHbEnable;
        this.offenderConfigTagConfigurations = offenderConfigTagConfigurations;

        this.offenderBleDebugInfoEnable = offenderBleLogEnable;
        this.offenderIsBatteryIndicationEnabled = offenderIsBatteryIndicationEnabled;
        this.offenderIsDeviceCaseTamperEenabled = offenderIsDeviceCaseTamperEenabled;

        this.launcherConfigSettingsPassword = launcherConfigSettingsPassword;
        this.DeviceConfigDeviceLock = DeviceConfigDeviceLock;
        this.eventsAlramsJson = eventsAlramsJson;
        this.locationTypes = locationTypes;
        this.pmComProfileJson = pmComProfile;
        this.profileEventsJson = profileEventsJson;
        this.homeAddressSettingsJson = homeAddressSettingsJson;
        this.homeLat = homeLat;
        this.homeLong = homeLong;
        this.dialerBlocker = dialerBlocker;
        this.offenderIsEmergencyEnabled = offenderIsEmergencyEnabled;
        this.DeviceConfigCellularApn = DeviceConfigCellularApn;
        this.offenderDeviceCaseTamperValidity = offenderDeviceCaseTamperValidity;
        this.offenderConfigCaseClosedThreshold = offenderConfigCaseClosedThreshold;
        this.offenderDeviceCaseTamperCalibraion = offenderDeviceCaseTamperCalibraion;
        this.offenderDeviceCaseTamperXMagnetThreshold = offenderDeviceCaseTamperXMagnetThreshold;
        this.offenderDeviceCaseTamperYMagnetThreshold = offenderDeviceCaseTamperYMagnetThreshold;
        this.offenderDeviceCaseTamperZMagnetThreshold = offenderDeviceCaseTamperZMagnetThreshold;
        this.offenderDeviceCaseTamperRecalibrationEnabled = offenderDeviceCaseTamperRecalibrationEnabled;
        this.offenderDeviceCaseTamperRecalibrationTimerInMinutes = offenderDeviceCaseTamperRecalibrationTimerInMinutes;
        this.commNetworkTest = commNetworkTest;
        this.offenderConfigKnoxSettingsJson = offenderConfigKnoxSettingsJson;
        this.locationWeightedAverage = locationWeightedAverage;
        this.locationSmoothing = locationSmoothing;
        this.locationSmoothingActivation = locationSmoothingActivation;
        this.locationServiceInterval = locationServiceInterval;
        this.locationAverageTimeFrame = locationAverageTimeFrame;
        this.locationServiceCalcType = locationServiceCalcType;
        this.backgroundAppWhiteList = backgroundAppWhiteList;
        this.officerModeTimeout = officerModeTimeout;
        this.voipSettings = voipSettings;
        this.appLanguage = appLanguage;
        this.customCallInterface = customCallInterface;
        this.ignoreSslCert = ignoreSslCert;
        this.batteryThreshold = batteryThreshold;

        this.lbsEnable = lbsEnable;
        this.lbsThresholdNormal = lbsThresholdNormal;
        this.lbsThresholdViolation = lbsThresholdViolation;
        this.lbsIntervalNormal = lbsIntervalNormal;
        this.lbsIntervalViolation = lbsIntervalViolation;
        this.lbsStopValidity = lbsStopValidity;
        this.OffenderMapUrl = OffenderMapUrl;
        this.debugInfoConfig = debugInfoConfig;
        this.deviceInfoCycles = deviceInfoCycles;
        this.turnOnScreen = turnOnScreen;
        this.allowedEventsWhileSuspend = allowedEventsWhileSuspend;
        this.accelerometerSettings = accelerometerSettings;
        this.gpsIntervalNoMotion = gpsIntervalNoMotion;
        this.zoneDriftEnable = zoneDriftEnable;
        this.zoneDriftInterval = zoneDriftInterval;
        this.zoneDriftDuration = zoneDriftDuration;
        this.zoneDriftLocations = zoneDriftLocations;
        this.guestTagEnabled = guestTagEnabled;
        this.guestTagTime = guestTagTime;
        this.purecomAsHomeUnit = purecomAsHomeUnit;
    }
}
