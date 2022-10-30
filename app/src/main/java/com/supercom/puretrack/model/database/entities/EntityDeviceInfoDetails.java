package com.supercom.puretrack.model.database.entities;

public class EntityDeviceInfoDetails extends DatabaseEntity {
    public String sw_version;
    public String hw_version_phone_model;
    public String hw_components;
    public String imei;
    public String os_version;
    public String db_version;
    public String battery_type;

    public EntityDeviceInfoDetails(String sw_version, String hw_version_phone_model,
                                   String hw_components, String imei, String os_version,
                                   String db_version, String battery_type) {
        this.sw_version = sw_version;
        this.hw_version_phone_model = hw_version_phone_model;
        this.hw_components = hw_components;
        this.imei = imei;
        this.os_version = os_version;
        this.db_version = db_version;
        this.battery_type = battery_type;
    }
}
