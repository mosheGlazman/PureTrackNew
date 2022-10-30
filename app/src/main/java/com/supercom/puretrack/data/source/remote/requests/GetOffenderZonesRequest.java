package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetOffenderZonesListener;

public class GetOffenderZonesRequest extends BaseAsyncTaskRequest {

    GetOffenderZonesListener mHandlerOffenderZones;
    String dataVersion;

    public GetOffenderZonesRequest(GetOffenderZonesListener handlerOffenderZones, String dataVersion) {
        mHandlerOffenderZones = handlerOffenderZones;
        this.dataVersion = dataVersion;
    }

    @Override
    protected String getHttpRequestType() {
        return "GET";
    }

    @Override
    protected String getServiceRequestString() {
        NetworkRepository networkRepository = NetworkRepository.getInstance();
        return "GetOffenderZones?"
                + "deviceId=" + NetworkRepository.getDeviceSerialNumber()
                + "&token=" + networkRepository.getTokenKey()
                + "&offenderId=" + DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId
                + "&version=" + dataVersion;
    }

    @Override
    protected String getBody() {
        return "";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandlerOffenderZones.handleResponse(result);
    }

}
