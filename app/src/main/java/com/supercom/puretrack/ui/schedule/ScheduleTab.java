package com.supercom.puretrack.ui.schedule;


import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.model.ui_models.ScheduleOfZonesList;
import com.supercom.puretrack.ui.adapter.AppointmentScheduleAdapter;
import com.supercom.puretrack.util.date.DateFormatterUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScheduleTab extends Fragment {
    private TextView fullDay;
    private TextView fullDayWithLanguage;
    private ListView scheduleList;
    private AppointmentScheduleAdapter appointmentScheduleAdapter;
    private String day;
    private long startDayTime;
    private long endDayTime;
    private List<EntityScheduleOfZones> recordsScheduleOfZonesList;

    Activity activity;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    public ScheduleTab() {
    }

    public static ScheduleTab newInstance(List<EntityScheduleOfZones> recordsScheduleOfZonesList, String day, long startTime, long endTime) {
        ScheduleTab scheduleTab = new ScheduleTab();
        ScheduleOfZonesList scheduleOfZonesList = new ScheduleOfZonesList((ArrayList<EntityScheduleOfZones>) recordsScheduleOfZonesList);
        Bundle args = new Bundle();
        args.putParcelable("schedule_of_zones_list", scheduleOfZonesList);
        args.putString("day", day);
        args.putLong("start_time", startTime);
        args.putLong("end_time", endTime);
        scheduleTab.setArguments(args);
        return scheduleTab;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScheduleOfZonesList scheduleOfZonesList = getArguments().getParcelable("schedule_of_zones_list");
        recordsScheduleOfZonesList = scheduleOfZonesList.getScheduleOfZonesList();
        day = getArguments().getString("day");
        startDayTime = getArguments().getLong("start_time");
        endDayTime = getArguments().getLong("end_time");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.schedule_tab, container, false);
        fullDay = view.findViewById(R.id.fullDay);
        fullDayWithLanguage = view.findViewById(R.id.fullDayWithLanguage);
        scheduleList = view.findViewById(R.id.scheduleList);
        try {
            SimpleDateFormat format = new SimpleDateFormat(DateFormatterUtil.DMY);
            Date date = format.parse(day);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // start from Sunday=1
            int month = cal.get(Calendar.MONTH);
            fullDayWithLanguage.setText(MainActivity.dayOfWeekArray[dayOfWeek] + ", " + cal.get(Calendar.DATE) + " " + MainActivity.monthArray[month]);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (recordsScheduleOfZonesList != null) {
            appointmentScheduleAdapter = new AppointmentScheduleAdapter(App.getAppContext(), R.layout.appointment_schedule_item, recordsScheduleOfZonesList, startDayTime, endDayTime);
            scheduleList.setAdapter(appointmentScheduleAdapter);
        }
        return view;
    }

    public void updateAppointmentScheduleList(List<EntityScheduleOfZones> recordsScheduleOfZonesList) {
        if (appointmentScheduleAdapter != null) {
            this.recordsScheduleOfZonesList.clear();
            this.recordsScheduleOfZonesList.addAll(recordsScheduleOfZonesList);
            appointmentScheduleAdapter.notifyDataSetChanged();
        }
    }

}
