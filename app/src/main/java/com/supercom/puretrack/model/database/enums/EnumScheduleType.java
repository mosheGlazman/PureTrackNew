package com.supercom.puretrack.model.database.enums;

public enum EnumScheduleType {
    SCHEDULE_TYPE_NONE(0),
    SCHEDULE_TYPE_CGO(1),
    SCHEDULE_TYPE_MBI(2),
    SCHEDULE_TYPE_MBO(3),
    SCHEDULE_TYPE_BIO(4);


    private final int value;
    private final String StrValue;
    private final String Color;

    EnumScheduleType(int value) {
        this.value = value;
        switch (value) {
            case 1:
                this.StrValue = "Can go out"; //CGO
                this.Color = "#006400";
                break;
            case 2:
                this.StrValue = "Must be at Zone"; //MBI
                this.Color = "#8B0000";
                break;
            case 3:
                this.StrValue = "Must leave home"; //MBO
                this.Color = "#8B0000";
                break;
            case 4:
                this.StrValue = "Biometric tests"; //BIO
                this.Color = "#8B0000";
                break;
            default:
                this.StrValue = "";
                this.Color = "#8B0000";
                break;
        }
    }

    public int getValue() {
        return value;
    }

    public String getStrValue() {
        return StrValue;
    }

    public String getColor() {
        return Color;
    }
}
