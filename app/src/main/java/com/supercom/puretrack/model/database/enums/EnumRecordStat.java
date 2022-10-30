package com.supercom.puretrack.model.database.enums;

// Records status for GPS points, events logs etc.
public enum EnumRecordStat {
    REC_STATUS_NEW(0),
    REC_STATUS_UPLOADING(1),
    REC_STATUS_DELIVERED(2),
    REC_STATUS_DELETED(3);

    private final int value;

    EnumRecordStat(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
