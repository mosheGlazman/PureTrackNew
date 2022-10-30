package com.supercom.puretrack.model.database.entities;

public class EntityOpenEventLog extends DatabaseEntity {

    public interface UploadResponseStatus {
        int RESPONSE_STATUS_OK = 0;
    }

    public long OpenEventId;
    public int OpenEventType;
    public int OpenEventZoneId;
    public int OpenEventScheduleId;
    public int OpenEventViolationCategory;
    public int OpenEventViolationSeverity;
    public int OpenEventIsHandeled;

    public EntityOpenEventLog(long openEventId, int openEventType, int openEventZoneId, int openEventScheduleId, int openEventViolationCategory, int openEventViolationSeverity,
                              int openEventIsHandeled) {
        this.OpenEventId = openEventId;
        this.OpenEventType = openEventType;
        this.OpenEventZoneId = openEventZoneId;
        this.OpenEventScheduleId = openEventScheduleId;
        this.OpenEventViolationCategory = openEventViolationCategory;
        this.OpenEventViolationSeverity = openEventViolationSeverity;
        this.OpenEventIsHandeled = openEventIsHandeled;
    }
}
