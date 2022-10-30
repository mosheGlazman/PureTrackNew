package com.supercom.puretrack.data.source.local.table_managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.supercom.puretrack.data.source.local.table.DatabaseTable;
import com.supercom.puretrack.data.source.local.table.TableOffenderDetails;
import com.supercom.puretrack.data.source.local.table.TableOffenderStatus;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.model.database.entities.EntityOffenderStatus;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;

import java.util.ArrayList;


public class TableOffenderStatusManager extends BaseTableManager {

    public interface OFFENDER_STATUS_CONS {
        String OFF_VIO_STAT = TableOffenderStatus.COLUMN_OFF_STATUS;
        String OFF_TAG_BATTERY_LEVEL = TableOffenderStatus.COLUMN_OFF_TAG_BATTERY_LEVEL;
        String OFF_IS_IN_RANGE = TableOffenderStatus.COLUMN_OFF_IS_IN_RANGE;
        String OFF_TAG_STAT_BATTERY = TableOffenderStatus.COLUMN_OFF_TAG_STATUS_BATTERY;
        String OFF_TAG_STAT_CASE = TableOffenderStatus.COLUMN_OFF_TAG_STATUS_CASE;
        String OFF_TAG_STAT_STRAP = TableOffenderStatus.COLUMN_OFF_TAG_STATUS_STRAP;
        String OFF_LAST_TAG_RECEIVE = TableOffenderStatus.COLUMN_OFF_LAST_TAG_RECEIVE;
        String OFF_IN_BEACON_ZONE = TableOffenderStatus.COLUMN_OFF_IN_BEACON_ZONE;
        String OFF_BEACON_STAT_CASE = TableOffenderStatus.COLUMN_OFF_BEACON_STATUS_CASE;
        String OFF_BEACON_STAT_PROX = TableOffenderStatus.COLUMN_OFF_BEACON_STATUS_PROX;
        String OFF_BEACON_STAT_MOTION = TableOffenderStatus.COLUMN_OFF_BEACON_STATUS_MOTION;
        String OFF_BEACON_STAT_BATTERY = TableOffenderStatus.COLUMN_OFF_BEACON_STATUS_BATTERY;
        String OFF_BEACON_STAT_CASE_INDEX = TableOffenderStatus.COLUMN_OFF_BEACON_STATUS_CASE_INDEX;
        String OFF_BEACON_STAT_MOTION_INDEX = TableOffenderStatus.COLUMN_OFF_BEACON_STATUS_MOTION_INDEX;
        String OFF_BEACON_STAT_PROX_INDEX = TableOffenderStatus.COLUMN_OFF_BEACON_STATUS_PROX_INDEX;
        String OFF_BEACON_HAS_OPEN_EVENT = TableOffenderStatus.COLUMN_OFF_BEACON_HAS_OPEN_EVENT;
        String OFF_TAG_STAT_CASE_INDEX = TableOffenderStatus.COLUMN_OFF_TAG_STATUS_CASE_INDEX;
        String OFF_TAG_STAT_STRAP_INDEX = TableOffenderStatus.COLUMN_OFF_TAG_STATUS_STRAP_INDEX;
        String OFF_STAT_DEVICE_BATTERY_STAT = TableOffenderStatus.COLUMN_OFF_STAT_DEVICE_BATTERY_STAT;
        String OFF_STAT_DEVICE_BATTERY_PERCENTAGE = TableOffenderStatus.COLUMN_OFF_STAT_DEVICE_BATTERY_PERCENTAGE;
        String OFF_LAST_GPS_POINT = TableOffenderStatus.COLUMN_OFF_LAST_GPS_POINT;
        String OFF_ZONE_VERSION = TableOffenderStatus.COLUMN_OFF_ZONE_VERSION;
        String OFF_LAST_SCHEUDLE_UPDATE = TableOffenderStatus.COLUMN_OFF_LAST_SCHEDULE_UPDATE;
        String OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER = TableOffenderStatus.COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_TESTS_COUNTER;
        String OFF_SCHEDULE_OF_ZONES_BIOMETRIC_LAST_CHECK = TableOffenderStatus.COLUMN_OFF_SCHEDULE_OF_ZONES_BIOMETRIC_LAST_CHECK;
        String OFF_DEVICE_DOWNLOADED_VERSION = TableOffenderStatus.COLUMN_OFF_DEVICE_DOWNLOADED_VERSION;
        String OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME = TableOffenderStatus.COLUMN_OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME;
        String OFF_ACTIVATE_STATUS = TableOffenderStatus.COLUMN_OFF_IS_OFFENDER_ACTIVATED;
        String OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD = TableOffenderStatus.COLUMN_OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD;
        String OFF_LAST_CREATED_EVENT_TYPE = TableOffenderStatus.COLUMN_OFF_LAST_CREATED_EVENT_TYPE;
        String OFF_LAST_OFFENDER_REQUEST_ID_TREATED = TableOffenderStatus.COLUMN_OFF_LAST_OFFENDER_REQUEST_ID_TREATED;
        String OFF_LAST_OFFENDER_REQUEST_STATUS = TableOffenderStatus.COLUMN_OFF_LAST_OFFENDER_REQUEST_STATUS;
        String OFF_ACTIVATION_OFFENDER_REQUEST_ID_TREATED = TableOffenderStatus.COLUMN_OFF_ACTIVATION_OFFENDER_REQUEST_ID_TREATED;
        String OFF_LAST_SYNC_RESPONSE_FROM_SERVER_JSON = TableOffenderStatus.COLUMN_OFF_LAST_SYNC_RESPONSE_FROM_SERVER_JSON;
        String OFF_CURRENT_PM_COM_PROFILE = TableOffenderStatus.COLUMN_OFF_CURRENT_PM_COM_PROFILE;
        String OFF_IS_MOBILE_DATA_ENABLED = TableOffenderStatus.COLUMN_OFF_IS_MOBILE_DATA_ENABLED;
        String OFF_TAG_TX_INDEX = TableOffenderStatus.COLUMN_OFF_TAG_TX_INDEX;
        String OFF_BEACON_TX_INDEX = TableOffenderStatus.COLUMN_OFF_BEACON_TX_INDEX;
        String OFF_CURRENT_COMM_NETWORK_TEST_STATUS = TableOffenderStatus.COLUMN_OFF_CURRENT_COMM_NETWORK_TEST_STATUS;
        String OFF_START_NETWORK_STATUS_COUNTER = TableOffenderStatus.COLUMN_OFF_START_NETWORK_STATUS_COUNTER;
        String OFF_FAILED_HANDLE_REQUESTS_LIST = TableOffenderStatus.COLUMN_OFF_FAILED_HANDLE_REQUESTS_LIST;
        String OFF_STAT_DEVICE_TEMPERATURE = TableOffenderStatus.COLUMN_OFF_STAT_DEVICE_TEMPERATURE;
        String OFF_BEACON_BATTERY_LEVEL = TableOffenderStatus.COLUMN_OFF_BEACON_BATTERY_LEVEL;
        String OFF_LAST_BEACON_RECEIVE = TableOffenderStatus.COLUMN_OFF_LAST_BEACON_RECEIVE;
        String IS_CYCLE_FINISHED_SUCCESSFULY = TableOffenderStatus.COLUMN_OFF_IS_CYCLE_FINISHED_SUCCESSFULLY;
        String OFF_SIM_ICCID = TableOffenderStatus.COLUMN_OFF_SIM_ICCID;
        String OFF_TIME_INITIATED_FLIGHT_MODE_END = TableOffenderStatus.COLUMN_OFF_TIME_INITIATED_FLIGHT_MODE_END;
        String OFF_LAST_LOCATION_UTC_TIME = TableOffenderStatus.COLUMN_OFF_LAST_LOCATION_UTC_TIME;
        String OFF_LAST_LBS_LOCATION_UTC_TIME = TableOffenderStatus.COLUMN_OFF_LAST_LBS_LOCATION_UTC_TIME;
        String COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS = TableOffenderStatus.COLUMN_DEVICE_STATUS_LOCKED_ATTEMPTS;
        String COLUMN_DEVICE_STATUS_LAST_SUCCESSFULY_COM = TableOffenderStatus.COLUMN_DEVICE_STATUS_LAST_SUCCESSFULLY_COM;

