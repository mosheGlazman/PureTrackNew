package com.supercom.puretrack.model.database.enums;

public enum EnumCallContact {
    CONTACT_OFFICER(0),
    CONTACT_AGENCY(1),
    CONTACT_EMERGENCY(2),
    CONTACT_MAX_VALUE(3);

    private final int value;

    EnumCallContact(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


}
