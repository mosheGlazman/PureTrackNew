package com.supercom.puretrack.model.database.entities;

public class EntityGuestTag extends DatabaseEntity {

    public int enabled;
    public int time;

    public EntityGuestTag(int enabled, int time) {
        this.enabled = enabled;
        this.time = time;
    }
}
