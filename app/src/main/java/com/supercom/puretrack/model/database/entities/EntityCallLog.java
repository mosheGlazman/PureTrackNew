package com.supercom.puretrack.model.database.entities;

public class EntityCallLog extends DatabaseEntity {

    public String callNumber;
    public int callTypeSystem;
    public String callLength;
    public String callTimeSystem;

    public int callId;
    public int callTypeFiltered;
    public int conducted; //incoming or outgoing
    public long callTimeFiltered; //time in utc

    public EntityCallLog(String callNumber, int callType, String callLength, String callTime, int callId, int callTypeFiltered, int conducted, long callTimeFiltered) {
        this.callNumber = callNumber;
        this.callTypeSystem = callType;
        this.callLength = callLength;
        this.callTimeSystem = callTime;
        this.callId = callId;
        this.callTypeFiltered = callTypeFiltered;
        this.conducted = conducted;
        this.callTimeFiltered = callTimeFiltered;
    }

}
