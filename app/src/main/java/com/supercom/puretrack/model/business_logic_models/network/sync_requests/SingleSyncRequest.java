package com.supercom.puretrack.model.business_logic_models.network.sync_requests;

import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;

public class SingleSyncRequest {
    public String requestDataVersion;
    public int requestDataType;
    public int requestResult = NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS;
}
