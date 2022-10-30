package com.supercom.puretrack.data.source.remote.requests;


import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetOffenderRequestListener;


public class GetOffenderRequests extends BaseAsyncTaskRequest {

    private final GetOffenderRequestListener mHandlerGetOffenderRequest;

    public GetOffenderRequests(GetOffenderRequestListener handlerOffenderRequest) {
        mHandlerGetOffenderRequest = handlerOffenderRequest;
    }

    @Override
    protected String getHttpRequestType() {
        return "GET";
    }

    @Override
    protected String getServiceRequestString() {
        return "GetOffenderRequests?"
                + "token=" + NetworkRepository.getInstance().getTokenKey()
                + "&deviceId=" + NetworkRepository.getDeviceSerialNumber();
    }

    @Override
    protected String getBody() {
        return "";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandlerGetOffenderRequest.handleResponse(result);
    }

}
