package com.supercom.puretrack.model.database.entities;

public class EntityDeviceInfoStatus extends DatabaseEntity {
    public String status_utc_time;
    public String operational_mode;
    public String battery_level;
    public String temperature;
    public String tag_last_ping;
    public String tag_battery;
    public String is_Tag_battery_tamper;
    public String beacon_last_ping;
    public String beacon_battery;
    public String is_beacon_battery_tamper;
    public String offender_in_range;
    public String knox_activated;
    public String kiosk_mode_enabled;
    public String event_upload_status;
    public String location_upload_status;


    public EntityDeviceInfoStatus(String utc_time, String operational_mode, String battery_level, String temperature, String tag_last_ping, String tag_battery, String is_Tag_battery_tamper,
                                  String beacon_last_ping, String beacon_battery, String is_beacon_battery_tamper, String offender_in_range, String knox_activated, String kiosk_mode_enabled,
                                  String event_upload_status, String location_upload_status) {
        this.status_utc_time = utc_time;
        this.operational_mode = operational_mode;
        this.battery_level = battery_level;
        this.temperature = temperature;
        this.tag_last_ping = tag_last_ping;
        this.tag_battery = tag_battery;
        this.is_Tag_battery_tamper = is_Tag_battery_tamper;
        this.beacon_last_ping = beacon_last_ping;
        this.beacon_battery = beacon_battery;
        this.is_beacon_battery_tamper = is_beacon_battery_tamper;
        this.offender_in_range = offender_in_range;
        this.knox_activated = knox_activated;
        this.kiosk_mode_enabled = kiosk_mode_enabled;


        this.event_upload_status = event_upload_status;
        this.location_upload_status = location_upload_status;

    }
}
