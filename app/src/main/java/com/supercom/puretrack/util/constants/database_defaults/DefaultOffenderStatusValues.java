package com.supercom.puretrack.util.constants.database_defaults;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;

public class DefaultOffenderStatusValues {

    public static final int DEFAULT_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME = 0;
    public static final int DEFAULT_IS_OFFENDER_ACTIVATED = 0;
    public static final int DEFAULT_BEACON_STATUS_BATTERY = 0;
    public static final int DEFAULT_LAST_OFFENDER_REQUEST_STATUS = NetworkRepositoryConstants.REQUEST_RESULT_OK;
    public static final String DEFAULT_LAST_SYNC_RESPONSE_FROM_SERVER_JSON = "''";
    public static final int DEFAULT_CURRENT_PM_COM_PROFILE = -1;
    public static final int DEFAULT_IS_MOBILE_DATA_ENABLED = 1;
    public static final int DEFAULT_CURRENT_COMM_NETWORK_TEST_STATUS = TableOffenderStatusManager.CURRENT_COMM_NETWORK_FAILURE_RESET_STATE.NORMAL;
    public static final int DEFAULT_START_NETWORK_STATUS_COUNTER = 1;
    public static final int DEFAULT_IS_CYCLE_FINISHED_SUCCESSFULLY = 1;
    public static final String DEFAULT_FAILED_HANDLE_REQUESTS_LIST = "[]";
    public static final String DEFAULT_SIM_ICCID = "";
    public static final int DEFAULT_INITIATED_FLIGHT_MODE_END = 0;
    public static final int DEFAULT_OFFENDER_IN_PURECOM_ZONE = 0;
    public static final int DEFAULT_TAG_MOTION = 1;
    public static final int DEFAULT_TAG_MOTION_INDEX = -1;
}
