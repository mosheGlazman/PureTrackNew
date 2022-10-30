package com.supercom.puretrack.model.database.entities;

public class EntitySchedule extends DatabaseEntity {
    public int AppointmentId;
    public int OffenderId;
    public long StartTime;
    public long EndTime;
    public int Type;        // EnumScheduleType
    public int BioTestsNum;
    public String Note;

    public EntitySchedule(int AppointmentId, int OffenderId, long StartTime, long EndTime, int Type, int BioTestsNum, String Note) {
        this.AppointmentId = AppointmentId;
        this.OffenderId = OffenderId;
        this.StartTime = StartTime;
        this.EndTime = EndTime;
        this.Type = Type;
        this.BioTestsNum = BioTestsNum;
        this.Note = Note;
    }
}
