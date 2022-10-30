package com.supercom.puretrack.data.source.remote.requests_listeners;

import com.supercom.puretrack.data.source.remote.parsers.GetDeviceConfigurationResultParser.DeviceConfigurationType;

public interface GetDeviceConfigurationListener {
    void handleResponse(String response, DeviceConfigurationType deviceConfigurationType, int versionNumber);
}