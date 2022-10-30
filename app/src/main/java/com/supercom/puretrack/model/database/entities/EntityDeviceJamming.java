package com.supercom.puretrack.model.database.entities;

public class EntityDeviceJamming extends DatabaseEntity{

    public int enabled;
    public int minimumGoodCellularLevelWcdma3G;
    public int minimumGoodCellularLevelLte4G;
    public int jammingEventTimerSensitivity;
    public int cellularLevelSampleInterval;

    public EntityDeviceJamming(int enabled, int minimumGoodCellularLevelWcdma3G, int minimumGoodCellularLevelLte4G, int jammingEventTimerSensitivity, int cellularLevelSampleInterval) {
        this.enabled = enabled;
        this.minimumGoodCellularLevelWcdma3G = minimumGoodCellularLevelWcdma3G;
        this.minimumGoodCellularLevelLte4G = minimumGoodCellularLevelLte4G;
        this.jammingEventTimerSensitivity = jammingEventTimerSensitivity;
        this.cellularLevelSampleInterval = cellularLevelSampleInterval;
    }
}
