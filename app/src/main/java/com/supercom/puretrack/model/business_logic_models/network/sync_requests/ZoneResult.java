package com.supercom.puretrack.model.business_logic_models.network.sync_requests;

import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;

public class ZoneResult {
    public static final int DB_RESULT_ERR = -1;

    public int zoneId;
    public int scheduleResult = NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS;
    public int zoneResult = NetworkRepositoryConstants.REQUEST_RESULT_OK;

    public ZoneResult(int ZoneId) {
        zoneId = ZoneId;
    }
}
