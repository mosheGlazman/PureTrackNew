package com.supercom.puretrack.model.database.entities;

public class EntityZones extends DatabaseEntity {
    public int ZoneId;
    public String ZoneName;
    public int OffenderId;
    public int TypeId;        // EnumExclustionType
    public int ShapeType;
    public String PointsJsonStr;
    public double Latitude;
    public double Longitude;
    public float Radius;
    public int SampleRate;
    public String Note;
    public int isIntoExclusionZoneState;
    public int InclusionZoneCntBuffer;
    public int ExclusionZoneCntBuffer;
    public int defaultAppointmentTypeId;
    public int zoneVersion;
    public int scheduleVersionOfZone;
    public int bufferZone;
    public int isIntoBufferZoneState;
    public int EnteringZoneCnt;
    public int ExitingZoneCnt;


    public EntityZones(int ZoneId,
                       String ZoneName,
                       int OffenderId,
                       int TypeId,
                       int ShapeType,
                       String PointsJsonStr,
                       double Latitude,
                       double Longitude,
                       float Radius,
                       int SampleRate,
                       String Note,
                       int isIntoExclusionZoneState,
                       int EnteringZoneCnt,
                       int InclusionZoneCntBuffer,
                       int ExitingZoneCnt,
                       int ExclusionZoneCntBuffer,
                       int defaultAppointmentTypeId,
                       int zoneVersion,
                       int scheduleVersionOfZone,
                       int bufferZone,
                       int IsIntoBufferZoneState) {
        this.ZoneId = ZoneId;
        this.ZoneName = ZoneName;
        this.OffenderId = OffenderId;
        this.TypeId = TypeId;
        this.ShapeType = ShapeType;
        this.PointsJsonStr = PointsJsonStr;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Radius = Radius;
        this.SampleRate = SampleRate;
        this.Note = Note;
        this.isIntoExclusionZoneState = isIntoExclusionZoneState;
        this.InclusionZoneCntBuffer = InclusionZoneCntBuffer;
        this.ExclusionZoneCntBuffer = ExclusionZoneCntBuffer;
        this.defaultAppointmentTypeId = defaultAppointmentTypeId;
        this.zoneVersion = zoneVersion;
        this.scheduleVersionOfZone = scheduleVersionOfZone;
        this.bufferZone = bufferZone;
        this.isIntoBufferZoneState = IsIntoBufferZoneState;
        this.EnteringZoneCnt = EnteringZoneCnt;
        this.ExitingZoneCnt = ExitingZoneCnt;
    }
}
