package com.supercom.puretrack.data.source.remote.parsers;

import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.OFFENDER_REQUEST_TYPE_TREATED;
import static com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants.REQUEST_RESULT_OK;

import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkStateType;
import com.supercom.puretrack.model.business_logic_models.network.sync_requests.OffenderRequestType;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.repositories.OffenderRequestsRepository;
import com.supercom.puretrack.data.repositories.SyncRequestsRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.PostHandleOffenderRequestListener;
import com.supercom.puretrack.data.source.remote.NetworkResponseListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PostHandleOffenderRequestsResultParser implements PostHandleOffenderRequestListener {
    public static final String TAG = "OffenderRequestsHandlingReqHandler";
    private final ViewUpdateListener updateActivityListener;
    private final NetworkResponseListener networkCycleListener;

    public PostHandleOffenderRequestsResultParser(ViewUpdateListener updateActivityListener, NetworkResponseListener networkCycleListener) {
        this.updateActivityListener = updateActivityListener;
        this.networkCycleListener = networkCycleListener;
    }

    @Override
    public void handleResponse(String response, int requestStatus) {

        String afterDecode;

        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.GET_OFFENDER_REQUEST_RECEIVE);

        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");

            handleOffenderRequestFailed();

            return;
        }

        afterDecode = response.replace("\\n", "\n"); // This change will fix the "ENTER" character
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");

        JSONObject jObjectHandleOffenderRequestResult;
        try {
            jObjectHandleOffenderRequestResult = new JSONObject(afterDecode);
            jObjectHandleOffenderRequestResult = jObjectHandleOffenderRequestResult.getJSONObject("HandleOffenderRequestResult");

            int mainStatus = jObjectHandleOffenderRequestResult.getInt("status");

            //if failed
            if (mainStatus != 0) {

                NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "main status: " + mainStatus);

                handleOffenderRequestFailed();

                return;
            }

            JSONArray JsonHandleRequestData = jObjectHandleOffenderRequestResult.getJSONArray("data");    //RequestsResult->data[]
            int innerStatus = JsonHandleRequestData.getJSONObject(0).getInt("status");

            //if failed
            if (innerStatus != 0) {

                NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "innerStatus: " + innerStatus);

                handleOffenderRequestFailed();
                return;
            }

            handleOffenderRequestSucceeded();

        } catch (JSONException e) {

            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);

            handleOffenderRequestFailed();

        }

    }

    private void handleOffenderRequestSucceeded() throws JSONException {

        int lastOffenderReuqestIdTreated = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                (OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_ID_TREATED);

        boolean shouldDeleteFromFailedRequests = TableOffenderStatusManager.sharedInstance().isHandleRequestExistsInFailedRequests(lastOffenderReuqestIdTreated);

        //old handle request that succeeded
        if (shouldDeleteFromFailedRequests) {
            TableOffenderStatusManager.sharedInstance().removeFailedHandleRequestsFromList(lastOffenderReuqestIdTreated);

            networkCycleListener.onOldHandleRequestResponseSucceeded();
        }

        //new handle request that succeeded
        else {
            SyncRequestsRepository syncRequestsRepository = SyncRequestsRepository.getInstance();
            int lastOffenderRequestStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName
                    (OFFENDER_STATUS_CONS.OFF_LAST_OFFENDER_REQUEST_STATUS);
            if (lastOffenderRequestStatus == NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS && syncRequestsRepository.getSingleReqToTreat() != null) {
                syncRequestsRepository.updateSingleSyncReqResultAndContinue(syncRequestsRepository.getSingleReqToTreat()
                        .requestDataType, NetworkRepositoryConstants.REQUEST_RESULT_IN_PROGRESS);
            } else {

                if (lastOffenderRequestStatus == REQUEST_RESULT_OK) {

                    if (OFFENDER_REQUEST_TYPE_TREATED == OffenderRequestType.ACTIVATE) {
                        updateActivityListener.onHandleResponseSucceeded(OffenderRequestType.ACTIVATE);
                    }
                }

                OffenderRequestsRepository.getInstance().handleOffenderRequestArray();
            }
        }

    }

    private void handleOffenderRequestFailed() {

        networkCycleListener.onHandleRequestResponseFailed();

    }
}
