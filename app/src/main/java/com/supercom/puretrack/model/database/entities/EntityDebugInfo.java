package com.supercom.puretrack.model.database.entities;

public class EntityDebugInfo extends DatabaseEntity {

    public long dateTime;
    public int debugLV1Id;
    public String message;
    public int messageId;
    public int mouduleId;
    public int objectId;
    public int requestId;
    public int subModuleId;
    public int priorityRecord;

    public EntityDebugInfo(long dateTime, int debugLV1Id, String message, int messageId, int moudleId, int objectId,
                           int requestId, int subMoudleId, int priorityRecord) {
        this.dateTime = dateTime;
        this.debugLV1Id = debugLV1Id;
        this.message = message;
        this.messageId = messageId;
        this.mouduleId = moudleId;
        this.objectId = objectId;
        this.requestId = requestId;
        this.subModuleId = subMoudleId;
        this.priorityRecord = priorityRecord;
    }

}
