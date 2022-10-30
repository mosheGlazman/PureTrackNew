package com.supercom.puretrack.model.database.entities;

public class EntityOffenderStatus extends DatabaseEntity {
    public int OffVioStat;
    public int OffTagBatteryLevel;
    public int OffIsInRange;
    public int OffTagStatCase;
    public int OffTagStatStrap;
    public int OffTagStatBattery;
    public int OffTagStatMotion;
    public int OffTagLastReceive;
    public int OffInBeaconZone;
    public int OffBeaconStatCase;
    public int OffBeaconStatProx;
    public int OffBeaconStatMotion;
    public int OffBeaconStatBattery;
    public int OffBeaconStatCaseIndex;
    public int OffBeaconStatMotionIndex;
    public int OffBeaconStatProxIndex;
    public int OffBeaconHasOpenEvent;
    public int OffTagStatCaseIndex;
    public int OffTagStatStrapIndex;

    public int OffDeviceBatteryStat;
    public int OffDeviceBatteryPercentage;
    public String LastGpsPointRecordJsonStr;
    public int OffZoneVersion;
    public long OffLastScheduleUpdate;
    public int offScheduleOfZonesBiometricTestsCounter;
    public long offScheduleOfZonesBiometricLastCheck;
    public String offDeviceDownloadedVersion;
    public int offDidOffenderGetValidAuthenticationForFirstTime;
    public int offIsOffenderActivated;
    public String offSmallestAccuracyPointAndAboveGoodThreshold;
    public int offLastCreatedEventType;
    public int offLastOffenderRequestIdStatus;
    public int offLastOffenderRequestStatus;
    public int offActivationOffenderRequestIdTreated;
    public String offLastSyncResponseFromServerJson;
    public int offCurrentPmComProfile;
    public int isMobileDataEnabled;
    public int offTagTxIndex;
    public int offBeaconTxIndex;
    public int offCurrentCommNetworkTestStatus;
    public int startNetworkStatusCounter;

    public String failedHandleRequestsJson;
    public int OffDeviceTemperature;
    public int OffBeaconBatteryLevel;
    public int OffBeaconLastReceive;
    public int isCycleFinishedSuccessfuly;
    public String ofSimICCID;
    public int timeInitiatedFlightModeEnd;
    public long lastLocationUtcTime;
    public long lastLbsLocationUtcTime;
    public int locked_status;
    public int lastSuccessfulyCom;
    public int offenderInBeaconZone;
    public int tagMotion;
    public int tagStatMotionIndex;


