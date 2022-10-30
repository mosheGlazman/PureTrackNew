package com.supercom.puretrack.util.constants.database_defaults;

import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.model.database.entities.EntityEventConfig;

import java.util.ArrayList;
import java.util.List;

public class DefaultEventConfigValues {

    public static List<EntityEventConfig> getDefaultEvents() {
        List<EntityEventConfig> defaultEventsList = new ArrayList<>();
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.settingsMenuLoginPerformed,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "User has successful logged into the settings via PureTrack ( entered the correct password). ",
                TableEventConfig.ViolationCategoryTypes.SETTINGS_MENU_LOGIN,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.settingsMenuLoginFailure,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "User has tried to login to the settings via PureTrack, but has failed to enter the correct password 2 consecutive times. ",
                TableEventConfig.ViolationCategoryTypes.SETTINGS_MENU_LOGIN,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.lbsLocationRequested,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "LBS Location Requested",
                TableEventConfig.ViolationCategoryTypes.LBS_REQUESTED,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.lbsLocationStopRequesting,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "GPS location restored after LBS",
                TableEventConfig.ViolationCategoryTypes.LBS_REQUESTED,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.sysSmsReceived,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "System SMS message received",
                TableEventConfig.ViolationCategoryTypes.SYSTEM_SMS_MESSAGE,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.sysSmsConditionsNotMet,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "System SMS SyncNow: Condition not met.",
                TableEventConfig.ViolationCategoryTypes.SYSTEM_SMS_MESSAGE,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.noFingerprintRegistered,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Biometric Test Failed - No Fingerprint Registered",
                TableEventConfig.ViolationCategoryTypes.BIOMETRIC_FAILED,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.tagEncryptionError,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Tag encryption error",
                TableEventConfig.ViolationCategoryTypes.TAG_ENCRYPTION,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.tagEncryptionRecovered,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Tag encryption recovered",
                TableEventConfig.ViolationCategoryTypes.TAG_ENCRYPTION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceBatteryFull,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Device battery full",
                TableEventConfig.ViolationCategoryTypes.DEVICE_BATTERY_FULL,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.tagSetupSuccess,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Tag Setup Performed",
                TableEventConfig.ViolationCategoryTypes.TAG_CONFIGURATION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.tagSetupFailed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Tag Setup Failed",
                TableEventConfig.ViolationCategoryTypes.TAG_CONFIGURATION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.pendingEnrolment,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Pending Enrollment",
                TableEventConfig.ViolationCategoryTypes.ACTIVATED,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.offenderEnrolmentPerformed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Offender enrolment performed",
                TableEventConfig.ViolationCategoryTypes.ACTIVATED,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.startProfile,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Start Profile",
                TableEventConfig.ViolationCategoryTypes.START_PROFILE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.endProfile,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "End Profile",
                TableEventConfig.ViolationCategoryTypes.START_PROFILE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.beaconBatteryTamper,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Beacon Battery Tamper",
                TableEventConfig.ViolationCategoryTypes.BEACON_BATTERY_TAMPER,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.beaconBatteryTamperClosed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Beacon Battery Tamper Close",
                TableEventConfig.ViolationCategoryTypes.BEACON_BATTERY_TAMPER,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventPowerOff,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Power Off",
                TableEventConfig.ViolationCategoryTypes.POWER,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventConnectionUnavailableDeviceRestart,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Connection Unavaliable Device Restart",
                TableEventConfig.ViolationCategoryTypes.INITIALIZED_RESTART,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventPowerOn,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Power On",
                TableEventConfig.ViolationCategoryTypes.POWER,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventMessageAck,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Message Ack",
                TableEventConfig.ViolationCategoryTypes.MESSAGE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventMessageTimeOut,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Message Time Out",
                TableEventConfig.ViolationCategoryTypes.MESSAGE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventNewOffenderMessage,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "event New Offender Message",
                TableEventConfig.ViolationCategoryTypes.MESSAGE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.mobileDataDisabled,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Mobile data disabled",
                TableEventConfig.ViolationCategoryTypes.MOBILE_DATA,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.mobileDataRestored,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Mobile data enabled",
                TableEventConfig.ViolationCategoryTypes.MOBILE_DATA,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.beaconEncryptionError,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Beacon encryption error",
                TableEventConfig.ViolationCategoryTypes.BEACON_ENCRYPTION,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.beaconEncryptionRecovered,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Beacon encryption recovered",
                TableEventConfig.ViolationCategoryTypes.BEACON_ENCRYPTION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.offenderFingerprintScanned,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Offender Fingerprint Scanned",
                TableEventConfig.ViolationCategoryTypes.ENROLLMENT,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.tag_beaconVerified,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Tag/ Beacon Verified",
                TableEventConfig.ViolationCategoryTypes.ENROLLMENT,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.knoxActivatedOnDevice,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "KNOX Activated on Device",
                TableEventConfig.ViolationCategoryTypes.ENROLLMENT,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.deviceLocationVerified,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Device Location Verified",
                TableEventConfig.ViolationCategoryTypes.ENROLLMENT,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.gpsFraudLocation,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Gps Fraud Location",
                TableEventConfig.ViolationCategoryTypes.GPS_FRAUD_LOCATION,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.gpsFraudLocationClosed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Gps Fraud Location Closed",
                TableEventConfig.ViolationCategoryTypes.GPS_FRAUD_LOCATION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.simCardReplaced,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Sim Card Replaced",
                TableEventConfig.ViolationCategoryTypes.SIM_CARD_REPLACE,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.flightModeEnabled,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Device in Flight Mode",
                TableEventConfig.ViolationCategoryTypes.FLIGHT_MODE_STATE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.flightModeDisabled,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Device exited Flight Mode",
                TableEventConfig.ViolationCategoryTypes.FLIGHT_MODE_STATE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventConnectionUnavailableDeviceRestart,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Connection Unavaliable Device Restart",
                TableEventConfig.ViolationCategoryTypes.INITIALIZED_RESTART,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventDeviceStartupAfterRestart,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Device Startup After Restart",
                TableEventConfig.ViolationCategoryTypes.INITIALIZED_RESTART,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.applicationInitializedMobileDataRestart,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Application initialized Mobile data restart",
                TableEventConfig.ViolationCategoryTypes.MOBILE_DATA_INITIALIZED_RESTART,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.enteredBufferOfInclusionZone,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Entered Buffer of Inclusion Zone During Curfew",
                TableEventConfig.ViolationCategoryTypes.BUFFER_ZONE,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.presentInBufferOfInclusionZone,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Present in Buffer of Inclusion Zone During Curfew ",
                TableEventConfig.ViolationCategoryTypes.BUFFER_ZONE,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.enteredBufferOfExclusionZone,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Entered Buffer of Exclusion Zone During Curfew",
                TableEventConfig.ViolationCategoryTypes.BUFFER_ZONE,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.presentInBufferOfExclusionZone,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Present in Buffer of Exclusion Zone During Curfew ",
                TableEventConfig.ViolationCategoryTypes.BUFFER_ZONE,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.exitedBufferOfInclusionZone,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Exited Buffer of Inclusion Zone",
                TableEventConfig.ViolationCategoryTypes.BUFFER_ZONE,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.exitedBufferOfExclusionZone,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Exited Buffer of Exclusion Zone",
                TableEventConfig.ViolationCategoryTypes.BUFFER_ZONE,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventEnteredInclusionZone,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Entered Inclusion Zone",
                TableEventConfig.ViolationCategoryTypes.ENTER_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventExitedInclusionZone,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Exited Inclusion Zone",
                TableEventConfig.ViolationCategoryTypes.ENTER_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventEnteredExclusionZone,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Entered Exclusion Zone",
                TableEventConfig.ViolationCategoryTypes.ENTER_EXCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventExitedExclusionZone,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Exited Exclusion Zone",
                TableEventConfig.ViolationCategoryTypes.ENTER_EXCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.PresentInInclusionZoneMustLeave,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Present In Inclusion Zone Must Leave",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_INSIDE_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.EnteredInclusionZoneDuringCurfew,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Entered Inclusion Zone During Curfew",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_INSIDE_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.ExitedInclusionZoneAfterViolation,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Exited Inclusion Zone After Violation",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_INSIDE_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.AppointmentEndedInsideViolationCleared,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Appointment inside Ended Violation Cleared",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_INSIDE_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.OutsideInclusionZoneMustEnter,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Outside Inclusion Zone Must Enter",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_OUTSIDE_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.ExitedInclusionZoneDuringCurfew,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Exited Inclusion Zone During Curfew",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_OUTSIDE_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.EnteredInclusionZoneAfterViolation,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Entered Inclusion Zone After Violation",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_OUTSIDE_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.AppointmentEndedOutsideViolationCleared,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Appointment outside Ended Violation Cleared",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_OUTSIDE_INCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.PresentInExclusionZoneMustLeave,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Present In Exclusion Zone Must Leave",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_INSIDE_EXCLUSION,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.EnteredExclusionZoneDuringCurfew,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Enterede Exclusion Zone During Curfew",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_INSIDE_EXCLUSION,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.ExitedExclusionZoneAfterViolation,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Exited Exclusion Zone After Violation",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_INSIDE_EXCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.ScheduleViolationClosed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Schedule Violation Closed",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_INSIDE_EXCLUSION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.OnACCharger,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Started charging on AC",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_CHARGING_AC,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.OffACCharger,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Stopped charging on AC",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_CHARGING_AC,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.OnUSBCharger,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Started charging on USB",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_CHARGING_USB,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.OffUSBCharger,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Stopped charging on USB",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_CHARGING_USB,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventTagTamperStrapOpen,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Tag Tamper Strap Open",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_TAG_STRAP_TAMPER,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventTagTamperStrapClose,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Tag Tamper Strap Close",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_TAG_STRAP_TAMPER,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventProximityOpen,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Proximity Tamper Open",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_TAG_PROXIMITY,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventProximityClose,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Proximity Tamper Close",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_TAG_PROXIMITY,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventTagTamperCaseOpen,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Case Tamper Open",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_TAG_CASE_TAMPER,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventTagTamperCaseClose,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Case Tamper Close",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_TAG_CASE_TAMPER,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.SimCardRemoved,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Removed Sim Card",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_SIM_CARD,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.SimCardInserted,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Inserted Sim Card",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_SIM_CARD,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventBeaconTamperCaseOpen,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Opened beacon case",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_BEACON_TAMPER_CASE,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventBeaconTamperCaseClose,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Closed beacon case",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_BEACON_TAMPER_CASE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventBeaconTamperProximityOpen,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Opened beacon Proxmity",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_BEACON_TAMPER_PROXIMITY,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventBeaconTamperProximityClose,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Closed beacon Proxmity",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_BEACON_TAMPER_PROXIMITY,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventBeaconMotionTamperOpen,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Opened beacon motion tamper",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_BEACON_MOTION_TAMPER,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventBeaconMotionTamperClose,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Closed beacon motion tamper",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_BEACON_MOTION_TAMPER,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventEnteredPureBeaconZone,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Entered beacon Zone",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_BEACON_TAMPER_ZONE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventLeftPureBeaconZone,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Left beacon Zone",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_BEACON_TAMPER_ZONE,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventTagTamperBatteryLow,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Battery low",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_TAG_BATTERY_TAMPER,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventTagTamperBatteryNormal,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Battery normal",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_TAG_BATTERY_TAMPER,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.PowerOnAfterSuddenShutDown,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Power On After Sudden ShudDown",
                TableEventConfig.ViolationCategoryTypes.VIOLATION_SUDDEN_SHUT_DOWN,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventMonitoringStarted,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Event Monitoring Started",
                TableEventConfig.ViolationCategoryTypes.MONITORING_STARTED,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.biometricTestPassed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Biometric Test Passed",
                TableEventConfig.ViolationCategoryTypes.BIOMETRIC_SUCCEEDED,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.biometricTestFailed,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Biometric Test Failed",
                TableEventConfig.ViolationCategoryTypes.BIOMETRIC_FAILED,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.biometricTestTimeOut,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Biometric Test Timeout",
                TableEventConfig.ViolationCategoryTypes.BIOMETRIC_FAILED,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.SyncSuccessful,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Sync Successful",
                TableEventConfig.ViolationCategoryTypes.SYNC,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.SyncFailed,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Sync Failed",
                TableEventConfig.ViolationCategoryTypes.SYNC,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventDeviceBatteryLow,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Battery low",
                TableEventConfig.ViolationCategoryTypes.BATTERY,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventDeviceBatteryCritical,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Battery critical",
                TableEventConfig.ViolationCategoryTypes.BATTERY,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventDeviceBatteryHigh,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Battery high",
                TableEventConfig.ViolationCategoryTypes.BATTERY,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventDeviceBatteryMedium,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Battery medium",
                TableEventConfig.ViolationCategoryTypes.BATTERY,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.offenderLocationUnavailable,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Offender Location Unavailable",
                TableEventConfig.ViolationCategoryTypes.GPS_SIGNAL,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.offenderLocationRestored,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Offender Location Restored",
                TableEventConfig.ViolationCategoryTypes.GPS_SIGNAL,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceSoftwareUpgradeSuccessful,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Software Upgrade Successful",
                TableEventConfig.ViolationCategoryTypes.UPGRADE_VERSION_SUCCEEDED,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.softwareUpgradeFailed,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Software Upgrade Failed",
                TableEventConfig.ViolationCategoryTypes.UPGRADE_VERSION_FAILED,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.offenderDeclinedUpgrade,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Software Offender Declined Upgrade",
                TableEventConfig.ViolationCategoryTypes.UPGRADE_VERSION_FAILED,
                TableEventConfig.ViolationSeverityTypes.ALARM,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.softwareUpgradeTimeOut,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Software upgrade Timeout ",
                TableEventConfig.ViolationCategoryTypes.UPGRADE_VERSION_FAILED,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.lockAfterPincodeAttemptsStarted,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Lock screen attempts started",
                TableEventConfig.ViolationCategoryTypes.PINCODE_ATTEMPTS,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.lockAfterPincodeAttemptsEnded,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Lock screen attempts ended",
                TableEventConfig.ViolationCategoryTypes.PINCODE_ATTEMPTS,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventSecureBootIssue,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Secure Boot Issue",
                TableEventConfig.ViolationCategoryTypes.PINCODE_ATTEMPTS,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.eventGuestTagEntered,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Guest tag detected",
                TableEventConfig.ViolationCategoryTypes.GUEST_TAGS,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.eventGuestTagLeft,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Guest tag left",
                TableEventConfig.ViolationCategoryTypes.GUEST_TAGS,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.photoCanceledByTheOffender,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Photo canceled by offender",
                TableEventConfig.ViolationCategoryTypes.PHOTO_TEST_CANCELED_BY_OFFENDER,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.photoTest,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Photo test",
                TableEventConfig.ViolationCategoryTypes.PHOTO_TEST,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceShieldingOpen,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Device shielding open",
                TableEventConfig.ViolationCategoryTypes.DEVICE_SHIELDING,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceShieldingClosed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Device shielding closed",
                TableEventConfig.ViolationCategoryTypes.DEVICE_SHIELDING,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add( new EntityEventConfig(
                TableEventConfig.EventTypes.tagNoMotion,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Tag no motion",
                TableEventConfig.ViolationCategoryTypes.TAG_NO_MOTION,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.tagMotion,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Tag motion",
                TableEventConfig.ViolationCategoryTypes.TAG_NO_MOTION,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceJammingTamper,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Jamming tamper",
                TableEventConfig.ViolationCategoryTypes.DEVICE_JAMMING,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceJammingClosed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Jamming closed",
                TableEventConfig.ViolationCategoryTypes.DEVICE_JAMMING,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceDiagnosticReport,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Self diagnostics event tamper",
                TableEventConfig.ViolationCategoryTypes.DEVICE_DIAGNOSTIC_REPORT,
                TableEventConfig.ViolationSeverityTypes.NORMAL,
                TableEventConfig.ActionType.NO_ACTION
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceCaseTamperOpen,
                TableEventConfig.EventCategory.OPEN_EVENT,
                "Device Case Tamper Open",
                TableEventConfig.ViolationCategoryTypes.DEVICE_CASE_TAMPER,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        defaultEventsList.add(new EntityEventConfig(
                TableEventConfig.EventTypes.deviceCaseTamperClosed,
                TableEventConfig.EventCategory.CLOSE_EVENT,
                "Device Case Tamper Closed",
                TableEventConfig.ViolationCategoryTypes.DEVICE_CASE_TAMPER,
                TableEventConfig.ViolationSeverityTypes.VIOLATION,
                TableEventConfig.ActionType.EARLY_NETWORK_CYCLE
        ));
        return defaultEventsList;
    }
}
