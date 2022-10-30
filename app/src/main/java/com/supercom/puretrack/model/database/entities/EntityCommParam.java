package com.supercom.puretrack.model.database.entities;

public class EntityCommParam extends DatabaseEntity {
    public int Interval;
    public int IntervalLowBattery;
    public int RetryWaitTime;
    public int HttpTimeout;

    public EntityCommParam(int Interval, int IntervalLowBattery, int RetryWaitTime, int HttpTimeout) {
        this.Interval = Interval;
        this.IntervalLowBattery = IntervalLowBattery;
        this.RetryWaitTime = RetryWaitTime;
        this.HttpTimeout = HttpTimeout;
    }
}
