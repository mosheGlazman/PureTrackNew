package com.supercom.puretrack.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.util.date.DateFormatterUtil;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.List;

public class AppointmentScheduleAdapter extends ArrayAdapter<EntityScheduleOfZones> {
    private final Context context;
    private final int resource;
    private final List<EntityScheduleOfZones> recordScheduleOfZonesList;
    private final long timeOfStartDay;
    private final long timeOfEndDay;

    public AppointmentScheduleAdapter(Context context, int resource, List<EntityScheduleOfZones> recordScheduleOfZonesList, long startDayTime, long endDayTime) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.recordScheduleOfZonesList = recordScheduleOfZonesList;
        this.timeOfStartDay = startDayTime;
        this.timeOfEndDay = endDayTime;
    }

    public int getCount() {
        return recordScheduleOfZonesList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(resource, null);
        }
        EntityScheduleOfZones recordScheduleOfZonesItem = recordScheduleOfZonesList.get(position);
        TextView zoneMessageText = convertView.findViewById(R.id.zoneMessageText);
        zoneMessageText.setText(context.getString(getZoneDescription(recordScheduleOfZonesItem.AppointmentTypeId)));
        zoneMessageText.setText(zoneMessageText.getText().toString() + " " + DatabaseAccess.getInstance().tableZones.getZoneInfoToShow(recordScheduleOfZonesItem));


        TextView scheduleStartTimeText = convertView.findViewById(R.id.startTime);
        TextView scheduleEndTimeText = convertView.findViewById(R.id.endTime);

        int currentTimeFormatByDeviceSettings = TimeUtil.getCurrentTimeFormatByDeviceSettings(context);
        if (currentTimeFormatByDeviceSettings == 12) { // am pm
            if (timeOfStartDay > recordScheduleOfZonesItem.StartTime) {
                scheduleStartTimeText.setText(TimeUtil.getTimeInAmPm(timeOfStartDay) + "am");
            } else {
                scheduleStartTimeText.setText(TimeUtil.getTimeInAmPm(recordScheduleOfZonesItem.StartTime) + TimeUtil.getAmOrPmLowerCaseString(recordScheduleOfZonesItem.StartTime));
            }
            if (timeOfEndDay < recordScheduleOfZonesItem.EndTime) {
                scheduleEndTimeText.setText(TimeUtil.getTimeInAmPm(timeOfEndDay - 1) + "pm");
            } else {
                scheduleEndTimeText.setText(TimeUtil.getTimeInAmPm(recordScheduleOfZonesItem.EndTime) + TimeUtil.getAmOrPmLowerCaseString(recordScheduleOfZonesItem.EndTime));
            }
        } else {
            if (timeOfStartDay > recordScheduleOfZonesItem.StartTime) {
                scheduleStartTimeText.setText(TimeUtil.formatFromMiliSecondToString(timeOfStartDay, DateFormatterUtil.HM));
            } else {
                scheduleStartTimeText.setText(TimeUtil.formatFromMiliSecondToString(recordScheduleOfZonesItem.StartTime, DateFormatterUtil.HM));
            }
            if (timeOfEndDay < recordScheduleOfZonesItem.EndTime) {
                scheduleEndTimeText.setText(TimeUtil.formatFromMiliSecondToString(timeOfEndDay, DateFormatterUtil.KM));
            } else {
                scheduleEndTimeText.setText(TimeUtil.formatFromMiliSecondToString(recordScheduleOfZonesItem.EndTime, DateFormatterUtil.HM));
            }
        }
        return convertView;
    }


    private int getZoneDescription(int zoneType) {
        // MOJ: Need to add inspection
        switch (zoneType) {
            case TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBI:
                return R.string.schedule_text_zone_must_be_in;

            case TableZonesManager.SCHEDULE_OF_ZONE_TYPE_MBO:
                return R.string.schedule_text_zone_must_be_out;

            case TableZonesManager.SCHEDULE_OF_ZONE_TYPE_CGI:
                return R.string.schedule_text_zone_can_go_in;

            case TableZonesManager.SCHEDULE_OF_ZONE_TYPE_ISP:
                return R.string.schedule_text_inspection;
        }
        return R.string.schedule_text_zone_must_be_in;
    }
}