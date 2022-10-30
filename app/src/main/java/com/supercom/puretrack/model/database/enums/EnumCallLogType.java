package com.supercom.puretrack.model.database.enums;


public enum EnumCallLogType {

    CALL(1),
    SMS(2),
    MESSAGE_FROM_PURECOM(3);

    private final int value;

    EnumCallLogType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
