package com.supercom.puretrack.model.database.entities;

import com.supercom.puretrack.model.database.enums.EnumRecordStat;

public class EntityGpsPoint extends DatabaseEntity {

    public static final int INITIAL_SYNC_RETRY_COUNT = 0;
    public static final int MAX_SYNC_RETRY_COUNT = 3;

    public interface UploadResponseStatus {
        int RESPONSE_STATUS_GPS_OK_NOT_PROXIMITY = 0;
        int RESPONSE_STATUS_GPS_OK_PROXIMITY_VIOLATION = 1;
        int RESPONSE_STATUS_GPS_OK_PROXIMITY_WARNING = 2;
    }

    public int id;
    public int offenderId;
    public int commStatus;    // values derived from 'EnumRecordStat'
    public long time;
    public double latitude;
    public double longitude;
    public double altitude;
    public float accuracy;
    public int satellitesNumber;
    public int providerType;
    public int pureMonitorSyncRetryCount;
    public int mobileDataEnabled;
    public double speed;
    public double bearing;
    public int isMockLocation;
    public int motionType;
    public int inCharging;
    public int tilt;
    public String xyzString;

    public EntityGpsPoint(int OffId, long Time, double latitude, double longitude, double altitude, float Accuracy, int satellitesNumber, int providerType,
                          int PureMonitorSyncRetryCount, int mobileDataEnabled, double speed, double bearing, int IsMockLocation, int motionType /*, int TimeZoneId*/) {
        this.commStatus = EnumRecordStat.REC_STATUS_NEW.getValue();
        this.offenderId = OffId;
        this.time = Time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = Accuracy;
        this.satellitesNumber = satellitesNumber;
        this.providerType = providerType;
        this.pureMonitorSyncRetryCount = PureMonitorSyncRetryCount;
        this.mobileDataEnabled = mobileDataEnabled;
        this.speed = speed;
        this.bearing = bearing;
        this.isMockLocation = IsMockLocation;
        this.motionType = motionType;
    }

    public EntityGpsPoint(int OffId, long Time, double latitude, double longitude, double altitude, float Accuracy, int satellitesNumber, int providerType,
                          int PureMonitorSyncRetryCount, int mobileDataEnabled,
                          double speed, double bearing, int IsMockLocation, int motionType, int inCharging, int tilt, String xyzString) {
        this.commStatus = EnumRecordStat.REC_STATUS_NEW.getValue();
        this.offenderId = OffId;
        this.time = Time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = Accuracy;
        this.satellitesNumber = satellitesNumber;
        this.providerType = providerType;
        this.pureMonitorSyncRetryCount = PureMonitorSyncRetryCount;
        this.mobileDataEnabled = mobileDataEnabled;
        this.speed = speed;
        this.bearing = bearing;
        this.isMockLocation = IsMockLocation;
        this.motionType = motionType;
        this.inCharging = inCharging;
        this.tilt = tilt;
        this.xyzString = xyzString;
    }


    public EntityGpsPoint(int Id, int OffId, int CommStat, long Time, double latitude, double longitude, double altitude, float Accuracy, int satellitesNumber, int providerType,
                          int PureMonitorSyncRetryCount, int mobileDataEnabled, double speed,
                          double bearing, int IsMockLocation, int motionType, int inCharging, int tilt, String xyzString) {
        this.commStatus = CommStat;
        this.id = Id;
        this.offenderId = OffId;
        this.time = Time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = Accuracy;
        this.satellitesNumber = satellitesNumber;
        this.providerType = providerType;
        this.pureMonitorSyncRetryCount = PureMonitorSyncRetryCount;
        this.mobileDataEnabled = mobileDataEnabled;
        this.speed = speed;
        this.bearing = bearing;
        this.isMockLocation = IsMockLocation;
        this.motionType = motionType;
        this.inCharging = inCharging;
        this.tilt = tilt;
        this.xyzString = xyzString;
    }
    public EntityGpsPoint(int OffId, int CommStat, long Time, double latitude, double longitude, double altitude, float Accuracy, int satellitesNumber, int providerType,
                          int PureMonitorSyncRetryCount, int mobileDataEnabled, double speed,
                          double bearing, int IsMockLocation, int motionType, int inCharging, int tilt, String xyzString) {
        this.commStatus = CommStat;
        this.offenderId = OffId;
        this.time = Time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = Accuracy;
        this.satellitesNumber = satellitesNumber;
        this.providerType = providerType;
        this.pureMonitorSyncRetryCount = PureMonitorSyncRetryCount;
        this.mobileDataEnabled = mobileDataEnabled;
        this.speed = speed;
        this.bearing = bearing;
        this.isMockLocation = IsMockLocation;
        this.motionType = motionType;
        this.inCharging = inCharging;
        this.tilt = tilt;
        this.xyzString = xyzString;
    }

    public EntityGpsPoint(long Time, double latitude, double longitude, double altitude, float Accuracy/*, int TimeZoneId*/) {
        this.commStatus = EnumRecordStat.REC_STATUS_NEW.getValue();
        this.time = Time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = Accuracy;
        this.satellitesNumber = 0;
        this.providerType = 2;
        this.speed = 0;
        this.bearing = 0;
        this.isMockLocation = 0;
        this.mobileDataEnabled = 1;
        this.pureMonitorSyncRetryCount = EntityGpsPoint.INITIAL_SYNC_RETRY_COUNT;
    }
}