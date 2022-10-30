package com.supercom.puretrack.model.business_logic_models.bluetooth_parsing;

import android.bluetooth.BluetoothDevice;

public class BaseDeviceModel {

    private int rollingCode;
    private int LightAdcValue;
    private int BatteryAdcValue;
    private int CaseTamperIndexNew;
    private int siteCode;
    private int connectionRssi;
    private int structureVersion;
    private String stringPacketUI;
    private int missedIndexCounter;

    //other fields
    private BluetoothDevice device;

    public int getRollingCode() {
        return rollingCode;
    }

    public void setRollingCode(int rollingCode) {
        this.rollingCode = rollingCode;
    }

    public int getLightAdcValue() {
        return LightAdcValue;
    }

    public void setLightAdcValue(int lightAdcValue) {
        LightAdcValue = lightAdcValue;
    }

    public int getBatteryAdcValue() {
        return BatteryAdcValue;
    }

    public void setBatteryAdcValue(int batteryAdcValue) {
        BatteryAdcValue = batteryAdcValue;
    }

    public int getCaseTamperIndexNew() {
        return CaseTamperIndexNew;
    }

    public void setCaseTamperIndexNew(int caseTamperIndexNew) {
        CaseTamperIndexNew = caseTamperIndexNew;
    }

    public int getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(int siteCode) {
        this.siteCode = siteCode;
    }

    public int getConnectionRssi() {
        return connectionRssi;
    }

    public void setConnectionRssi(int connectionRssi) {
        this.connectionRssi = connectionRssi;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getStructureVersion() {
        return structureVersion;
    }

    public void setStructureVersion(int structureVersion) {
        this.structureVersion = structureVersion;
    }

    public String getStringPacketUI() {
        return stringPacketUI;
    }

    public void setStringPacketUI(String stringPacketUI) {
        this.stringPacketUI = stringPacketUI;
    }

    public int getMissedIndex() {
        return missedIndexCounter;
    }

    public void setMissedIndex(int missedIndex) {
        this.missedIndexCounter = missedIndex;
    }

}
