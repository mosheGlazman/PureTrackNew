package com.supercom.puretrack.model.database.entities;

public class EntityOffenderPhoto extends DatabaseEntity {

    public String photoEncodedToBase64;
    public String requestId;
    public int eventId;

    public EntityOffenderPhoto(String photoEncodedToBase64, String requestId, int eventId) {
        this.photoEncodedToBase64 = photoEncodedToBase64;
        this.requestId = requestId;
        this.eventId = eventId;
    }
}
