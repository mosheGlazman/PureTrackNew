package com.supercom.puretrack.data.broadcast_receiver;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableZonesManager;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.concurrent.TimeUnit;

public class UpdateUIReceiver extends BaseAlarmManagerBroadcastReciever {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent mainActivityIntent = new Intent(MainActivity.MAIN_RECEIVER_EXTRA);
        mainActivityIntent.putExtra(MainActivity.MAIN_RECEIVER_EXTRA, MainActivity.UPDATE_UI_RECEIVER_EXTRA);
        LocalBroadcastManager.getInstance(context).sendBroadcast(mainActivityIntent);

        EntityGpsPoint offenderLastGpsPoint = TableOffenderStatusManager.sharedInstance().getOffenderLastGpsPoint();
        double lat = 0;
        double lon = 0;
        if (offenderLastGpsPoint != null) {
            TableZonesManager.sharedInstance().checkZoneIntersection(offenderLastGpsPoint);
            lat = offenderLastGpsPoint.latitude;
            lon = offenderLastGpsPoint.longitude;
        }

        String messageToUpload = "UPDATE_UI_RECEIVER_EXTRA, last gps point: lat: " + lat + " lon: " + lon;
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Receivers.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        TableZonesManager.sharedInstance().checkBeaconZoneStatus();

        registerForNextClosestTimeAppointment();
    }

    public void registerForNextClosestTimeAppointment() {
        String messageToUpload;
        long closetTimeToStartAlarmManager = DatabaseAccess.getInstance().tableScheduleOfZones.getClosetTimeOfAppointmentIncludesGraceTime
                (TableZonesManager.SCHEDULE_OF_ZONE_TYPE_ALL_EXCEPT_BIO, true);
        if (closetTimeToStartAlarmManager != -1) {

            setOnetimeTimer(App.getContext(), closetTimeToStartAlarmManager, UpdateUIReceiver.class, 9);

            messageToUpload = "Next future UI Scheudle : " + TimeUtil.GetTimeString(((long) closetTimeToStartAlarmManager), TimeUtil.SIMPLE);
        } else {
            messageToUpload = "No future UI Scheudle";
        }
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

}
