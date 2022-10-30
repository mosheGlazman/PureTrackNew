package com.supercom.puretrack.model.database.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class EntityScheduleOfZones extends DatabaseEntity implements Parcelable {
    public int RecId;
    public int ZoneId;
    public int AppointmentId;
    public int AppointmentTypeId;
    public int DeviceId;
    public int OffenderId;
    public int EntityTypeId;
    public String EntityName;
    public String Note;
    public long StartTime;
    public long EndTime;
    public int amountOfBiometricTests;


    public EntityScheduleOfZones(
            int RecId,
            int ZoneId,
            int AppointmentId,
            int AppointmentTypeId,
            int DeviceId,
            int OffenderId,
            int EntityTypeId,
            String EntityName,
            String Note,
            long StartTime,
            long EndTime,
            int amountOfBiometricTests) {
        this.RecId = RecId;
        this.ZoneId = ZoneId;
        this.AppointmentId = AppointmentId;
        this.AppointmentTypeId = AppointmentTypeId;
        this.DeviceId = DeviceId;
        this.OffenderId = OffenderId;
        this.EntityTypeId = EntityTypeId;
        this.EntityName = EntityName;
        this.Note = Note;
        this.StartTime = StartTime;
        this.EndTime = EndTime;
        this.amountOfBiometricTests = amountOfBiometricTests;
    }

    public static final Creator<EntityScheduleOfZones> CREATOR = new Creator<EntityScheduleOfZones>() {
        @Override
        public EntityScheduleOfZones createFromParcel(Parcel in) {
            int RecId = in.readInt();
            int ZoneId = in.readInt();
            int AppointmentId = in.readInt();
            int AppointmentTypeId = in.readInt();
            int DeviceId = in.readInt();
            int OffenderId = in.readInt();
            int EntityTypeId = in.readInt();
            String EntityName = in.readString();
            String Note = in.readString();
            long StartTime = in.readLong();
            long EndTime = in.readLong();
            int amountOfBiometricTests = in.readInt();
            return new EntityScheduleOfZones(RecId,
                    ZoneId,
                    AppointmentId,
                    AppointmentTypeId,
                    DeviceId,
                    OffenderId,
                    EntityTypeId,
                    EntityName,
                    Note,
                    StartTime,
                    EndTime,
                    amountOfBiometricTests);
        }

        @Override
        public EntityScheduleOfZones[] newArray(int size) {
            return new EntityScheduleOfZones[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(RecId);
        dest.writeInt(ZoneId);
        dest.writeInt(AppointmentId);
        dest.writeInt(AppointmentTypeId);
        dest.writeInt(DeviceId);
        dest.writeInt(OffenderId);
        dest.writeInt(EntityTypeId);
        dest.writeString(EntityName);
        dest.writeString(Note);
        dest.writeLong(StartTime);
        dest.writeLong(EndTime);
        dest.writeInt(amountOfBiometricTests);
    }
}
