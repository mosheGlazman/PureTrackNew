package com.supercom.puretrack.model.database.entities;

public class EntityApnDetails extends DatabaseEntity {
    public String apn_icc_id_prefix;
    public String apn_details;
    public String apn_name;
    public String apn_user;
    public String apn_password;
    public int apn_auth_type;

    public EntityApnDetails(String apn_icc_id_prefix, String apn_detals, String apn_name, String apn_user, String apn_password, int apn_auth_type) {
        this.apn_icc_id_prefix = apn_icc_id_prefix;
        this.apn_details = apn_detals;
        this.apn_name = apn_name;
        this.apn_user = apn_user;
        this.apn_password = apn_password;
        this.apn_auth_type = apn_auth_type;
    }

    public EntityApnDetails(String apn_icc_id_prefix, String apn_detals, String apn_name) {
        this.apn_icc_id_prefix = apn_icc_id_prefix;
        this.apn_details = apn_detals;
        this.apn_name = apn_name;
    }
}