        String OFFENDER_IN_PURECOM_ZONE = TableOffenderStatus.COLUMN_DEVICE_STATUS_OFFENDER_IN_PURECOM_ZONE;
        String TAG_MOTION = TableOffenderStatus.COLUMN_DEVICE_STATUS_TAG_MOTION;
        String OFF_TAG_STAT_MOTION_INDEX = TableOffenderStatus.OFF_TAG_STAT_MOTION_INDEX;
    }


    public interface CURRENT_COMM_NETWORK_FAILURE_RESET_STATE {
        int NORMAL = 0;
        int RESTART_MOBILE_DATA = 1;
        int RESTART_DEVICE = 2;
    }

    private TableOffenderStatusManager() {
    }

    private static final TableOffenderStatusManager INSTANCE = new TableOffenderStatusManager();


    public static TableOffenderStatusManager sharedInstance() {
        return INSTANCE;
    }

    public EntityGpsPoint getOffenderLastGpsPoint() {

        String lstGpsPointJsonString = getStringValueByColumnName(OFFENDER_STATUS_CONS.OFF_LAST_GPS_POINT);
        Gson gson = new Gson();
        return gson.fromJson(lstGpsPointJsonString, new TypeToken<EntityGpsPoint>() {
        }.getType());
    }

    public boolean getIsOffenderInRange() {
        return getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IS_IN_RANGE) > 0;
    }

    public EntityGpsPoint getSmallestAccuracyPointAndAboveGoodThreshold() {

        String smallestAccuracyPointAndAboveGoodThreshouldJsonString = getStringValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_SMALLEST_ACCURACY_POINT_AND_ABOVE_GOOD_THRESHOLD);
        Gson gson = new Gson();
        return gson.fromJson(smallestAccuracyPointAndAboveGoodThreshouldJsonString,
                new TypeToken<EntityGpsPoint>() {
                }.getType());
    }

    public long getCurrentProximityTimeToOpenEvent() {
        boolean isInBeaconZone = getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE) == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE;
        long proximityTime;
        if (isInBeaconZone || isInHomeRadius()) {
            proximityTime = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TIME_SENSITIVITY_INSIDE_BEACON);
        } else {
            proximityTime = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_TIME_SENSITIVITY_OUTSIDE_BEACON);
        }
        return proximityTime;
    }

    public int getProximityRssiLimit() {
        boolean isInBeaconZone = getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_IN_BEACON_ZONE) == TableOffenderDetails.OffenderBeaconZoneStatus.INSIDE_BEACON_ZONE;
        if (isInBeaconZone || isInHomeRadius()) {
            return DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().OffenderConfigRssiHomeRange;
        } else {
            return DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().OffenderConfigRssiOutsideRange;
        }
    }

    public boolean isInHomeRadius() {
        int currentProfile = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_PM_COM_PROFILE);
        int homeProfileId = TableOffenderDetailsManager.sharedInstance().getHomeAddressSettingsObject().ProfileID;
        return currentProfile == homeProfileId;
    }

    @Override
    protected DatabaseTable getTable() {
        return DatabaseAccess.getInstance().tableOffStatus;
    }

    @Override
    protected EnumDatabaseTables getEnumDBTable() {
        return EnumDatabaseTables.TABLE_OFFENDER_STATUS;
    }

    public static class HandleRequestToBeSentToServerData {
        public int requestId;
        public int status;

        public HandleRequestToBeSentToServerData(int requestId, int status) {
            this.requestId = requestId;
            this.status = status;
        }
    }

    public ArrayList<HandleRequestToBeSentToServerData> getFailedHandleRequestsList() {

        String handleRequestsToBeSentToServerString = getStringValueByColumnName(OFFENDER_STATUS_CONS.OFF_FAILED_HANDLE_REQUESTS_LIST);
        ArrayList<HandleRequestToBeSentToServerData> handleRequestsToBeSentToServerList = null;
        Gson gson = new Gson();
        try {
            handleRequestsToBeSentToServerList = gson.fromJson(handleRequestsToBeSentToServerString,
                    new TypeToken<ArrayList<HandleRequestToBeSentToServerData>>() {
                    }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (handleRequestsToBeSentToServerList != null && !handleRequestsToBeSentToServerList.isEmpty()) {
            return handleRequestsToBeSentToServerList;
        } else {
            return new ArrayList<>();
        }
    }

    public void removeFailedHandleRequestsFromList(int requestId) {

        //remove item from list
        ArrayList<HandleRequestToBeSentToServerData> handleRequestToBeSentToServerArray = getFailedHandleRequestsList();
        for (int i = 0; i < handleRequestToBeSentToServerArray.size(); i++) {
            if (handleRequestToBeSentToServerArray.get(i).requestId == requestId) {
                handleRequestToBeSentToServerArray.remove(i);
                break;
            }
        }

        //update new json
        String handleRequestToBeSentToServerJson = new Gson().toJson(handleRequestToBeSentToServerArray);
        updateColumnString(OFFENDER_STATUS_CONS.OFF_FAILED_HANDLE_REQUESTS_LIST, handleRequestToBeSentToServerJson);
    }

    public void updateFailedHandleRequestsList(HandleRequestToBeSentToServerData handleRequestToBeSentToServerData) {

        //add item to list
        ArrayList<HandleRequestToBeSentToServerData> handleRequestToBeSentToServerArray = getFailedHandleRequestsList();
        handleRequestToBeSentToServerArray.add(handleRequestToBeSentToServerData);

        //update new json
        String handleRequestToBeSentToServerJson = new Gson().toJson(handleRequestToBeSentToServerArray);
        updateColumnString(OFFENDER_STATUS_CONS.OFF_FAILED_HANDLE_REQUESTS_LIST, handleRequestToBeSentToServerJson);
    }

    public boolean isHandleRequestExistsInFailedRequests(int requestId) {
        ArrayList<HandleRequestToBeSentToServerData> handleRequestToBeSentToServerArray = getFailedHandleRequestsList();
        for (int i = 0; i < handleRequestToBeSentToServerArray.size(); i++) {
            if (handleRequestToBeSentToServerArray.get(i).requestId == requestId) {
                return true;
            }
        }
        return false;
    }

    public EntityOffenderStatus getRecordOffStatus() {
        return DatabaseAccess.getInstance().tableOffStatus.Get();
    }
}
