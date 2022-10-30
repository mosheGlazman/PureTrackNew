package com.supercom.puretrack.model.database.enums;

public enum EnumDatabaseTables {
    TABLE_GPS_POINTS(0),
    TABLE_EVENT_LOG(1),
    TABLE_OFFENDER_DETAILS(2),
    TABLE_CALL_CONTACTS(3),
    TABLE_TEXT_MSG(4),
    TABLE_DEVICE_DETAILS(5),
    TABLE_COMM_SERVERS(6),
    TABLE_COMM_PARAMS(7),
    TABLE_SCHEDULE(8),
    TABLE_ZONES(9),
    TABLE_SCHEDULE_OF_ZONES(10),
    TABLE_EVENT_CONFIG(11),
    TABLE_OPEN_EVENT_LOG(12),
    TABLE_OFFENDER_STATUS(13),
    TABLE_CALL_LOG(14),
    TABLE_ZONES_DELETED(15),
    TABLE_DEBUG_INFO(16),
    TABLE_DEVICE_INFO_DETAILS(17),
    TABLE_DEVICE_INFO_STATUS(18),
    TABLE_DEVICE_INFO_CELLULAR(19),
    TABLE_APN_DETAILS(20),
    TABLE_GUEST_TAG(21),
    TABLE_OFFENDER_PHOTO(22),
    TABLE_DEVICE_SHIELDING(23),
    TABLE_TAG_MOTION(24),
    TABLE_SCANNER_TYPE(25),
    TABLE_DEVICE_JAMMING(26),
    TABLE_SELF_DIAGNOSTIC_EVENTS(27),
    TABLE_CASE_TAMPER(28),
    TABLE_AutoRestart(29);


    private final int value;

    EnumDatabaseTables(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
