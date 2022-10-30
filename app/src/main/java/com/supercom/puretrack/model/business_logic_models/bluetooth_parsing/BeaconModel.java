package com.supercom.puretrack.model.business_logic_models.bluetooth_parsing;

public class BeaconModel extends TagModel {

    private int tempBeaconId;
    private int ProxOffAdcValue;
    private int ProxOnAdcValue;
    private int BeaconProximityTamperIndexNew;
    private boolean isBeaconTamperProximityOpen;
    private boolean isBeaconTamperCaseOpen;

    //other fields
    private long LongLastPureBeaconPacketRx;

    public int getTempBeaconId() {
        return tempBeaconId;
    }

    public void setTempBeaconId(int tempBeaconId) {
        this.tempBeaconId = tempBeaconId;
    }

    public int getProxOffAdcValue() {
        return ProxOffAdcValue;
    }

    public void setProxOffAdcValue(int proxOffAdcValue) {
        ProxOffAdcValue = proxOffAdcValue;
    }

    public int getProxOnAdcValue() {
        return ProxOnAdcValue;
    }

    public void setProxOnAdcValue(int proxOnAdcValue) {
        ProxOnAdcValue = proxOnAdcValue;
    }

    public int getBeaconProximityTamperIndexNew() {
        return BeaconProximityTamperIndexNew;
    }

    public void setBeaconProximityTamperIndexNew(int beaconProximityTamperIndexNew) {
        BeaconProximityTamperIndexNew = beaconProximityTamperIndexNew;
    }

    public boolean isBeaconTamperProximityOpen() {
        return isBeaconTamperProximityOpen;
    }

    public void setBeaconTamperProximityOpen(boolean isBeaconTamperProximityOpen) {
        this.isBeaconTamperProximityOpen = isBeaconTamperProximityOpen;
    }

    public boolean isBeaconTamperCaseOpen() {
        return isBeaconTamperCaseOpen;
    }

    public void setBeaconTamperCaseOpen(boolean isBeaconTamperCaseOpen) {
        this.isBeaconTamperCaseOpen = isBeaconTamperCaseOpen;
    }

    public long getLongLastPureBeaconPacketRx() {
        return LongLastPureBeaconPacketRx;
    }

    public void setLongLastPureBeaconPacketRx(long longLastPureBeaconPacketRx) {
        LongLastPureBeaconPacketRx = longLastPureBeaconPacketRx;
    }


}
