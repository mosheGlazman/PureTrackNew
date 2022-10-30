package com.supercom.puretrack.data.source.remote.parsers;

import android.util.Log;

import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkStateType;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.ViolationCategoryTypes;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.InsertOffenderLocationsListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class InsertOffenderLocationsResultParser implements InsertOffenderLocationsListener {

    public static String TAG = "GpsPointsUploadReqHandler";
    private final ViewUpdateListener updateActivityListener;

    public InsertOffenderLocationsResultParser(ViewUpdateListener updateActivityListener) {
        this.updateActivityListener = updateActivityListener;
    }

    @Override
    public void handleResponse(String response, int eventId, int intEventId) {

        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");
            NetworkRepository.getInstance().sendNewEventArray();
            return;
        }


        int[] RecordGpsPoint_RowId = new int[1000];
        String[] LoccationStatus = new String[1000];

        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.SEND_LOCATION_FINISH);

        String afterDecode;

        afterDecode = response.replace("\\n", "\n");
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");

        try {
            JSONObject JsonInsertOffenderLocations = new JSONObject(afterDecode);
            JSONObject JsonInsertOffenderLocationsIn = JsonInsertOffenderLocations.getJSONObject("InsertOffenderLocationsResult");
            String status = JsonInsertOffenderLocationsIn.getString("status");
            String error = JsonInsertOffenderLocationsIn.getString("error");

            //if (status == 0) {

            JSONArray JsonInsertOffenderLocationsInData = JsonInsertOffenderLocationsIn.getJSONArray("data");    //RequestsResult->data[]

            for (int i = 0; i < JsonInsertOffenderLocationsInData.length(); i++) {
                RecordGpsPoint_RowId[i] = JsonInsertOffenderLocationsInData.getJSONObject(i).getInt("RequestID");
                LoccationStatus[i] = JsonInsertOffenderLocationsInData.getJSONObject(i).getString("status");

                //Clean all that saved on PureMonitor
                if (Integer.parseInt(LoccationStatus[i]) == EntityGpsPoint.UploadResponseStatus.RESPONSE_STATUS_GPS_OK_NOT_PROXIMITY ||
                        Integer.parseInt(LoccationStatus[i]) == EntityGpsPoint.UploadResponseStatus.RESPONSE_STATUS_GPS_OK_PROXIMITY_VIOLATION ||
                        Integer.parseInt(LoccationStatus[i]) == EntityGpsPoint.UploadResponseStatus.RESPONSE_STATUS_GPS_OK_PROXIMITY_WARNING) {
                    DatabaseAccess.getInstance().tableGpsPoint.deleteRowById(RecordGpsPoint_RowId[i]);
                } else // Increase the RecordSyncRetriesCount of the GpsPoint record. (Default MAX_SYNC_RETRY_COUNT = 3)
                {
                    DatabaseAccess.getInstance().tableGpsPoint.updateRecordSyncRetriesCount(RecordGpsPoint_RowId[i]);

                    NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response error from location id " + RecordGpsPoint_RowId[i]);
                }

                /********************************************
                 *
                 * Location Status for PureProtect feature
                 *
                 *********************************************/
                boolean hasOpenGpsProximityViolatonEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.GPS_PROXIMITY_VIOLATION) != -1;
                boolean hasOpenGpsProximityWarningEvent = DatabaseAccess.getInstance().tableOpenEventsLog.getOpenerIdByViolationCategory(ViolationCategoryTypes.GPS_PROXIMITY_WARNING) != -1;
                if (Integer.parseInt(LoccationStatus[i]) == EntityGpsPoint.UploadResponseStatus.RESPONSE_STATUS_GPS_OK_NOT_PROXIMITY) {
                    if (hasOpenGpsProximityViolatonEvent) {
                        NetworkRepositoryConstants.isGpsProximityViolationOpened = false;
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventGpsProximityViolationClose, -1, -1);
                        updateActivityListener.onGpsPointsUploadedFinishedToParse();
                    }
                    if (hasOpenGpsProximityWarningEvent) {
                        NetworkRepositoryConstants.isGpsProximityWarningOpened = false;
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventGpsProximityWarningClose, -1, -1);
                        updateActivityListener.onGpsPointsUploadedFinishedToParse();
                    }

                } else if (Integer.parseInt(LoccationStatus[i]) == EntityGpsPoint.UploadResponseStatus.RESPONSE_STATUS_GPS_OK_PROXIMITY_VIOLATION) {
                    if (!hasOpenGpsProximityViolatonEvent) {
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventGpsProximityViolationOpen, -1, -1);
                    }
                    if (!NetworkRepositoryConstants.isGpsProximityViolationOpened) {
                        NetworkRepositoryConstants.isGpsProximityViolationOpened = true;
                        updateActivityListener.onGpsPointsUploadedFinishedToParse();
                    }
                } else if (Integer.parseInt(LoccationStatus[i]) == EntityGpsPoint.UploadResponseStatus.RESPONSE_STATUS_GPS_OK_PROXIMITY_WARNING) {
                    if (hasOpenGpsProximityViolatonEvent) {
                        NetworkRepositoryConstants.isGpsProximityViolationOpened = false;
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventGpsProximityViolationClose, -1, -1);
                    }

                    if (!hasOpenGpsProximityWarningEvent) {
                        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.eventGpsProximityWarningOpen, -1, -1);
                    }
                    if (!NetworkRepositoryConstants.isGpsProximityWarningOpened) {
                        NetworkRepositoryConstants.isGpsProximityWarningOpened = true;
                        updateActivityListener.onGpsPointsUploadedFinishedToParse();
                    }
                }
            }
        } catch (JSONException e) {
            Log.i(TAG, "\n\n" + "Error in " + TAG);
            LoggingUtil.updateNetworkLog(TAG + "\n\n" + "Error in " + TAG, true);
            String messageToUpload = "Error in " + TAG;
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);
        }

        NetworkRepository.getInstance().sendNewEventArray();


    }

}
