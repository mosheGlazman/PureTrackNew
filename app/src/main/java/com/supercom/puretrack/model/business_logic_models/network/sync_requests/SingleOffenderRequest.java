package com.supercom.puretrack.model.business_logic_models.network.sync_requests;

import java.util.ArrayList;

public class SingleOffenderRequest {
    public String RequestId;
    public int OffenderRequestType;
    public int OffenderId;
    public ArrayList<SingleSyncRequest> RequestData;            // not parsed, it depends on request type.
}
