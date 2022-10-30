package com.supercom.puretrack.model.database.entities;

public class EntitySelfDiagnosticEvent extends DatabaseEntity {

    public int enabled;
    public int gyroscopeSensitivity;
    public int magneticSensitivity;

    public EntitySelfDiagnosticEvent(int enabled, int gyroscopeSensitivity, int magneticSensitivity) {
        this.enabled = enabled;
        this.gyroscopeSensitivity = gyroscopeSensitivity;
        this.magneticSensitivity = magneticSensitivity;
    }
}
