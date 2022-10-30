package com.supercom.puretrack.data.source.remote.parsers;

import android.util.Log;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.InsertDeviceCommunicationsListener;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class InsertDeviceCommunictionsResultParser implements InsertDeviceCommunicationsListener {

    public static final String TAG = "InsertDevCommReqHandler";

    @Override
    public void handleResponse(String response, int eventId, int intEventId) {
        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");
            NetworkRepository.getInstance().getOffenderRequest(true);
            return;
        }

        int idItem;
        String errorItem;

        String afterDecode;

        afterDecode = response.replace("\\n", "\n");
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");

        try {
            JSONObject JsonInsertOffenderLocations = new JSONObject(afterDecode);
            JSONObject JsonInsertOffenderLocationsIn = JsonInsertOffenderLocations.getJSONObject("InsertDeviceCommunicationsResult");

            JSONArray JsonInsertOffenderLocationsInData = JsonInsertOffenderLocationsIn.getJSONArray("data");
            for (int i = 0; i < JsonInsertOffenderLocationsInData.length(); i++) {
                idItem = JsonInsertOffenderLocationsInData.getJSONObject(i).getInt("ItemId");
                errorItem = JsonInsertOffenderLocationsInData.getJSONObject(i).getString("Error");

                //Clean all that saved on PureMonitor
                if (errorItem.isEmpty()) {
                    DatabaseAccess.getInstance().tableCallLog.deleteRowById(idItem);
                } else {
                    NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + errorItem);
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

        NetworkRepository.getInstance().sendInsertDeviceDebugInfo();
    }

}
