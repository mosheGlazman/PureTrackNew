package com.supercom.puretrack.model.database.entities;

public class EntityScannerType extends DatabaseEntity {

    public int normalScanEnabled;
    public int macScanEnabled;
    public String manufacturerId;
    public String tagMacAddress;
    public String beaconMacAddress;


    public EntityScannerType(int normalScanEnabled, int macScanEnabled, String manufacturerId, String tagMacAddress, String beaconMacAddress) {
        this.normalScanEnabled = normalScanEnabled;
        this.macScanEnabled = macScanEnabled;
        this.manufacturerId = manufacturerId;
        this.tagMacAddress = tagMacAddress;
        this.beaconMacAddress = beaconMacAddress;
    }
}
