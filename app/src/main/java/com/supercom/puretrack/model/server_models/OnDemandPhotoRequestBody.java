package com.supercom.puretrack.model.server_models;

public class OnDemandPhotoRequestBody {

    public int offenderID;
    public int offenderRequestID;
    public String data;
    public int eventIDOnDevice;
    public String deviceID;

    public OnDemandPhotoRequestBody(int offenderID, int offenderRequestID, String data, int eventIDOnDevice, String deviceID) {
        this.offenderID = offenderID;
        this.offenderRequestID = offenderRequestID;
        this.data = data;
        this.eventIDOnDevice = eventIDOnDevice;
        this.deviceID = deviceID;
    }
}
