package com.supercom.puretrack.model.database.entities;

import com.supercom.puretrack.model.database.enums.EnumRecordStat;

public class EntityEventLog extends DatabaseEntity {
    public long PKEventId;
    public int EventZoneId; // erea zone
    public int EvType;
    public long UtcTime;
    public String Timezone;
    public int EntityId;
    public int DevStatus;
    public int OffStatus;
    public String ExtraData;
    public long RelatedEvId;
    public long RequestId;
    public int RecStatus;    // values derived from 'EnumRecordStat'

    public int TagCaseTamperStat;
    public int TagStrapTamperStat;
    public int TagBatteryTamperStat;
    public long BeaconLastCommunication;
    public int BeaconBatteryTamperStat;
    public int BeaconCaseTamperStat;
    public int BeaconProxTamperStat;
    public int OffIsInRange;

    public int PureMonitorSyncRetryCount;
    public String additionalInfo;


    public EntityEventLog(long pkEventId,
                          int eventZoneId,
                          int Type,
                          long UtcTime,
                          String Timezone,
                          int EntityId,
                          int DevStatus,
                          int OffStatus,
                          String ExtraData,
                          long RelatedEvId,
                          long RequestId,

                          int TagCaseTamperStat,
                          int TagStrapTamperStat,
                          int TagBatteryTamperStat,
                          long BeaconLastCommunication,
                          int BeaconBatteryTamperStat,
                          int BeaconCaseTamperStat,
                          int BeaconProxTamperStat,
                          int OffIsInRange,
                          int PureMonitorSyncRetryCount,
                          String additionalInfo
    ) {
        this.PKEventId = pkEventId;
        this.EventZoneId = eventZoneId;
        this.EvType = Type;
        this.UtcTime = UtcTime;
        this.Timezone = Timezone;
        this.EntityId = EntityId;
        this.DevStatus = DevStatus;
        this.OffStatus = OffStatus;
        this.ExtraData = ExtraData;
        this.RelatedEvId = RelatedEvId;
        this.RequestId = RequestId;
        this.RecStatus = EnumRecordStat.REC_STATUS_NEW.getValue();

        this.TagCaseTamperStat = TagCaseTamperStat;
        this.TagStrapTamperStat = TagStrapTamperStat;
        this.TagBatteryTamperStat = TagBatteryTamperStat;
        this.BeaconLastCommunication = BeaconLastCommunication;
        this.BeaconBatteryTamperStat = BeaconBatteryTamperStat;
        this.BeaconCaseTamperStat = BeaconCaseTamperStat;
        this.BeaconProxTamperStat = BeaconProxTamperStat;
        this.OffIsInRange = OffIsInRange;

        this.PureMonitorSyncRetryCount = PureMonitorSyncRetryCount;
        this.additionalInfo = additionalInfo;
    }
}

