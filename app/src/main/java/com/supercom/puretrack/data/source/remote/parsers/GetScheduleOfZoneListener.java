package com.supercom.puretrack.data.source.remote.parsers;

import android.util.Log;

import com.supercom.puretrack.model.business_logic_models.network.sync_requests.ZoneResult;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.model.database.enums.EnumDatabaseTables;
import com.supercom.puretrack.model.database.entities.EntityScheduleOfZones;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableZones;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.repositories.SyncRequestsRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetOffenderScheduleOfZoneListener;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.date.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class GetScheduleOfZoneListener implements GetOffenderScheduleOfZoneListener {

    public static final String TAG = "SchedOfZoneReqHandler";
    private int AppointmentId;

    @Override
    public void handleResponse(String response, int zoneId, int newScheduleVersion) {
        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");

            NetworkRepository.getInstance().httpTerminateToken();
            return;
        }

        JSONObject jObjectGetOffenderScheduleOfZoneResult = null;

        int status;
        String error;
        JSONArray jArraySchduleOfZonesResultData;

        int AppointmentTypeId;        // null
        String AppointmentTypeString;        // "Can Go In"
        int DeviceId;                // 148
        String EntityName = null;                // null
        int OffenderId;                // 1008
        long StartDateLong;
        String StartDateString;
        long EndDateLong;
        String EndDateString;
        String Note;                    // ""
        int AmountOfBiometircTests;    // 0
        int HeaderHexColorCode;    // #35A84D
        int BodyHexColorCode;        // #D7EEDB
        boolean RecurrenceEnabled;        // true or false
        int EntityTypeId;            // 8
        int amountOfBiometricTest;


        String afterDecode;
        afterDecode = response;
        afterDecode = response.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");


        try {
            jObjectGetOffenderScheduleOfZoneResult = new JSONObject(afterDecode);
            jObjectGetOffenderScheduleOfZoneResult = jObjectGetOffenderScheduleOfZoneResult.getJSONObject("GetOffenderScheduleOfZoneResult");
            status = jObjectGetOffenderScheduleOfZoneResult.getInt("status");
            error = jObjectGetOffenderScheduleOfZoneResult.getString("error");
            jArraySchduleOfZonesResultData = jObjectGetOffenderScheduleOfZoneResult.getJSONArray("data");

            DatabaseAccess.getInstance().tableScheduleOfZones.DeleteOffenderScheduleOfOneZone(zoneId);

            long finalResult = NetworkRepositoryConstants.REQUEST_RESULT_OK;
            for (int i = 0; i < jArraySchduleOfZonesResultData.length(); i++) {
                AppointmentId = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("AppointmentId");                    // 35297
                AppointmentTypeId = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("AppointmentTypeId");            // null
                AppointmentTypeString = jArraySchduleOfZonesResultData.getJSONObject(i).getString("AppointmentType");        // "Can Go In"
                DeviceId = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("DeviceId");                                // 148
//		        EntityName = jArraySchduleOfZonesResultData.getJSONObject(i).getString("EntityName");						// null
                OffenderId = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("OffenderId");                            // 1008
                Note = jArraySchduleOfZonesResultData.getJSONObject(i).getString("Note");                                    // ""
                AmountOfBiometircTests = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("AmountOfBiometircTests");    // 0
                HeaderHexColorCode = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("DeviceId");                    // #35A84D
                BodyHexColorCode = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("DeviceId");                        // #D7EEDB
//		        RecurrenceId = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("RecurrenceId");						// null
//		        ParentRecurrenceId = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("ParentRecurrenceId");			// null
                RecurrenceEnabled = jArraySchduleOfZonesResultData.getJSONObject(i).getBoolean("RecurrenceEnabled");        // true or false
//		        RecurrenceStartDate = jArraySchduleOfZonesResultData.getJSONObject(i).getLong("DeviceId");					// null
                EntityTypeId = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("DeviceId");                            // 8
                StartDateString = jArraySchduleOfZonesResultData.getJSONObject(i).getString("StartDate");
                EndDateString = jArraySchduleOfZonesResultData.getJSONObject(i).getString("EndDate");
                amountOfBiometricTest = jArraySchduleOfZonesResultData.getJSONObject(i).getInt("AmountOfBiometircTests");

                StartDateString = StartDateString.replace("/Date(", "");
                StartDateString = StartDateString.replace(")/", "");
                EndDateString = EndDateString.replace("/Date(", "");
                EndDateString = EndDateString.replace(")/", "");

                StartDateLong = Long.parseLong(StartDateString);// - 3600000*4;// ;
                EndDateLong = Long.parseLong(EndDateString);// - 3600000*4;


                long tmpResult = DatabaseAccess.getInstance().insertNewRecord(EnumDatabaseTables.TABLE_SCHEDULE_OF_ZONES,
                        new EntityScheduleOfZones(
                                AppointmentId, //TODO: [Aivars @ 14.05.2015] : For now saving the AppointmentId value.(What should be saved as a RecId ? Do we need it ?)
                                zoneId,
                                AppointmentId,
                                AppointmentTypeId,
                                DeviceId,
                                OffenderId,
                                EntityTypeId,
                                EntityName,
                                Note,
                                StartDateLong,
                                EndDateLong,
                                amountOfBiometricTest)
                );

                //If one of the schedules fails , meaning the result of all schedules in that zone is failed.
                if (tmpResult == ZoneResult.DB_RESULT_ERR) {
                    finalResult = tmpResult;

                    NetworkRepository.getInstance().handleErrorDuringCycle(TAG + "" + "Error in zone: " + zoneId + " AppointmentId " + AppointmentId);

                    Log.i(TAG, "\n\n" + "Error in zone: " + zoneId + " AppointmentId " + AppointmentId);
                    LoggingUtil.updateNetworkLog("\n\n" + TAG + "\n" + TimeUtil.getCurrentTimeStr() + " : " + "Error in zone: " + zoneId + " AppointmentId " + AppointmentId, false);
                    String messageToUpload = "Error in " + TAG;
                    TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                            DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
                }
            }

            String where = TableZones.COLUMN_ZON_ID + "=" + zoneId;
            DatabaseAccess.getInstance().UpdateField(EnumDatabaseTables.TABLE_ZONES, TableZones.COLUMN_ZON_SCHEDULE_VERSION, newScheduleVersion, where, null);

            SyncRequestsRepository.getInstance().updateScheduleResult_AndContinue(zoneId, finalResult);
        } catch (JSONException e) {

            String exception = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + "" + exception);

            Log.i(TAG, "\n\n" + "Error in zone: " + zoneId + " AppointmentId " + AppointmentId);
            LoggingUtil.updateNetworkLog("\n\n" + TAG + "\n" + TimeUtil.getCurrentTimeStr() + " : " + "Error in zone: " + zoneId + " AppointmentId " + AppointmentId, false);
            String messageToUpload = "Error in zone: " + zoneId + " AppointmentId " + AppointmentId;
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            SyncRequestsRepository.getInstance().updateScheduleResult_AndContinue(zoneId, ZoneResult.DB_RESULT_ERR);//-1 is Err
        }
    }
}
