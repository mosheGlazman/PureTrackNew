package com.supercom.puretrack.data.source.remote.parsers;

import android.util.Log;

import com.supercom.puretrack.model.business_logic_models.network.sync_requests.SyncType;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.repositories.SyncRequestsRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetOffenderZonesListener;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class GetOffenderZonesResultParser implements GetOffenderZonesListener {
    public static String TAG = "OffenderZonesReqHandler";
    private final String zoneVersionFromServer;

    public GetOffenderZonesResultParser(String zoneVersionFromServer) {
        this.zoneVersionFromServer = zoneVersionFromServer;
    }

    @Override
    public void handleResponse(String response) {

        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");

            NetworkRepository.getInstance().httpTerminateToken();
            return;
        }

        JSONObject jObjectGetOffenderZonesResult = null;

        int status;
        String error;
        JSONArray jArrayZonesResultData;


        String afterDecode;
        afterDecode = response;
        afterDecode = response.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");


        try {
            jObjectGetOffenderZonesResult = new JSONObject(afterDecode);
            jObjectGetOffenderZonesResult = jObjectGetOffenderZonesResult.getJSONObject("GetOffenderZonesResult");
            status = jObjectGetOffenderZonesResult.getInt("status");
            error = jObjectGetOffenderZonesResult.getString("error");
            jArrayZonesResultData = jObjectGetOffenderZonesResult.getJSONArray("data");

            SyncRequestsRepository.getInstance().treatZonesSync(jArrayZonesResultData, Integer.parseInt(zoneVersionFromServer));
        } catch (JSONException e) {
            String exception = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + "" + exception);

            Log.i(TAG, "\n\n" + "Error in main zone response -  GetOffenderZonesResult ");
            LoggingUtil.updateNetworkLog(TAG + "\n\n" + "Error in main zone response -  GetOffenderZonesResult ", true);
            String messageToUpload = "Error in " + TAG;
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
            SyncRequestsRepository.getInstance().updateSingleSyncReqResultAndContinue(SyncType.ZONES, NetworkRepositoryConstants.REQUEST_RESULT_ERR);
        }
    }
}
