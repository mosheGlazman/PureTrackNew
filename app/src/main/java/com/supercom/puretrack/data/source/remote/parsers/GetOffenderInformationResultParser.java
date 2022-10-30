package com.supercom.puretrack.data.source.remote.parsers;


import android.util.Log;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetOffenderInformationListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkRequestName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetOffenderInformationResultParser implements GetOffenderInformationListener {

    private final ViewUpdateListener viewUpdateListener;

    public GetOffenderInformationResultParser(ViewUpdateListener updateUiListener) {
        this.viewUpdateListener = updateUiListener;
    }

    @Override
    public void handleResponse(String response) {
        Log.i("bug70","GetOffenderInformationResultParser handleResponse");
        if (response == null) {
            NetworkRepository.getInstance().checkForPostOnDemandPhoto(NetworkRequestName.RegularFlow);
            return;
        }

        JSONObject jObjectGetGetOffenderInformationResult;
        String afterDecode;

        int status;
        JSONArray jArrayData;
        String dError;
        boolean offenderAtHome;


        afterDecode = response;
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");

        try {
            jObjectGetGetOffenderInformationResult = new JSONObject(afterDecode);
            jObjectGetGetOffenderInformationResult = jObjectGetGetOffenderInformationResult.getJSONObject("GetOffenderInformationResult");

            // Parsing Stage 1
            status 	= jObjectGetGetOffenderInformationResult.getInt("status");
            jArrayData = jObjectGetGetOffenderInformationResult.getJSONArray("data");

            if (status == 0) {
                // Parsing Stage 2
                dError = jArrayData.getJSONObject(0).getString("error");

                if ((dError.contains("Tag not found")) || (dError.contains("SQL error."))) {
                    offenderAtHome = false;
                } else {
                    try {
                        offenderAtHome = jArrayData.getJSONObject(0).getBoolean("Is_In_Home"); // Offender is now at home (home unit reported)
                    } catch (Exception exp) {
                        offenderAtHome = false;
                    }
                }
                if(viewUpdateListener!= null) {
                    viewUpdateListener.onOffenderAtHomeStatusChanged(offenderAtHome);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("bug70","GetOffenderInformationResultParser ERROR",e);
        }
        NetworkRepository.getInstance().checkForPostOnDemandPhoto(NetworkRequestName.RegularFlow);
    }

}
