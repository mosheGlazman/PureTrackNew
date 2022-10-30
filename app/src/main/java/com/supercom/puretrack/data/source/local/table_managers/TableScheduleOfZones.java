package com.supercom.puretrack.data.source.local.table_managers;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.List;

public class TableScheduleOfZones {
    public static final String TAG = "DBScheduleOfZones";

    private static final TableScheduleOfZones INSTANCE = new TableScheduleOfZones();

    public TableScheduleOfZones() {
    }

    public static TableScheduleOfZones sharedInstance() {
        return INSTANCE;
    }

    public List<EntityScheduleOfZones> getDayScheduleWhereUserMustBeIn(int startTimeOffset, int endTimeOffset) {
        return DatabaseAccess.getInstance().tableScheduleOfZones.getDayScheduleWhereUserMustBeIn(TimeUtil.GetTimeWithOffset(startTimeOffset), TimeUtil.GetTimeWithOffset(endTimeOffset));
    }
}
