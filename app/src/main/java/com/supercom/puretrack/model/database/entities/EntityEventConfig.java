package com.supercom.puretrack.model.database.entities;

public class EntityEventConfig extends DatabaseEntity {
    public int EventType;
    public int IsOpenEvent;
    public String EventDescr;
    public int ViolationCategory;
    public int ViolationSeverity;
    public int actionType;

    public EntityEventConfig(int eventType, int isOpenEvent, String eventDescr, int violationCategory, int violationSeverity, int action) {
        this.EventType = eventType;
        this.IsOpenEvent = isOpenEvent;
        this.EventDescr = eventDescr;
        this.ViolationCategory = violationCategory;
        this.ViolationSeverity = violationSeverity;
        this.actionType = action;
    }
}
