package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.CreateDeviceListener;

public class CreateDeviceRequest extends BaseAsyncTaskRequest {
    private final CreateDeviceListener listener;

    public CreateDeviceRequest(CreateDeviceListener listener) {
        this.listener = listener;
    }

    @Override
    protected String getHttpRequestType() {
        return "GET";
    }

    @Override
    protected String getServiceRequestString() {
        return "api/DeviceInstallation/SetupDevice/RGh7TWjjL5KpTfeeAAVE/4"; // 4 = PT
    }

    @Override
    protected String getBody() {
        return "";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        listener.handleResponse(result);
    }
}
