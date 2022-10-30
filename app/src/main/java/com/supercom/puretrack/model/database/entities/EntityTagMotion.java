package com.supercom.puretrack.model.database.entities;

public class EntityTagMotion extends DatabaseEntity{

    public int enabled;
    public int signalsToNoMotion;
    public int noMotionPercentage;
    public int signalsToMotion;
    public int motionPercentage;

    public EntityTagMotion(int enabled, int signalsToNoMotion, int noMotionPercentage, int signalsToMotion, int motionPercentage) {
        this.enabled = enabled;
        this.signalsToNoMotion = signalsToNoMotion;
        this.noMotionPercentage = noMotionPercentage;
        this.signalsToMotion = signalsToMotion;
        this.motionPercentage = motionPercentage;
    }
}
