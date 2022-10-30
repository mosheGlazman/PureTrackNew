package com.supercom.puretrack.data.source.remote.requests_listeners;

public interface InsertOffenderLocationsListener {
    void handleResponse(String response, int eventId, int intEventId);
}