    public EntityOffenderStatus(
            int OffVioStat,
            int OffTagBatteryLevel,
            int OffIsInRange,
            int OffTagStatCase,
            int OffTagStatStrap,
            int OffTagStatBattery,
            int OffTagStatMotion,
            int OffTagLastReceive,
            int OffInBeaconZone,
            int OffBeaconStatCase,
            int OffBeaconStatProx,
            int OffBeaconStatMotion,
            int OffBeaconStatBattery,
            int OffBeaconStatCaseIndex,
            int OffBeaconStatMotionIndex,
            int OffBeaconStatProxIndex,
            int OffBeaconHasOpenEvent,
            int OffTagStatCaseIndex,
            int OffTagStatStrapIndex,
            int OffDeviceBatteryStat,
            int OffDeviceBatteryPercentage,
            String LastGPSPointRecordJsonStr,
            int OffZoneVersion,
            long OffLastScheduleUpdate,
            int offScheduleOfZonesBiometricTestsCounter,
            long offScheduleOfZonesBiometricLastCheck,
            String offDeviceDownloadedVersion,
            int offDidOffenderGetValidAuthenticationForFirstTime,
            int offIsOffenderActivated,
            String smallestAccuracyPointAndAboveGoodThreshold,
            int offLastCreatedEvent,
            int offLastOffenderRequestIdTreated,
            int offLastOffenderRequestStatus,
            int offActivationOffenderRequestIdTreated,
            String offLastSyncResponseFromServerJson,
            int offCurrentPmComProfile,
            int isMobileDataEnabled,
            int OffTagTxIndex,
            int OffBeaconTxIndex,
            int offCurrentCommNetworkTestStatus,
            int startNetworkStatusCounter,
            String failedHandleRequestsJson,
            int OffDeviceTemperature,
            int OffBeaconBatt,
            int OffBeaconLastReceive,
            int isCycleFinishedSuccessfuly,
            String ofSimICCID,
            int timeInitiatedFlightModeEnd,
            long lastLocationUtcTime,
            long lastLbsLocationUtcTime,
            int locked_status,
            int lastSuccessfulyCom,
            int offenderInBeaconZone,
            int tagMotion,
            int tagStatMotionIndex
    ) {
        this.OffVioStat = OffVioStat;
        this.OffTagBatteryLevel = OffTagBatteryLevel;
        this.OffIsInRange = OffIsInRange;
        this.OffTagStatCase = OffTagStatCase;
        this.OffTagStatStrap = OffTagStatStrap;
        this.OffTagStatBattery = OffTagStatBattery;
        this.OffTagStatMotion = OffTagStatMotion;
        this.OffTagLastReceive = OffTagLastReceive;
        this.OffInBeaconZone = OffInBeaconZone;
        this.OffBeaconStatCase = OffBeaconStatCase;
        this.OffBeaconStatProx = OffBeaconStatProx;
        this.OffBeaconStatMotion = OffBeaconStatMotion;
        this.OffBeaconStatBattery = OffBeaconStatBattery;
        this.OffBeaconStatCaseIndex = OffBeaconStatCaseIndex;
        this.OffBeaconStatMotionIndex = OffBeaconStatMotionIndex;
        this.OffBeaconStatProxIndex = OffBeaconStatProxIndex;
        this.OffBeaconHasOpenEvent = OffBeaconHasOpenEvent;
        this.OffTagStatCaseIndex = OffTagStatCaseIndex;
        this.OffTagStatStrapIndex = OffTagStatStrapIndex;
        this.OffDeviceBatteryStat = OffDeviceBatteryStat;
        this.OffDeviceBatteryPercentage = OffDeviceBatteryPercentage;
        this.LastGpsPointRecordJsonStr = LastGPSPointRecordJsonStr;
        this.OffZoneVersion = OffZoneVersion;
        this.OffLastScheduleUpdate = OffLastScheduleUpdate;
        this.offScheduleOfZonesBiometricTestsCounter = offScheduleOfZonesBiometricTestsCounter;
        this.offScheduleOfZonesBiometricLastCheck = offScheduleOfZonesBiometricLastCheck;
        this.offDeviceDownloadedVersion = offDeviceDownloadedVersion;
        this.offDidOffenderGetValidAuthenticationForFirstTime = offDidOffenderGetValidAuthenticationForFirstTime;
        this.offIsOffenderActivated = offIsOffenderActivated;
        this.offSmallestAccuracyPointAndAboveGoodThreshold = smallestAccuracyPointAndAboveGoodThreshold;
        this.offLastCreatedEventType = offLastCreatedEvent;
        this.offLastOffenderRequestIdStatus = offLastOffenderRequestIdTreated;
        this.offLastOffenderRequestStatus = offLastOffenderRequestStatus;
        this.offActivationOffenderRequestIdTreated = offActivationOffenderRequestIdTreated;
        this.offLastSyncResponseFromServerJson = offLastSyncResponseFromServerJson;
        this.offCurrentPmComProfile = offCurrentPmComProfile;
        this.isMobileDataEnabled = isMobileDataEnabled;
        this.offTagTxIndex = OffTagTxIndex;
        this.offBeaconTxIndex = OffBeaconTxIndex;
        this.offCurrentCommNetworkTestStatus = offCurrentCommNetworkTestStatus;
        this.startNetworkStatusCounter = startNetworkStatusCounter;
        this.failedHandleRequestsJson = failedHandleRequestsJson;
        this.OffDeviceTemperature = OffDeviceTemperature;
        this.OffBeaconBatteryLevel = OffBeaconBatt;
        this.OffBeaconLastReceive = OffBeaconLastReceive;
        this.isCycleFinishedSuccessfuly = isCycleFinishedSuccessfuly;
        this.ofSimICCID = ofSimICCID;
        this.timeInitiatedFlightModeEnd = timeInitiatedFlightModeEnd;
        this.lastLocationUtcTime = lastLocationUtcTime;
        this.lastLbsLocationUtcTime = lastLbsLocationUtcTime;
        this.locked_status = locked_status;
        this.lastSuccessfulyCom = lastSuccessfulyCom;
        this.offenderInBeaconZone = offenderInBeaconZone;
        this.tagMotion = tagMotion;
        this.tagStatMotionIndex = tagStatMotionIndex;
    }
}
