package com.supercom.puretrack.data.source.remote.parsers;

import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkRequestName;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkStateType;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.entities.EntityEventLog;
import com.supercom.puretrack.model.database.entities.EntityOpenEventLog;
import com.supercom.puretrack.data.source.local.table.TableEventConfig.EventTypes;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.InsertDeviceEventsListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InsertDeviceEventsResultParser implements InsertDeviceEventsListener {
    public final static int MIN_RECORDS_TO_REPEAT_EVENT_LOG_REQUEST = 3;

    private final String TAG = "EventsUploadReqHandler";

    private final ViewUpdateListener updateActivityListener;

    private final NetworkRequestName nextRequestToSend;

    public InsertDeviceEventsResultParser(ViewUpdateListener updateActivityListener, NetworkRequestName nextRequestToSend) {
        this.updateActivityListener = updateActivityListener;
        this.nextRequestToSend = nextRequestToSend;
    }

    @Override
    public void handleResponse(String response) {
        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");

            NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.SEND_EVENTS_FINISH);
            NetworkRepository.getInstance().continueToNextRequest(nextRequestToSend);
            return;
        }


        int eventRowId;
        int insertStatus;

        String afterDecode;

        afterDecode = response.replace("\\n", "\n");
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");

        try {
            JSONObject JsonInsertOffenderLocations = new JSONObject(afterDecode);
            JSONObject JsonInsertOffenderLocationsIn = JsonInsertOffenderLocations.getJSONObject("InsertDeviceEventsResult");

            int status = JsonInsertOffenderLocationsIn.getInt("status");
            if (updateActivityListener != null && status == EntityOpenEventLog.UploadResponseStatus.RESPONSE_STATUS_OK) {
                updateActivityListener.onEventResposeOkFromServer();
            } else {
                NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "main status: " + status);
            }

            JSONArray JsonInsertOffenderLocationsInData = JsonInsertOffenderLocationsIn.getJSONArray("data");    //RequestsResult->data[]

            for (int i = 0; i < JsonInsertOffenderLocationsInData.length(); i++) {
                eventRowId = JsonInsertOffenderLocationsInData.getJSONObject(i).getInt("DeviceEventID");
                insertStatus = JsonInsertOffenderLocationsInData.getJSONObject(i).getInt("InsertStatus");
                //Clean all that saved on PureMonitor

                EntityEventLog recordByRawId = DatabaseAccess.getInstance().tableEventLog.getEventLogRecByRowId(eventRowId);
                if (recordByRawId != null) {
                    if (insertStatus >= 0/*RecordOpenEventLog.UploadResponseStatus.RESPONSE_STATUS_OK*/) {

                        // check if this events ack is related to text message that was sent
                        DatabaseAccess.getInstance().tableMessages.SetAckMsgByEventID(eventRowId);


                        DatabaseAccess.getInstance().tableEventLog.deleteRowById(eventRowId);

                        if (recordByRawId.EvType == EventTypes.flightModeEnabled) {
                            //we use a flag in order to know in the end of the cycle to enable flight mode
                            NetworkRepository.getInstance().getFlightModeData().setShouldEnableFlightMode(true);
                        }

                    } else {
                        // Increase the RecordSyncRetriesCount of the event record
                        DatabaseAccess.getInstance().tableEventLog.updateRecordSyncRetriesCount(eventRowId);
                        NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response error from event id " + eventRowId);
                    }
                } else {
                    String messageToUpload = "Error in " + TAG + " -> PM Event ensert status: " + insertStatus + "Could not find event record for eventRowId = " + eventRowId;
                    App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.errors);

                    NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + messageToUpload);
                }
            }
        } catch (JSONException e) {
            App.writeToNetworkLogsAndDebugInfo(TAG, "Error in " + TAG, DebugInfoModuleId.Exceptions);

            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);

        }
        NetworkRepository.getInstance().continueToNextRequest(nextRequestToSend);
    }
}
