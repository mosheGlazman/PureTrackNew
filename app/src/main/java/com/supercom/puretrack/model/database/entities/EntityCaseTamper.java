package com.supercom.puretrack.model.database.entities;

public class EntityCaseTamper extends DatabaseEntity {

    public int enabled;
    public int caseClosedThreshold;
    public int magnetCalibrationOnRestart;
    public int accelerationThreshold=105;
    public int accelerationMillisProximity=20000;

    public EntityCaseTamper(int enabled, int caseClosedThreshold, int magnetCalibrationOnRestart,int accelerationThreshold,int accelerationMillisProximity) {
        this.enabled = enabled;
        this.caseClosedThreshold = caseClosedThreshold;
        this.magnetCalibrationOnRestart = magnetCalibrationOnRestart;
        this.accelerationThreshold = accelerationThreshold;
        this.accelerationMillisProximity = accelerationMillisProximity;
    }
}
