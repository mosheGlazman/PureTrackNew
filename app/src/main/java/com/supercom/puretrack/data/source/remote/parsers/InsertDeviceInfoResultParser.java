package com.supercom.puretrack.data.source.remote.parsers;

import android.util.Log;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableDeviceInfoManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.InsertDeviceInfoListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class InsertDeviceInfoResultParser implements InsertDeviceInfoListener {

    public static final String TAG = "InsertDevInfoReqHandler";
    private final ViewUpdateListener updateActivityListener;

    public InsertDeviceInfoResultParser(ViewUpdateListener updateActivityListener) {
        this.updateActivityListener = updateActivityListener;
    }

    @Override
    public void handleResponse(String response) {
        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");
            NetworkRepository.getInstance().getOffenderRequest(true);
            return;
        }

        String afterDecode;

        afterDecode = response.replace("\\n", "\n");
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");

        try {
            JSONObject JsonInsertDeviceDebugInfo = new JSONObject(afterDecode);
            JSONObject JsonInsertDeviceDebugInfoResult = JsonInsertDeviceDebugInfo.getJSONObject("InsertDeviceInfoResult");
            int status = JsonInsertDeviceDebugInfoResult.getInt("status");

            //succeeded
            if (status == 0) {
                TableDeviceInfoManager.sharedInstance().clearAllTablesRelatedToDeviceInfoContract();
            } else //failed
            {
                if (TableDeviceInfoManager.sharedInstance().isReachedMaximumAttempts()) {
                    TableDeviceInfoManager.sharedInstance().clearAllTablesRelatedToDeviceInfoContract();
                } else {
                    TableDeviceInfoManager.sharedInstance().increaseAttemptsCounter();
                }

                NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "main status: " + status);
            }
            NetworkRepository.getInstance().getOffenderRequest(true);
        } catch (JSONException e) {
            Log.i(TAG, "\n\n" + "Error in " + TAG);
            LoggingUtil.updateNetworkLog(TAG + "\n\n" + "Error in " + TAG, true);
            String messageToUpload = "Error in " + TAG;
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

            NetworkRepository.getInstance().getOffenderRequest(true);

            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);
        }
    }

}
