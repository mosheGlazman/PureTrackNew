package com.supercom.puretrack.data.source.remote.requests;


import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetOffenderScheduleOfZoneListener;


public class GetOffenderScheduleOfZoneRequest extends BaseAsyncTaskRequest {

    GetOffenderScheduleOfZoneListener mHandlerOffenderScheduleOfZone;
    int zoneId;
    private final int scheduleVersion;

    public GetOffenderScheduleOfZoneRequest(GetOffenderScheduleOfZoneListener handlerOffenderScheduleOfZones, int ScheduleVersion, int ZoneId) {
        mHandlerOffenderScheduleOfZone = handlerOffenderScheduleOfZones;
        scheduleVersion = ScheduleVersion;
        zoneId = ZoneId;
    }

    @Override
    protected String getHttpRequestType() {
        return "GET";
    }

    @Override
    protected String getServiceRequestString() {
        NetworkRepository networkRepository = NetworkRepository.getInstance();
        return "GetOffenderScheduleOfZone?"
                + "zoneId=" + zoneId
                + "&deviceId=" + NetworkRepository.getDeviceSerialNumber()//String.valueOf(funcGetDeviceId())//DEVICE_ID)
                + "&token=" + networkRepository.getTokenKey()
                + "&offenderId=" + DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().offenderId//CONS_OFFENDER_ID.JAMES_SAWYER)
                + "&scheduleVersion=" + scheduleVersion;
    }

    @Override
    protected String getBody() {
        return "";
    }

    @Override
    protected void startHttpResponseHandle(String result, int responseCode) {
        mHandlerOffenderScheduleOfZone.handleResponse(result, zoneId, scheduleVersion);
    }

}
