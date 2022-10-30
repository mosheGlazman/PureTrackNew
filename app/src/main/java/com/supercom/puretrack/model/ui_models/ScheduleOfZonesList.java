package com.supercom.puretrack.model.ui_models;

import android.os.Parcel;
import android.os.Parcelable;

import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;

import java.util.ArrayList;

public class ScheduleOfZonesList implements Parcelable {

    private ArrayList<EntityScheduleOfZones> scheduleOfZonesList;

    public ScheduleOfZonesList(ArrayList<EntityScheduleOfZones> iconsArray) {
        this.scheduleOfZonesList = iconsArray;
    }

    public static final Creator<ScheduleOfZonesList> CREATOR = new Creator<ScheduleOfZonesList>() {
        @Override
        public ScheduleOfZonesList createFromParcel(Parcel in) {
            ArrayList<EntityScheduleOfZones> icons = new ArrayList<EntityScheduleOfZones>();
            in.readTypedList(icons, EntityScheduleOfZones.CREATOR);
            return new ScheduleOfZonesList(icons);
        }

        @Override
        public ScheduleOfZonesList[] newArray(int size) {
            return new ScheduleOfZonesList[size];
        }
    };

    public ArrayList<EntityScheduleOfZones> getScheduleOfZonesList() {
        return scheduleOfZonesList.size() > 0 ? this.scheduleOfZonesList : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(scheduleOfZonesList);
    }
}
