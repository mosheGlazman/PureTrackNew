package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetAuthenticationTokenListener;

public class GetAuthenticationTokenRequest extends BaseAsyncTaskRequest {
    private final GetAuthenticationTokenListener mHandler;

    public GetAuthenticationTokenRequest(GetAuthenticationTokenListener handler) {
        mHandler = handler;
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "GetAuthenticationToken";
    }

    @Override
    protected String getBody() {
        String installedVersionNumber = App.getInstalledVersionNumber();

        return "<root type=\"object\"> " +
                "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +
                "<password type=\"string\">" + NetworkRepository.getInstance().getServerPassword() + "</password>" +
                "<version type=\"string\">" + installedVersionNumber + "</version>" +
                "<comm_type type=\"string\">" + App.GetCommDebugString() + "</comm_type>" +
                "</root>";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandler.handleResponse(result);
    }
}