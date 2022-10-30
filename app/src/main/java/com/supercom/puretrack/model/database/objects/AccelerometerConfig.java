package com.supercom.puretrack.model.database.objects;

public class AccelerometerConfig {
    public int Enabled;
    public int MotionWinSamples;
    public int MotionWinPercentage;
    public double MotionThreshold;
    public int StaticWinSamples;
    public int StaticWinPercentage;
    public double staticThreshold;
    public int motion_sample_time;
    public int motion_window_time;
    public int motion_window_level;

    public AccelerometerConfig() {
        Enabled = 1;
        MotionWinSamples = 100;
        MotionWinPercentage = 30;
        MotionThreshold = 0.2;
        StaticWinSamples = 3000;
        StaticWinPercentage = 95;
        staticThreshold = 0.2;
        motion_sample_time = 5;
        motion_window_time = 120;
        motion_window_level = 30;
    }
}
