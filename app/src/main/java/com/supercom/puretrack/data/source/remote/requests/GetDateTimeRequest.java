package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetDateTimeListener;

public class GetDateTimeRequest extends BaseAsyncTaskRequest {

    private final GetDateTimeListener mHandler;

    public GetDateTimeRequest(GetDateTimeListener handler) {
        mHandler = handler;
    }

    @Override
    protected String getHttpRequestType() {
        return "GET";
    }

    @Override
    protected String getServiceRequestString() {
        return "GetDateTime?"
                + "token=" + NetworkRepository.getInstance().getTokenKey()
                + "&deviceId=" + NetworkRepository.getDeviceSerialNumber();
    }

    @Override
    protected String getBody() {
        return "";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandler.handleResponse(result);
    }

}
