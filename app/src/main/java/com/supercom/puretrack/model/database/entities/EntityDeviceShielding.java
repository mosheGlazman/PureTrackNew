package com.supercom.puretrack.model.database.entities;

public class EntityDeviceShielding extends DatabaseEntity {

    public int enabled;
    public int openEventThreshold;
    public int openEventCellEnabled;
    public int openEventBluetoothEnabled;
    public int openEventWifiEnabled;
    public int closeEventThreshold;
    public int closeEventCellEnabled;
    public int closeEventBluetoothEnabled;
    public int closeEventWifiEnabled;
    public int checkIntervalSec;
    public int wifiThresholdSec;
    public int mobileNetworkThresholdSec;
    public int bleThresholdSec;


    public EntityDeviceShielding(int enabled, int openEventThreshold, int openEventCellEnabled, int openEventBluetoothEnabled, int openEventWifiEnabled, int closeEventThreshold, int closeEventCellEnabled, int closeEventBluetoothEnabled, int closeEventWifiEnabled, int checkIntervalSec, int bleThresholdSec, int wifiThresholdSec, int mobileNetworkThresholdSec) {
        this.enabled = enabled;
        this.openEventThreshold = openEventThreshold;
        this.openEventCellEnabled = openEventCellEnabled;
        this.openEventBluetoothEnabled = openEventBluetoothEnabled;
        this.openEventWifiEnabled = openEventWifiEnabled;
        this.closeEventThreshold = closeEventThreshold;
        this.closeEventCellEnabled = closeEventCellEnabled;
        this.closeEventBluetoothEnabled = closeEventBluetoothEnabled;
        this.closeEventWifiEnabled = closeEventWifiEnabled;
        this.checkIntervalSec = checkIntervalSec;
        this.bleThresholdSec = bleThresholdSec;
        this.wifiThresholdSec = wifiThresholdSec;
        this.mobileNetworkThresholdSec = mobileNetworkThresholdSec;
    }
}
