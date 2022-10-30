package com.supercom.puretrack.model.database.enums;

public enum EnumDatabaseColumnType {
    COLUMN_TYPE_INTEGER_PK(0),
    COLUMN_TYPE_INTEGER(1),
    COLUMN_TYPE_STRING(2),
    COLUMN_TYPE_REAL(3);

    public final int value;

    EnumDatabaseColumnType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
