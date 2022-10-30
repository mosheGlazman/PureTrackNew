package com.supercom.puretrack.model.database.entities;

import static com.supercom.puretrack.util.constants.network.ServerUrls.SERVER_URL_AES_KEY_BYTES;

import com.supercom.puretrack.util.encryption.AESUtils;

import java.security.GeneralSecurityException;

public class EntityDeviceDetails extends DatabaseEntity {
    public int DeviceId;
    private String DeviceSn;
    public String CommunicationKey;
    public String SwVersion;
    public String RfFwVersion;
    public long receivedUsageDataOfApplication;
    public long sentDataUsageOfApplication;

    public EntityDeviceDetails(int DeviceId,
                               String DeviceSn,
                               String CommunicationKey,
                               String SwVersion,
                               String RfFwVersion,
                               long receivedUsageDataOfApplication,
                               long sentDataUsageOfApplication) {
        this.DeviceId = DeviceId;
        SetDeviceSN(DeviceSn);
        this.CommunicationKey = CommunicationKey;
        this.SwVersion = SwVersion;
        this.RfFwVersion = RfFwVersion;
        this.receivedUsageDataOfApplication = receivedUsageDataOfApplication;
        this.sentDataUsageOfApplication = sentDataUsageOfApplication;
    }

    public void SetDeviceSN(String strDeviceSn) {
        try {
            this.DeviceSn = AESUtils.encrypt(SERVER_URL_AES_KEY_BYTES, strDeviceSn);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

    }

    public String getDeviceSerialNumber() {
        try {
            return AESUtils.decrypt(SERVER_URL_AES_KEY_BYTES, this.DeviceSn);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String GetDeviceSNClear() {
        return this.DeviceSn;

    }
}
