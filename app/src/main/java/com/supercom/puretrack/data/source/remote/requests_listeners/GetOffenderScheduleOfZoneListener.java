package com.supercom.puretrack.data.source.remote.requests_listeners;

public interface GetOffenderScheduleOfZoneListener {
    void handleResponse(String response, int zoneId, int scheduleVersion);
}
