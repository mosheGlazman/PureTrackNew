package com.supercom.puretrack.data.source.remote.requests;


import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.parsers.GetDeviceConfigurationResultParser.DeviceConfigurationType;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetDeviceConfigurationListener;


public class GetDeviceConfigurationRequest extends BaseAsyncTaskRequest {

    GetDeviceConfigurationListener mHandlerDeviceConfiguration;
    private final DeviceConfigurationType deviceConfigurationType;
    private final int versionNumber;

    public GetDeviceConfigurationRequest(GetDeviceConfigurationListener handlerConfiguration, DeviceConfigurationType deviceConfigurationType, int versionNumber) {
        mHandlerDeviceConfiguration = handlerConfiguration;
        this.deviceConfigurationType = deviceConfigurationType;
        this.versionNumber = versionNumber;
    }

    @Override
    protected String getHttpRequestType() {
        return "GET";
    }

    @Override
    protected String getServiceRequestString() {
        NetworkRepository networkRepository = NetworkRepository.getInstance();
        return "GetDeviceConfiguration?"
                + "deviceId=" + NetworkRepository.getDeviceSerialNumber()
                + "&token=" + networkRepository.getTokenKey()
                + "&deviceConfigVersion=" + versionNumber;
    }

    @Override
    protected String getBody() {
        return "";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandlerDeviceConfiguration.handleResponse(result, deviceConfigurationType, versionNumber);
    }

    protected int getSpacesToIndentEachLevel() {
        return 0;
    }

}
