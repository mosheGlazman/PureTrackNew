package com.supercom.puretrack.model.database.entities;

public class EntityDeviceInfoCellular extends DatabaseEntity {
    public String cell_utc_time;
    public String registration_type;
    public String network_id;
    public String cell_reception;
    public String cell_mobile_data;
    public String sim_id;
    public String device_phone_number;

    public EntityDeviceInfoCellular(String utc_time, String registration_type, String network_id, String cell_reception,
                                    String cell_mobile_data, String sim_id, String device_phone_number) {
        this.cell_utc_time = utc_time;
        this.registration_type = registration_type;
        this.network_id = network_id;
        this.cell_reception = cell_reception;
        this.cell_mobile_data = cell_mobile_data;
        this.sim_id = sim_id;
        this.device_phone_number = device_phone_number;
    }
}
