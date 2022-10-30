package com.supercom.puretrack.data.source.remote.requests;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoCellular;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoDetails;
import com.supercom.puretrack.model.database.entities.EntityDeviceInfoStatus;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.parsers.InsertDeviceInfoResultParser;

import java.util.List;

public class InsertDeviceInfoRequest extends BaseAsyncTaskRequest {

    private final InsertDeviceInfoResultParser insertDeviceInfoResultParser;
    private final EntityDeviceInfoDetails recordDeviceInfoDetails;
    private final List<EntityDeviceInfoStatus> recordDeviceInfoStatusArray;
    private final List<EntityDeviceInfoCellular> recordDeviceInfoCellularArray;

    public InsertDeviceInfoRequest(InsertDeviceInfoResultParser insertDeviceInfoResultParser,
                                   EntityDeviceInfoDetails recordDeviceInfoDetails, List<EntityDeviceInfoStatus> recordDeviceInfoStatusArray, List<EntityDeviceInfoCellular> recordDeviceInfoCellularArray) {
        this.insertDeviceInfoResultParser = insertDeviceInfoResultParser;
        this.recordDeviceInfoDetails = recordDeviceInfoDetails;
        this.recordDeviceInfoStatusArray = recordDeviceInfoStatusArray;
        this.recordDeviceInfoCellularArray = recordDeviceInfoCellularArray;
    }

    @Override
    protected String getHttpRequestType() {
        return "POST";
    }

    @Override
    protected String getServiceRequestString() {
        return "InsertDeviceInfo";
    }

    @Override
    protected String getBody() {
        String token = NetworkRepository.getInstance().getTokenKey();

        String deviceInfoString =
                "<root type=\"object\">" +
                        "<deviceId type=\"string\">" + NetworkRepository.getDeviceSerialNumber() + "</deviceId>" +
                        "<token type=\"string\">" + token + "</token>" +
                        "<offender_id type=\"string\">" + TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_ID) + "</offender_id>";
        if (recordDeviceInfoDetails != null) {
            deviceInfoString +=
                    "<device_details type=\"array\">" +
                            "<item type=\"object\">" +
                            "<sw_version type=\"string\">" + recordDeviceInfoDetails.sw_version + "</sw_version>" +
                            "<hw_version-phone_model type=\"string\">" + recordDeviceInfoDetails.hw_version_phone_model + "</hw_version-phone_model>" +
                            "<hw_components type=\"string\">" + recordDeviceInfoDetails.hw_components + "</hw_components>" +
                            "<imei type=\"string\">" + recordDeviceInfoDetails.imei + "</imei>" +
                            "<os_version type=\"string\">" + recordDeviceInfoDetails.os_version + "</os_version>" +
                            "<database_version type=\"string\">" + recordDeviceInfoDetails.db_version + "</database_version>" +
                            "<battery_type type=\"string\">" + recordDeviceInfoDetails.battery_type + "</battery_type>" +
                            "</item>" +
                            "</device_details>";
        } else {
            deviceInfoString +=
                    "<device_details type=\"array\">" +
                            "<item type=\"object\">" +
                            "<sw_version type=\"string\">" + "</sw_version>" +
                            "<hw_version-phone_model type=\"string\">" + "</hw_version-phone_model>" +
                            "<hw_components type=\"string\">" + "</hw_components>" +
                            "<imei type=\"string\">" + "</imei>" +
                            "<os_version type=\"string\">" + "</os_version>" +
                            "<database_version type=\"string\">" + "</database_version>" +
                            "<battery_type type=\"string\">" + "</battery_type>" +
                            "</item>" +
                            "</device_details>";
        }
        deviceInfoString += "<device_status type=\"array\">";
        for (EntityDeviceInfoStatus recordDeviceInfoStatus : recordDeviceInfoStatusArray) {
            deviceInfoString +=
                    "<item type=\"object\">" +
                            "<utc_time type=\"string\">" + recordDeviceInfoStatus.status_utc_time + "</utc_time>" +
                            "<operational_mode type=\"string\">" + recordDeviceInfoStatus.operational_mode + "</operational_mode>" +
                            "<battery_level type=\"string\">" + recordDeviceInfoStatus.battery_level + "</battery_level>" +
                            "<temperature type=\"string\">" + recordDeviceInfoStatus.temperature + "</temperature>" +
                            "<tag_last_ping type=\"string\">" + recordDeviceInfoStatus.tag_last_ping + "</tag_last_ping>" +
                            "<tag_battery type=\"string\">" + recordDeviceInfoStatus.tag_battery + "</tag_battery>" +
                            "<is_Tag_battery_tamper type=\"string\">" + recordDeviceInfoStatus.is_Tag_battery_tamper + "</is_Tag_battery_tamper>" +
                            "<beacon_last_ping type=\"string\">" + recordDeviceInfoStatus.beacon_last_ping + "</beacon_last_ping>" +
                            "<beacon_battery type=\"string\">" + recordDeviceInfoStatus.beacon_battery + "</beacon_battery>" +
                            "<is_beacon_battery_tamper type=\"string\">" + recordDeviceInfoStatus.is_beacon_battery_tamper + "</is_beacon_battery_tamper>" +
                            "<offender_in_range type=\"string\">" + recordDeviceInfoStatus.offender_in_range + "</offender_in_range>" +
                            "<events_upload_status type=\"string\">" + recordDeviceInfoStatus.event_upload_status + "</events_upload_status>" +
                            "<locations_upload_status type=\"string\">" + recordDeviceInfoStatus.location_upload_status + "</locations_upload_status>" +
                            "</item>";
        }
        deviceInfoString += "</device_status>" +
                "<cellular_info type=\"array\">";
        for (EntityDeviceInfoCellular recordDeviceInfoCellular : recordDeviceInfoCellularArray) {
            deviceInfoString +=
                    "<item type=\"object\">" +
                            "<utc_time type=\"string\">" + recordDeviceInfoCellular.cell_utc_time + "</utc_time>" +
                            "<registration_type type=\"string\">" + recordDeviceInfoCellular.registration_type + "</registration_type>" +
                            "<network_id type=\"string\">" + recordDeviceInfoCellular.network_id + "</network_id>" +
                            "<cell_reception type=\"string\">" + recordDeviceInfoCellular.cell_reception + "</cell_reception>" +
                            "<cell_mobile_data type=\"string\">" + recordDeviceInfoCellular.cell_mobile_data + "</cell_mobile_data>" +
                            "<sim_id type=\"string\">" + recordDeviceInfoCellular.sim_id + "</sim_id>" +
                            "<device_phone_number type=\"string\">" + recordDeviceInfoCellular.device_phone_number + "</device_phone_number>" +
                            "</item>";
        }
        deviceInfoString += "</cellular_info>" +
                "</root>";

        return deviceInfoString;
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        insertDeviceInfoResultParser.handleResponse(result);
    }
}
