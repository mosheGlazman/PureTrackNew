package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.PostTerminateRequestListener;

public class PostTerminateRequest extends BaseAsyncTaskRequest {
    private final PostTerminateRequestListener mHandlerTerminate;

    public PostTerminateRequest(PostTerminateRequestListener iHttpResponseTerminate) {
        mHandlerTerminate = iHttpResponseTerminate;
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        NetworkRepository networkManagerInstance = NetworkRepository.getInstance();
        return "TerminateSession?"
                + "deviceId=" + NetworkRepository.getDeviceSerialNumber()
                + "&token=" + networkManagerInstance.getTokenKey();
    }

    @Override
    protected String getBody() {
        String deviceId = NetworkRepository.getDeviceSerialNumber();
        String token = NetworkRepository.getInstance().getTokenKey();

        return "<root type=\"object\">" +
                "<deviceId type=\"string\">" + deviceId + "</deviceId>" +
                "<token type=\"string\">" + token + "</token>" +
                "</root>";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandlerTerminate.handleResponse(result);
    }

}
