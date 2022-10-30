package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.CreateDeviceListener;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetDateTimeListener;
import com.supercom.puretrack.data.source.remote.requests_listeners.TestConnectionListener;

public class TestConnectionRequest extends BaseAsyncTaskRequest {

    private final TestConnectionListener listener;

    public TestConnectionRequest(TestConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    protected String getHttpRequestType() {
        return "GET";
    }

    @Override
    protected String getServiceRequestString() {
        try {
            return "GetDateTime?"
                    + "token=" + NetworkRepository.getInstance().getTokenKey()
                    + "&deviceId=" + NetworkRepository.getDeviceSerialNumber();
        }catch (Exception ex){
            return "GetDateTime?"
                    + "token=" + "a1212121"
                    + "&deviceId=" + "123456";
        }
    }

    @Override
    protected String getBody() {
        return "";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        listener.onResponse(responseCode==200);
    }
}
