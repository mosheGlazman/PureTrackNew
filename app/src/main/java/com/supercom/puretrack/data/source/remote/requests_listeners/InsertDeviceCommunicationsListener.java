package com.supercom.puretrack.data.source.remote.requests_listeners;

public interface InsertDeviceCommunicationsListener {

    void handleResponse(String response, int eventId, int intEventId);
}
