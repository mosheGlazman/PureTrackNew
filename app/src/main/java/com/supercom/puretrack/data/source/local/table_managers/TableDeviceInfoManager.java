package com.supercom.puretrack.data.source.local.table_managers;

import com.supercom.puretrack.data.source.local.local_managers.hardware.CellularInfoManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.CellularInfoManager.CellInfoObj;
import com.supercom.puretrack.data.source.local.local_managers.hardware.DeviceInformationManager;
import com.supercom.puretrack.data.source.local.local_managers.hardware.DeviceInformationManager.DeviceInfoDetailsObj;
import com.supercom.puretrack.data.source.local.local_managers.hardware.DeviceInformationManager.DeviceInfoStatusObj;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoCellular;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoDetails;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoStatus;
import com.supercom.puretrack.model.database.entities.EntityOffenderStatus;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TableDeviceInfoManager {
    public static final int MAX_ATTEMPTS_BEFORE_DELETE_DEVICE_INFO_ROWS = 5;

    private int currentFailedAttemptSendingDeviceInfoCounter = 0;

    private static final TableDeviceInfoManager INSTANCE = new TableDeviceInfoManager();

    private TableDeviceInfoManager() {
    }

    public static TableDeviceInfoManager sharedInstance() {
        return INSTANCE;
    }

    public EntityDeviceInfoDetails getDeviceInfoDetailsRecord() {
        return DatabaseAccess.getInstance().tableDeviceInfoDetails.getDeviceInfoDetailsRecordForUpload();
    }

    public List<EntityDeviceInfoStatus> getDeviceInfoStatusRecords() {
        return DatabaseAccess.getInstance().tableDeviceInfoStatus.getDeviceInfoStatusRecordsForUpload();
    }

    public List<EntityDeviceInfoCellular> getDeviceInfoCellularRecords() {
        return DatabaseAccess.getInstance().tableDeviceInfoCellular.getDeviceInfoCellularRecordsForUpload();
    }

    public void addNewRecordDeviceInfoDetailsToDB(String sw_version, String hw_version_phone_model, String hw_components, String imei, String os_version, String db_version, String battery_type) {
        EntityDeviceInfoDetails recordDeviceInfoDetails = new EntityDeviceInfoDetails(sw_version, hw_version_phone_model, hw_components, imei, os_version, db_version, battery_type);
        DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_DEVICE_INFO_DETAILS, recordDeviceInfoDetails);
    }

    public void addNewRecordDeviceInfoStatusToDB(String operational_mode, String battery_level, String temperature, String tag_last_ping, String tag_battery, String is_Tag_battery_tamper,
                                                 String beacon_last_ping, String beacon_battery, String is_beacon_battery_tamper, String offender_in_range, String knox_activated,
                                                 String kiosk_mode_enabled, String events_upload_stat, String locations_upload_stat) {
        EntityDeviceInfoStatus recordDeviceInfoStatus = new EntityDeviceInfoStatus(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())), operational_mode, battery_level,
                temperature, tag_last_ping, tag_battery, is_Tag_battery_tamper, beacon_last_ping, beacon_battery, is_beacon_battery_tamper, offender_in_range, knox_activated,
                kiosk_mode_enabled, events_upload_stat, locations_upload_stat);

        DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_DEVICE_INFO_STATUS, recordDeviceInfoStatus);
    }

    public void addNewRecordDeviceInfoCellularToDB(String registration_type, String network_id, String cell_reception,
                                                   String cell_mobile_data, String sim_id, String device_phone_number) {
        EntityDeviceInfoCellular recordDeviceInfoCellular = new EntityDeviceInfoCellular(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())), registration_type,
                network_id, cell_reception, cell_mobile_data, sim_id, device_phone_number);
        DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_DEVICE_INFO_CELLULAR, recordDeviceInfoCellular);
    }

    public void deleteAllRecordsInTableDeviceInfoDetails() {
        DatabaseAccess.getInstance().deleteAllRecordsInTable(EnumDatabaseTables.TABLE_DEVICE_INFO_DETAILS);
    }

    private void deleteAllRecordsInTableDeviceInfoStatus() {
        DatabaseAccess.getInstance().deleteAllRecordsInTable(EnumDatabaseTables.TABLE_DEVICE_INFO_STATUS);
    }

    private void deleteAllRecordsInTableDeviceInfoCellular() {
        DatabaseAccess.getInstance().deleteAllRecordsInTable(EnumDatabaseTables.TABLE_DEVICE_INFO_CELLULAR);
    }

    public void clearAllTablesRelatedToDeviceInfoContract() {
        deleteAllRecordsInTableDeviceInfoDetails();
        deleteAllRecordsInTableDeviceInfoStatus();
        deleteAllRecordsInTableDeviceInfoCellular();
        clearAttemptsCounter();
    }

    public boolean isTableDeviceInfoDetailsEmpty() {
        return DatabaseAccess.getInstance().isTableEmpty(EnumDatabaseTables.TABLE_DEVICE_INFO_DETAILS);
    }

    public void saveDeviceDetailsToDB() {
        EntityDeviceInfoDetails recordDeviceInfoDetails = createDeviceInfoDetails();
        addNewRecordDeviceInfoDetailsToDB(recordDeviceInfoDetails.sw_version, recordDeviceInfoDetails.hw_version_phone_model, recordDeviceInfoDetails.hw_components,
                recordDeviceInfoDetails.imei, recordDeviceInfoDetails.os_version, recordDeviceInfoDetails.db_version, recordDeviceInfoDetails.battery_type);
    }

    public void saveDeviceStatusToDB() {
        EntityDeviceInfoStatus recordDeviceInfoStatus = createDeviceInfoStatus();

        addNewRecordDeviceInfoStatusToDB(recordDeviceInfoStatus.operational_mode, recordDeviceInfoStatus.battery_level, recordDeviceInfoStatus.temperature, recordDeviceInfoStatus.tag_last_ping,
                recordDeviceInfoStatus.tag_battery, recordDeviceInfoStatus.is_Tag_battery_tamper, recordDeviceInfoStatus.beacon_last_ping, recordDeviceInfoStatus.beacon_battery,
                recordDeviceInfoStatus.is_beacon_battery_tamper, recordDeviceInfoStatus.offender_in_range, recordDeviceInfoStatus.knox_activated, recordDeviceInfoStatus.kiosk_mode_enabled,
                recordDeviceInfoStatus.event_upload_status, recordDeviceInfoStatus.location_upload_status);
    }

    public void saveCellularInfoToDB() {
        EntityDeviceInfoCellular recordDeviceInfoCellular = createDeviceInfoCellular();
        addNewRecordDeviceInfoCellularToDB(recordDeviceInfoCellular.registration_type, recordDeviceInfoCellular.network_id, recordDeviceInfoCellular.cell_reception,
                recordDeviceInfoCellular.cell_mobile_data, recordDeviceInfoCellular.sim_id, recordDeviceInfoCellular.device_phone_number);
    }

    private EntityDeviceInfoDetails createDeviceInfoDetails() {
        DeviceInfoDetailsObj deviceInfoDetailsObj = new DeviceInformationManager().getDeviceInfoDetailsObj();
        return new EntityDeviceInfoDetails(deviceInfoDetailsObj.sw_version, deviceInfoDetailsObj.hw_version_phone_model, deviceInfoDetailsObj.hw_components, deviceInfoDetailsObj.imei,
                deviceInfoDetailsObj.os_version, deviceInfoDetailsObj.db_version, deviceInfoDetailsObj.batteryCapacity);
    }


    private int CalcLocationsUploadStat() {
        int locationsUploadStat;

        if (DatabaseAccess.getInstance().tableGpsPoint.getNewLocationsCount() == 0) {
            if (DatabaseAccess.getInstance().tableGpsPoint.getFailedLocationsCount() == 0) {
                // all locations had been uploaded
                locationsUploadStat = 0;
            } else {
                // some locations failed to upload
                locationsUploadStat = 2;
            }
        } else {
            // not all locations has uploaded
            locationsUploadStat = 1;
        }
        return locationsUploadStat;
    }

    private int CalcEventsUploadStat() {
        int eventsUploadStat;

        if (DatabaseAccess.getInstance().tableEventLog.getNewEventsCount() == 0) {
            if (DatabaseAccess.getInstance().tableEventLog.getFailedEventsCount() == 0) {
                // all events had been uploaded
                eventsUploadStat = 0;
            } else {
                // some events failed to upload
                eventsUploadStat = 2;
            }
        } else {
            // not all events has uploaded
            eventsUploadStat = 1;
        }
        return eventsUploadStat;
    }


    private EntityDeviceInfoStatus createDeviceInfoStatus() {
        EntityOffenderStatus recordOffenderStatus = TableOffenderStatusManager.sharedInstance().getRecordOffStatus();

        // calculate event upload status
        int eventsUploadStat = CalcEventsUploadStat();

        // calculate location upload status
        int locationsUploadStat = CalcLocationsUploadStat();

        DeviceInfoStatusObj deviceInfoStatusObj = new DeviceInformationManager().getDeviceInfoStatusObj();
        return new EntityDeviceInfoStatus(null, String.valueOf(recordOffenderStatus.offIsOffenderActivated), String.valueOf(recordOffenderStatus.OffDeviceBatteryPercentage),
                String.valueOf(recordOffenderStatus.OffDeviceTemperature), String.valueOf(recordOffenderStatus.OffTagLastReceive), String.valueOf(recordOffenderStatus.OffTagBatteryLevel),
                String.valueOf(recordOffenderStatus.OffTagStatBattery), String.valueOf(recordOffenderStatus.OffBeaconLastReceive), String.valueOf(recordOffenderStatus.OffBeaconBatteryLevel),
                String.valueOf(recordOffenderStatus.OffBeaconStatBattery), String.valueOf(recordOffenderStatus.OffIsInRange), deviceInfoStatusObj.knox_activated,
                deviceInfoStatusObj.kiosk_mode_enabled, String.valueOf(eventsUploadStat), String.valueOf(locationsUploadStat));
    }

    private EntityDeviceInfoCellular createDeviceInfoCellular() {
        CellInfoObj cellInfoObj = CellularInfoManager.sharedInstance().getCellularInfoObj();
        return new EntityDeviceInfoCellular(null, cellInfoObj.registration_type, cellInfoObj.network_id, cellInfoObj.cell_reception, cellInfoObj.cell_mobile_data, cellInfoObj.sim_id, cellInfoObj.device_phone_number);
    }

    public boolean isReachedMaximumAttempts() {
        return currentFailedAttemptSendingDeviceInfoCounter >= MAX_ATTEMPTS_BEFORE_DELETE_DEVICE_INFO_ROWS;
    }

    public void clearAttemptsCounter() {
        currentFailedAttemptSendingDeviceInfoCounter = 0;
    }

    public void increaseAttemptsCounter() {
        currentFailedAttemptSendingDeviceInfoCounter++;
    }
}
