package com.supercom.puretrack.util.constants.database_defaults;

public class DefaultDatabaseData {

    // Device details
    public static final int DeviceId = 10;
    public static final String DeviceSerialNumber = "";
    public static final String DeviceCommKey = "1";
    public static final String DeviceSwVer = "1.2";
    public static final String DeviceFwVer = "1.2";
    // Offender Details
    public static final int OffenderId = 10;
    // Communication Server Details
    public static final String ServerHttpHeader = "http://";
    public static final String ServerIpAddress = "192.116.235.243";
    public static final String ServerWebService = "/PureMonitorWCFService/RestfulService.svc/";
    // Communication parameters
    public static final int CommSyncIntervalOk = 20;
    public static final int CommSyncIntervalLowBattery = 30;
    public static final int CommRetryWaitTime = 60;
    public static final int CommHttpTimeout = 15;

}
