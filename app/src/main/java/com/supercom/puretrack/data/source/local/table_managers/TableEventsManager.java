package com.supercom.puretrack.data.source.local.table_managers;

import static com.supercom.puretrack.ui.activity.MainActivity.isOffenderInSuspendSchedule;

import android.util.Log;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.local_managers.business_logic.DeviceStateManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableDeviceDetails;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ActionType;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.EventTypes;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.source.local.table.TableEventLog;
import com.supercom.puretrack.data.source.local.table.TableOpenEventsLog.IsHandledEvent;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.network.communication_profile.ProfilingEventsConfig;
import com.supercom.puretrack.model.database.entities.EntityEventConfig;
import com.supercom.puretrack.model.database.entities.EntityEventLog;
import com.supercom.puretrack.model.database.entities.EntityOpenEventLog;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.model.database.entities.EntityZones;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.date.TimeUtil;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.general.NumberComputationUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TableEventsManager {

    private boolean isValidEventToAddToLog = true;

    private static final int NO_APPOINTMENT = -1;

    public static final String TAG = "DBEventsManager";
    public ProfilingEventsConfig profilingEventsConfig = new ProfilingEventsConfig();

    private DBEventsManagerListener eventsManagerListener;

    private static final TableEventsManager INSTANCE = new TableEventsManager();

    private TableEventsManager() {
    }

    public static TableEventsManager sharedInstance() {
        return INSTANCE;
    }

    public interface DBEventsManagerListener {
        void onEventCreated(EntityEventConfig recordEventConfig, int zoneId);
    }

    public void setEventsManagerListener(DBEventsManagerListener eventsManagerListener) {
        this.eventsManagerListener = eventsManagerListener;
    }

    private void addOpenEventToLog(EntityEventConfig recEventConfig_toInsert,
                                   int eventTypeToInsert,
                                   long time,
                                   int scheduleId,
                                   int zoneId,
                                   String strBat,
                                   int devStatus,
                                   long requestId,
                                   int offenderId,
                                   String additionalInfo) {

        int timeZoneId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_TIME_ZONE);

        // Adding Open Event to the TableEventLog
        EntityEventLog recordEventLog = new EntityEventLog(-1, zoneId, eventTypeToInsert, time, String.valueOf(timeZoneId),
                offenderId, devStatus, TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_VIO_STAT), strBat, -1, requestId,
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE),// int TagCaseTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP),  // int TagStrapTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_BATTERY),// int TagBatteryTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_LAST_BEACON_RECEIVE), // long BeaconLastCommunication
                0,    // int BeaconBatteryTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE), // int BeaconCaseTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX),  // int BeaconProxTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IS_IN_RANGE),
                0, additionalInfo);

        long effectedRowId = DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_EVENT_LOG, recordEventLog);
        recordEventLog = DatabaseAccess.getInstance().tableEventLog.getEventLogRecByRowId(effectedRowId);

        if (recEventConfig_toInsert != null) {
            // Adding openEvent to TableOpenEvents
            EntityOpenEventLog recOpenEventLog = new EntityOpenEventLog(
                    recordEventLog.PKEventId, eventTypeToInsert, zoneId, scheduleId, recEventConfig_toInsert.ViolationCategory
                    , recEventConfig_toInsert.ViolationSeverity, IsHandledEvent.NOT_HANDLED);
            DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_OPEN_EVENT_LOG, recOpenEventLog);
        }

    }

    private long addOpenEventToLogRetID(EntityEventConfig recEventConfig_toInsert,
                                        int eventTypeToInsert,
                                        long time,
                                        int scheduleId,
                                        int zoneId,
                                        String strBat,
                                        int devStatus,
                                        long requestId,
                                        int offenderId,
                                        String additionalInfo) {

        int timeZoneId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_TIME_ZONE);

        // Adding Open Event to the TableEventLog
        EntityEventLog recordEventLog = new EntityEventLog(-1, zoneId, eventTypeToInsert, time, String.valueOf(timeZoneId),
                offenderId, devStatus, TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_VIO_STAT), strBat, -1, requestId,
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE),// int TagCaseTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP),  // int TagStrapTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_BATTERY),// int TagBatteryTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_LAST_BEACON_RECEIVE), // long BeaconLastCommunication
                0,    // int BeaconBatteryTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE), // int BeaconCaseTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX),  // int BeaconProxTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IS_IN_RANGE),
                0, additionalInfo);

        long effectedRowId = DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_EVENT_LOG, recordEventLog);
        recordEventLog = DatabaseAccess.getInstance().tableEventLog.getEventLogRecByRowId(effectedRowId);

        if (recEventConfig_toInsert != null) {
            // Adding openEvent to TableOpenEvents
            EntityOpenEventLog recOpenEventLog = new EntityOpenEventLog(
                    recordEventLog.PKEventId, eventTypeToInsert, zoneId, scheduleId, recEventConfig_toInsert.ViolationCategory
                    , recEventConfig_toInsert.ViolationSeverity, IsHandledEvent.NOT_HANDLED);
            DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_OPEN_EVENT_LOG, recOpenEventLog);
        }
        return effectedRowId;

    }

    private void addClosingEventTolog(EntityEventConfig recEventConfig_toInsert,
                                      int eventTypeToInsert,
                                      long time,
                                      int scheduleId,
                                      int zoneId,
                                      String strBat,
                                      int devStatus,
                                      long requestId,
                                      int offenderId,
                                      String additionalInfo) {

        // Find the openetEventId in the TableOpenEventsLog by ViolationCategory
        long openerEventId;

        if (zoneId != -1) {
            openerEventId = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdForZoneViolationCategory(
                    recEventConfig_toInsert.ViolationCategory, zoneId);
        } else {
            openerEventId = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(recEventConfig_toInsert.ViolationCategory);
        }

        // Delete all Open Events from TableOpenEvents where VioCategory == closerEvent.VioCategory
        DatabaseAccess.getInstance().tableOpenEventsLog.deleteAll_WithViolationCategory(recEventConfig_toInsert.ViolationCategory,
                zoneId);

        int timeZoneId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_TIME_ZONE);

        updateOffenderStatus();
        // Adding Close Event to the TableEventLog with the openerEventId
        EntityEventLog recordEventLog = new EntityEventLog(
                -1, zoneId, eventTypeToInsert, time, String.valueOf(timeZoneId),
                offenderId, devStatus, TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_VIO_STAT), strBat, openerEventId, requestId,
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_CASE),// int TagCaseTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_STRAP),  // int TagStrapTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_TAG_STAT_BATTERY),// int TagBatteryTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_LAST_BEACON_RECEIVE), // long BeaconLastCommunication
                0,    // int BeaconBatteryTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_CASE), // int BeaconCaseTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_BEACON_STAT_PROX),  // int BeaconProxTamperStat
                TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IS_IN_RANGE),
                0, additionalInfo);

        DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_EVENT_LOG, recordEventLog);
    }

    public void addEventToLog(int eventTypeToInsert) {
        addEventToLog(eventTypeToInsert, -1, -1, System.currentTimeMillis(), "");
    }

    public void addEventToLog(int eventTypeToInsert, int scheduleId, int zoneId) {
        addEventToLog(eventTypeToInsert, scheduleId, zoneId, System.currentTimeMillis(), "");
    }

    public int addEventToLogRetID(int eventTypeToInsert, int scheduleId, int zoneId, String additionalInfo) {
        return addEventToLogRetID(eventTypeToInsert, scheduleId, zoneId, System.currentTimeMillis(), additionalInfo);
    }

    public void addEventToLog(int eventTypeToInsert, int scheduleId, int zoneId, long time) {
        addEventToLog(eventTypeToInsert, scheduleId, zoneId, time, "");
    }

    public void addEventToLog(int eventTypeToInsert, int scheduleId, int zoneId, String additionalInfo) {
        addEventToLog(eventTypeToInsert, scheduleId, zoneId, System.currentTimeMillis(), additionalInfo);
    }


    /**
     * add event to log, and server to server if needed
     */
    public void addEventToLog(int eventTypeToInsert, int scheduleId, int zoneId, long time, String additionalInfo) {
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        boolean isEventThatAllowedToInsert = isEventAllowedToInsertDBAlthoughtNotAllocated(eventTypeToInsert);
        if (!isOffenderAllocated && !isEventThatAllowedToInsert) return;

        if (isOffenderInSuspendSchedule) {
            String columnName = OFFENDER_DETAILS_CONS.OFFENDER_EVENTS_ALLOWED_WHILE_IN_SUSPEND_SCHEDULE;
            String allowedEventsWhileInSuspend = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(columnName);
            String[] splittedValues = allowedEventsWhileInSuspend.split(",");
            for (int i = 0; i < splittedValues.length; i++) {
                if (splittedValues[i].equals(String.valueOf(eventTypeToInsert))) {
                    isValidEventToAddToLog = true;
                    break;
                } else {
                    isValidEventToAddToLog = false;
                }
            }
        }

        if (!isValidEventToAddToLog) {
            isValidEventToAddToLog = true;
            return;
        }

        String strBat;
        int devStatus = 2;
        long requestId = 5;
        int offenderId = 1;
        Log.w(TAG, "*** addEventToLog *** " + "\n [" + TimeUtil.getCurrentTimeStr() + "]  EventType = " + eventTypeToInsert + "\n  ZoneId = " + zoneId);
        LoggingUtil.fileLogZonesUpdate("\nz: " + zoneId + " -> ev: " + eventTypeToInsert);


        clearAllOpenEvents_WithAppointmentEnded();

        strBat = String.valueOf(DeviceStateManager.getInstance().getNewBatteryLevel());

        EntityEventConfig recordEventConfig = DatabaseAccess.getInstance().tableEventConfig.getRecordByEventType(eventTypeToInsert);

        if (recordEventConfig == null) return;
        if (isOpenEvent(recordEventConfig)) {
            addOpenEventToLog(recordEventConfig, eventTypeToInsert, time, scheduleId, zoneId, strBat, devStatus, requestId, offenderId,
                    additionalInfo);
        } else {
            addClosingEventTolog(recordEventConfig, eventTypeToInsert, time, scheduleId, zoneId, strBat, devStatus, requestId, offenderId,
                    additionalInfo);
        }

        updateOffenderStatus();

        if (eventsManagerListener != null) {
            eventsManagerListener.onEventCreated(recordEventConfig, zoneId);
        }

        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_CREATED_EVENT_TYPE, eventTypeToInsert);

        sendEventsToServerIfNeeded(recordEventConfig);

    }

    public int addEventToLogRetID(int eventTypeToInsert, int scheduleId, int zoneId, long time, String additionalInfo) {
        int retVal = -1;
        boolean isOffenderAllocated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS)
                == OffenderActivation.OFFENDER_STATUS_ALLOCATED;
        boolean isEventThatAllowedToInsert = isEventAllowedToInsertDBAlthoughtNotAllocated(eventTypeToInsert);

        if (isOffenderAllocated || isEventThatAllowedToInsert) {

            String strBat;
            int devStatus = 2;
            long requestId = 5;
            int offenderId = 1;

            Log.w(TAG, "*** addEventToLog *** " + "\n [" + TimeUtil.getCurrentTimeStr() + "]  EventType = " + eventTypeToInsert + "\n  ZoneId = " + zoneId);
            LoggingUtil.fileLogZonesUpdate("\nz: " + zoneId + " -> ev: " + eventTypeToInsert);

            clearAllOpenEvents_WithAppointmentEnded();

            strBat = String.valueOf(DeviceStateManager.getInstance().getNewBatteryLevel());

            EntityEventConfig recEventConfig_toInsert = DatabaseAccess.getInstance().tableEventConfig.getRecordByEventType(eventTypeToInsert);

            if (recEventConfig_toInsert != null) {
                if (isOpenEvent(recEventConfig_toInsert)) {
                    retVal = (int) addOpenEventToLogRetID(recEventConfig_toInsert, eventTypeToInsert, time, scheduleId, zoneId, strBat, devStatus, requestId, offenderId,
                            additionalInfo);
                } else {
                    addClosingEventTolog(recEventConfig_toInsert, eventTypeToInsert, time, scheduleId, zoneId, strBat, devStatus, requestId, offenderId,
                            additionalInfo);
                }

                updateOffenderStatus();

                if (eventsManagerListener != null) {
                    eventsManagerListener.onEventCreated(recEventConfig_toInsert, zoneId);
                }

                TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_LAST_CREATED_EVENT_TYPE, eventTypeToInsert);

                sendEventsToServerIfNeeded(recEventConfig_toInsert);
            }
        }
        return retVal;
    }

    private boolean isEventAllowedToInsertDBAlthoughtNotAllocated(int eventTypeToInsert) {
        return (eventTypeToInsert == EventTypes.eventMonitoringStarted ||
                eventTypeToInsert == EventTypes.pendingEnrolment ||
                eventTypeToInsert == EventTypes.offenderEnrolmentPerformed || eventTypeToInsert == EventTypes.offenderFingerprintScanned ||
                eventTypeToInsert == EventTypes.tag_beaconVerified || eventTypeToInsert == EventTypes.knoxActivatedOnDevice ||
                eventTypeToInsert == EventTypes.deviceLocationVerified || eventTypeToInsert == EventTypes.SyncSuccessful ||
                eventTypeToInsert == EventTypes.SyncFailed);
    }

    /**
     * @param recordEventConfig events now, and not wait for next cycle if action is EARLY_NETWORK_CYCLE
     */
    private void sendEventsToServerIfNeeded(EntityEventConfig recordEventConfig) {
        if (recordEventConfig == null || recordEventConfig.actionType != ActionType.EARLY_NETWORK_CYCLE) return;
        String messageToUpload = " EARLY_NETWORK_CYCLE " + " EventType " + recordEventConfig.EventType;
        Log.w(TAG, messageToUpload);
        LoggingUtil.updateNetworkLog("\n\n*** [" + TimeUtil.getCurrentTimeStr() + "]" + messageToUpload, false);
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Events.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        NetworkRepository.getInstance().startNewCycle();

    }

    public void addDeviceSatrtupEventToLogIfNeed() {
        boolean hasOpenPowerOffEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenEventByEventType(TableEventConfig.EventTypes.eventPowerOff) != -1;
        if (hasOpenPowerOffEvent) {
            addEventToLog(TableEventConfig.EventTypes.eventPowerOn, -1, -1, System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(1));
        }

        boolean hasOpenInitiazliedRestartEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenEventByEventType(EventTypes.eventConnectionUnavailableDeviceRestart) != -1;
        if (hasOpenInitiazliedRestartEvent) {
            addEventToLog(TableEventConfig.EventTypes.eventDeviceStartupAfterRestart, -1, -1, System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(1));
        }

        DatabaseAccess.getInstance().UpdateField(EnumDatabaseTables.TABLE_DEVICE_DETAILS, TableDeviceDetails.COLUMN_DEBUG_INFO_RECEIVED_DATA_USAGE, 0);
        DatabaseAccess.getInstance().UpdateField(EnumDatabaseTables.TABLE_DEVICE_DETAILS, TableDeviceDetails.COLUMN_DEBUG_INFO_SENT_DATA_USAGE, 0);

    }

    public void addPowerOnAfterSuddenlyShutDownEventToLogIfNeed() {
        boolean hasOpenEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenEventByEventType(TableEventConfig.EventTypes.eventPowerOff) != -1;
        if (!hasOpenEvent) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.PowerOnAfterSuddenShutDown, -1, -1, System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(1), NumberComputationUtil.createRandomPassword().toString());
        }else{
            Log.i("bug651","hasOpenEvent");
        }
    }
    public void addPowerOnAfterSuddenlyShutDownEventToLog(int batteryPercent) {
         TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.PowerOnAfterSuddenShutDown, -1, -1, System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(1), "{\"batteryPercent:"+batteryPercent+"\"}");
    }
    private boolean isOpenEvent(EntityEventConfig recEventConfig) {
        //If the recEventConfig is null treat the event as OPEN
        if (recEventConfig == null) {
            return true;
        } else {
            return recEventConfig.IsOpenEvent == 1;
        }
    }

    public void updateOffenderStatus() {
        int maxSeverityTypeThatOpen = DatabaseAccess.getInstance().tableOpenEventsLog.getOffenderStatus();
        TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_VIO_STAT, maxSeverityTypeThatOpen);
    }

    private void clearAllOpenEvents_WithAppointmentEnded() {
//		For each OpenEvent in TableOpenEvents do
//		{
//			RecordScheduleOfZones scheduleOfZone = DatabaseAccess.getInstance().TblScheduleOfZones.getScheduleOfZoneById(OpenEvent.scheduleId);
//			if(appointmentEnded(scheduleOfZone))
//			{
//				Insert closing event eventAppointmentEndedViolationCleared to TableEventLog, with EventRelatedId=OpenEvId;
//				Delete the OpenEvent from TableOpenEvents; 
//			}
//		}
    }

    public void addEventFor_EnteredInclZone(int zoneId) {
        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);
        int appointmentTypeId = currentScheduleOfZone == null ? NO_APPOINTMENT : currentScheduleOfZone.AppointmentTypeId;
        int appointmentId = currentScheduleOfZone == null ? -1 : currentScheduleOfZone.AppointmentId;

        // we check hasOpenEvent_inViolationOutsideInclusionCategory(zoneId) - in case we add zone exactly where offender located now
        if (hasOpenEvent_inViolationOutsideInclusionCategory(zoneId) &&
                (appointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI || (hasDefaultZoneWithMBI(zoneId) && appointmentTypeId == NO_APPOINTMENT))) {
            sharedInstance().addEventToLog(TableEventConfig.EventTypes.EnteredInclusionZoneAfterViolation, appointmentId, zoneId);
        } else if (appointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO && !IsInGraceTime(currentScheduleOfZone)) {
            sharedInstance().addEventToLog(TableEventConfig.EventTypes.EnteredInclusionZoneDuringCurfew, appointmentId, zoneId);
        } else {
            sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventEnteredInclusionZone, appointmentId, zoneId);
        }
    }

    public void addEventEntryOfExclusionZone(int zoneId) {
        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);
        if (currentScheduleOfZone != null) {
            if (currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventEnteredExclusionZone, currentScheduleOfZone.AppointmentId, zoneId);
            }
        } else {

            EntityScheduleOfZones lastAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetLastAppointmentWhichNotBiometric(zoneId);
            boolean isNotInViolationSinceLastAppointment =
                    (lastAppointment != null && lastAppointment.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI && IsInGraceTime(lastAppointment));

            EntityScheduleOfZones nextAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetNextAppointmentWhichNotBiometric(zoneId);
            boolean isNotInViolationSinceNextAppointment =
                    (nextAppointment != null && nextAppointment.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI && IsInGraceTime(nextAppointment));

            if (!isNotInViolationSinceLastAppointment && !isNotInViolationSinceNextAppointment) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.EnteredExclusionZoneDuringCurfew, -1, zoneId);
            } else {
                int appointmentId = -1;
                if (lastAppointment != null) {
                    appointmentId = lastAppointment.AppointmentId;
                } else {
                    appointmentId = nextAppointment.AppointmentId;
                }
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventEnteredExclusionZone, appointmentId, zoneId);
            }
        }

    }

    public void addEventFor_ExitInclZone(int zoneId, String additionalInfo) {

        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);
        int appointmentTypeId;
        int appointmentId;

        if (currentScheduleOfZone != null) {

            appointmentTypeId = currentScheduleOfZone.AppointmentTypeId;
            appointmentId = currentScheduleOfZone.AppointmentId;

            if (appointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI && !IsInGraceTime(currentScheduleOfZone)) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.ExitedInclusionZoneDuringCurfew, appointmentId, zoneId, additionalInfo);
            } else if (hasOpenEvent_inViolationInsideInclusionCategory(zoneId) &&
                    (appointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO || appointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGO)) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.ExitedInclusionZoneAfterViolation, appointmentId, zoneId, additionalInfo);
            } else {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventExitedInclusionZone, appointmentId, zoneId, additionalInfo);
            }
        } else {

            appointmentTypeId = NO_APPOINTMENT;
            appointmentId = -1;

            EntityScheduleOfZones lastAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetLastAppointmentWhichNotBiometric(zoneId);
            boolean isNotInViolationSinceLastAppointment =
                    (lastAppointment != null && lastAppointment.AppointmentTypeId != TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI && IsInGraceTime(lastAppointment));

            EntityScheduleOfZones NextAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetNextAppointmentWhichNotBiometric(zoneId);
            boolean isNotInViolationSinceNextAppointment =
                    (NextAppointment != null && NextAppointment.AppointmentTypeId != TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI && IsInGraceTime(NextAppointment));

            if (hasDefaultZoneWithMBI(zoneId) && !isNotInViolationSinceLastAppointment && !isNotInViolationSinceNextAppointment) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.ExitedInclusionZoneDuringCurfew, appointmentId, zoneId, additionalInfo);
            } else {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventExitedInclusionZone, appointmentId, zoneId, additionalInfo);
            }
        }
    }

    public void addEventEntryOfBufferZone(int zoneId) {
        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);
        if (currentScheduleOfZone != null) {
            if (currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.enteredBufferOfExclusionZone, currentScheduleOfZone.AppointmentId, zoneId);
            }
        } else {

            EntityScheduleOfZones lastAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetLastAppointmentWhichNotBiometric(zoneId);
            boolean isNotInViolationSinceLastAppointment =
                    (lastAppointment != null && lastAppointment.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI && IsInGraceTime(lastAppointment));

            EntityScheduleOfZones nextAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetNextAppointmentWhichNotBiometric(zoneId);
            boolean isNotInViolationSinceNextAppointment =
                    (nextAppointment != null && nextAppointment.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI && IsInGraceTime(nextAppointment));

            if (!isNotInViolationSinceLastAppointment && !isNotInViolationSinceNextAppointment) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.enteredBufferOfExclusionZone, -1, zoneId);
            } else {
                int appointmentId = -1;
                if (lastAppointment != null) {
                    appointmentId = lastAppointment.AppointmentId;
                } else {
                    appointmentId = nextAppointment.AppointmentId;
                }
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.enteredBufferOfExclusionZone, appointmentId, zoneId);
            }
        }

    }

    public void addEventExitOfBufferZone(int zoneId) {
        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);
        if (currentScheduleOfZone != null) {
            if (currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.exitedBufferOfExclusionZone, currentScheduleOfZone.AppointmentId, zoneId);
            } else if (currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGO ||
                    currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.exitedBufferOfExclusionZone, currentScheduleOfZone.AppointmentId, zoneId);
            }
        } else // there is no schedule for current time
        {
            if (DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEventInViolationCategory(ViolationCategoryTypes.VIOLATION_INSIDE_EXCLUSION)) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.exitedBufferOfExclusionZone, -1, zoneId);
            } else {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.exitedBufferOfExclusionZone, -1, zoneId);
            }
        }
    }

    public void addEventFor_ExitExclZone(int zoneId) {
        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);
        if (currentScheduleOfZone != null) {
            if (currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.ExitedExclusionZoneAfterViolation, currentScheduleOfZone.AppointmentId, zoneId);
            } else if (currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGO ||
                    currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventExitedExclusionZone, currentScheduleOfZone.AppointmentId, zoneId);
            }
        } else // there is no schedule for current time
        {
            if (DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEventInViolationCategory(ViolationCategoryTypes.VIOLATION_INSIDE_EXCLUSION)) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.ExitedExclusionZoneAfterViolation, -1, zoneId);
            } else {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventExitedExclusionZone, -1, zoneId);
            }
        }
    }

    public void addEventFor_StillInsideInclZone(int zoneId) {
        boolean hasOpenEventForInsideInclusionZone = hasOpenEvent_inViolationInsideInclusionCategory(zoneId);

        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);

        int appointmentTypeId = currentScheduleOfZone == null ? NO_APPOINTMENT : currentScheduleOfZone.AppointmentTypeId;
        int appointmentId = currentScheduleOfZone == null ? -1 : currentScheduleOfZone.AppointmentId;

        if (appointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO && !hasOpenEventForInsideInclusionZone && !IsInGraceTime(currentScheduleOfZone)) {
            long startTime = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId).StartTime;
            sharedInstance().addEventToLog(TableEventConfig.EventTypes.PresentInInclusionZoneMustLeave, appointmentId, zoneId, startTime);
        } else if (appointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGO && hasOpenEventForInsideInclusionZone) {
            EntityScheduleOfZones lastAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetLastAppointmentWhichNotBiometric(zoneId);
            if (lastAppointment != null) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.AppointmentEndedInsideViolationCleared, appointmentId, zoneId,
                        lastAppointment.EndTime);
            }
        } else if ((appointmentTypeId != TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO || wasZoneDeletedFromServer(zoneId)) && hasOpenEventForInsideInclusionZone) {
            EntityScheduleOfZones lastAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetLastAppointmentWhichNotBiometric(zoneId);
            if (lastAppointment != null) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.AppointmentEndedInsideViolationCleared, appointmentId, zoneId,
                        lastAppointment.EndTime);
            }
        }
    }

    private boolean hasOpenEvent_inViolationOutsideInclusionCategory(int zoneId) {
        return DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEvent_ForZone(TableEventConfig.EventTypes.OutsideInclusionZoneMustEnter, zoneId)
                || DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEvent_ForZone(TableEventConfig.EventTypes.ExitedInclusionZoneDuringCurfew, zoneId);

    }

    private boolean hasOpenEvent_inViolationInsideInclusionCategory(int zoneId) {
        return DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEvent_ForZone(EventTypes.PresentInInclusionZoneMustLeave, zoneId)
                || DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEvent_ForZone(EventTypes.EnteredInclusionZoneDuringCurfew, zoneId);
    }

    public void addEventFor_StillInsideExclZone(int zoneId) {
        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);
        if (currentScheduleOfZone != null) {
            if (currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO && !IsInGraceTime(currentScheduleOfZone)) {
                if (!hasOpenEvent_inViolationInsideExclusionCategory(zoneId)) {
                    long startTime = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId).StartTime;
                    sharedInstance().addEventToLog(TableEventConfig.EventTypes.PresentInExclusionZoneMustLeave, currentScheduleOfZone.AppointmentId,
                            zoneId, startTime);
                }
            } else if (currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGO ||
                    currentScheduleOfZone.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI) {
                if (hasOpenEvent_inViolationInsideExclusionCategory(zoneId)) {
                    sharedInstance().addEventToLog(TableEventConfig.EventTypes.ScheduleViolationClosed, currentScheduleOfZone.AppointmentId, zoneId);
                }
            }
        } else {  // there is no schedule for current time
            if (!hasOpenEvent_inViolationInsideExclusionCategory(zoneId)) {

                // We want to find last appointment in order to know how much time user located in exclusion zone
                EntityScheduleOfZones lastAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetLastAppointmentWhichNotBiometric(zoneId);

                if (lastAppointment != null && lastAppointment.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI && !IsInGraceTime(lastAppointment)) {
                    sharedInstance().addEventToLog(TableEventConfig.EventTypes.PresentInExclusionZoneMustLeave, -1, zoneId, lastAppointment.EndTime);
                }
            } else if (hasOpenEvent_inViolationInsideExclusionCategory(zoneId) && wasZoneDeletedFromServer(zoneId)) {
                sharedInstance().addEventToLog(TableEventConfig.EventTypes.ExitedExclusionZoneAfterViolation, -1, zoneId);
            }
        }
    }


    private boolean hasOpenEvent_inViolationInsideExclusionCategory(int zoneId) {
        return DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEvent_ForZone(EventTypes.PresentInExclusionZoneMustLeave, zoneId)
                || DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEvent_ForZone(EventTypes.EnteredExclusionZoneDuringCurfew, zoneId);
    }

    public void addEventFor_StillOutsideInclZone(int zoneId) {
        boolean hasOpenEventForOutsideInclusionZone = hasOpenEvent_inViolationOutsideInclusionCategory(zoneId);

        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);

        int appointmentTypeId = currentScheduleOfZone == null ? NO_APPOINTMENT : currentScheduleOfZone.AppointmentTypeId;

        if (!hasOpenEventForOutsideInclusionZone) {
            if ((hasDefaultZoneWithMBI(zoneId) && appointmentTypeId == NO_APPOINTMENT) ||
                    (appointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI && !IsInGraceTime(currentScheduleOfZone))) {

                if (currentScheduleOfZone == null) {

                    EntityScheduleOfZones nextAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetNextAppointmentWhichNotBiometric(zoneId);
                    boolean isNotInViolationSinceNextAppointment =
                            (nextAppointment != null && nextAppointment.AppointmentTypeId != TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI && IsInGraceTime(nextAppointment));

                    if (!isNotInViolationSinceNextAppointment) {

                        // We want to find last appointment in order to know if we should take end time of last appointment
                        EntityScheduleOfZones lastAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetLastAppointmentWhichNotBiometric(zoneId);

                        if (lastAppointment == null) {
                            sharedInstance().addEventToLog(TableEventConfig.EventTypes.OutsideInclusionZoneMustEnter, -1, zoneId);
                        } else {
                            if (!IsInGraceTime(lastAppointment)) {
                                long startTime = lastAppointment.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI ?
                                        System.currentTimeMillis() : lastAppointment.EndTime;
                                sharedInstance().addEventToLog(TableEventConfig.EventTypes.OutsideInclusionZoneMustEnter, -1, zoneId, startTime);
                            } else if (IsInGraceTime(lastAppointment) && lastAppointment.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI) {
                                sharedInstance().addEventToLog(TableEventConfig.EventTypes.OutsideInclusionZoneMustEnter, -1, zoneId);
                            }
                        }
                    }
                } else {
                    sharedInstance().addEventToLog(TableEventConfig.EventTypes.OutsideInclusionZoneMustEnter, currentScheduleOfZone.AppointmentId,
                            zoneId, currentScheduleOfZone.StartTime);
                }
            }
        } else // has open event
        {
            // changes where made to zone
            if (wasZoneDeletedFromServer(zoneId) || (appointmentTypeId != NO_APPOINTMENT && appointmentTypeId != TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI)
                    || (hasDefaultZoneWithMBI(zoneId) && appointmentTypeId != NO_APPOINTMENT) ||
                    (!hasDefaultZoneWithMBI(zoneId) && appointmentTypeId == NO_APPOINTMENT)) {

                int appointmentId = currentScheduleOfZone == null ? -1 : currentScheduleOfZone.AppointmentId;

                long timeToSendEvent = System.currentTimeMillis();

                if (currentScheduleOfZone != null && currentScheduleOfZone.AppointmentTypeId != TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI) {
                    timeToSendEvent = currentScheduleOfZone.StartTime;
                } else {

                    // We want to find last appointment in order to know if we should take end time of last appointment
                    EntityScheduleOfZones lastAppointment = DatabaseAccess.getInstance().tableScheduleOfZones.GetLastAppointmentWhichNotBiometric(zoneId);
                    if (lastAppointment != null && lastAppointment.AppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI) {
                        timeToSendEvent = lastAppointment.EndTime;
                    }
                }

                sharedInstance().addEventToLog(TableEventConfig.EventTypes.AppointmentEndedOutsideViolationCleared, appointmentId,
                        zoneId, timeToSendEvent);
            }
        }
    }

    /**
     * Will check if user located in grace time in order to know if should send an event
     */
    private boolean IsInGraceTime(EntityScheduleOfZones scheduleOfZone) {
        if (scheduleOfZone != null) {
            long grace = TimeUnit.SECONDS.toMillis(TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                    (OFFENDER_DETAILS_CONS.DETAILS_OFF_SCHEDULE_GRACE));
            return (Math.abs(TimeUtil.GetUtcTime() - scheduleOfZone.StartTime) <= grace ||
                    Math.abs(TimeUtil.GetUtcTime() - scheduleOfZone.EndTime) <= grace);
        }
        return false;
    }

    private boolean wasZoneDeletedFromServer(int zoneId) {
        return DatabaseAccess.getInstance().tableZonesDeleted.getZoneRecordByZoneId(zoneId) != null;
    }

    private boolean hasDefaultZoneWithMBI(int zoneId) {
        EntityZones zone = DatabaseAccess.getInstance().tableZones.getZoneRecordByZoneId(zoneId);
        return zone != null && zone.defaultAppointmentTypeId == TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI;
    }

    public void addEventFor_StillOutsideExclZone(int zoneId) {
        EntityScheduleOfZones currentScheduleOfZone = DatabaseAccess.getInstance().tableScheduleOfZones.getCurrentScheduleOfZone(zoneId);
        if (currentScheduleOfZone != null) {
            //Not relevant for ExclusionZone
        } else { // there is no schedule for current time

        }
    }

    public boolean hasEarlyNetworkCycleEvents() {

        List<EntityEventLog> allEventLogRecords = DatabaseAccess.getInstance().tableEventLog.getAllEventLogRecords();
        for (EntityEventLog recordEventLog : allEventLogRecords) {
            EntityEventConfig recordByEventType = DatabaseAccess.getInstance().tableEventConfig.getRecordByEventType(recordEventLog.EvType);
            if (recordByEventType != null && recordByEventType.actionType == ActionType.EARLY_NETWORK_CYCLE &&
                    recordEventLog.PureMonitorSyncRetryCount < TableEventLog.MAX_SYNC_RETRY_COUNT) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOpenEventInViolationCategory(int violationCategory) {
        return DatabaseAccess.getInstance().tableOpenEventsLog.hasOpenEventInViolationCategory(violationCategory);
    }

}
