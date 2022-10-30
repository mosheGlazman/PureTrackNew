package com.supercom.puretrack.data.source.remote.parsers;

import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkStateType;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.repositories.OffenderRequestsRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetOffenderRequestListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetOffenderRequestsResultParser implements GetOffenderRequestListener {
    public final static String TAG = "OffenderReqHandler";
    private final ViewUpdateListener updateActivityListener;

    public GetOffenderRequestsResultParser(ViewUpdateListener updateActivityListener) {
        this.updateActivityListener = updateActivityListener;
    }

    @Override
    public void handleResponse(String response) {
        String afterDecode;

        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.GET_OFFENDER_REQUEST_RECEIVE);

        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");

            NetworkRepository.getInstance().httpTerminateToken();
            return;
        }

        afterDecode = response.replace("\\n", "\n"); // This change will fix the "ENTER" character
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");

        try {
            JSONObject JsonGetOffenderRequestsResult = new JSONObject(afterDecode);
            JSONObject JsonGetOffenderRequestsResultIn = JsonGetOffenderRequestsResult.getJSONObject("GetOffenderRequestsResult");
            String error = JsonGetOffenderRequestsResultIn.getString("error");

            if (error != null && !error.equals("")) {

                NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + error);
                NetworkRepository.getInstance().httpTerminateToken();
                return;
            }


            JSONArray JsonGetOffenderRequestsResultInDataArray = JsonGetOffenderRequestsResultIn.getJSONArray("data");    //RequestsResult->data[]

            OffenderRequestsRepository.getInstance().setOffenderRequestsResultJsonDataArray(JsonGetOffenderRequestsResultInDataArray);
            OffenderRequestsRepository.getInstance().setViewUpdateListener(updateActivityListener);
            OffenderRequestsRepository.getInstance().handleOffenderRequestArray();
        } catch (JSONException e) {

            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);

            NetworkRepository.getInstance().handleOffenderRequestError();
        }
    }
}
