package com.supercom.puretrack.data.source.remote.parsers;

import android.util.Log;

import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkStateType;
import com.supercom.puretrack.model.database.entities.EntityGpsPoint;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetLbsLocationListener;
import com.supercom.puretrack.data.source.remote.ViewUpdateListener;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class GetLbsLocationResultParser implements GetLbsLocationListener {
    public static String TAG = "LbsLocationReqHandler";

    private final ViewUpdateListener updateActivityListener;

    public GetLbsLocationResultParser(ViewUpdateListener updateActivityListener) {
        this.updateActivityListener = updateActivityListener;
    }

    @Override
    public void handleResponse(String response) {
        long Time;
        double Lat, Lon, Alt;
        float Accuracy;

        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");
            NetworkRepository.getInstance().sendNewEventArray();
            return;
        }

        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.SEND_LOCATION_FINISH);

        String afterDecode;

        afterDecode = response.replace("\\n", "\n");
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");


        try {
            JSONObject JsonGetLbsLocation = new JSONObject(afterDecode);
            JSONObject JsonGetLbsLocationIn = JsonGetLbsLocation.getJSONObject("GetLbsLocationResult");
            String status = JsonGetLbsLocationIn.getString("status");
            String error = JsonGetLbsLocationIn.getString("error");

            if (!JsonGetLbsLocationIn.isNull("data")) {
                JSONObject JsonGetLbsLocationInData = JsonGetLbsLocationIn.getJSONObject("data");

                if (Integer.parseInt(status) == 0) {
                    if (JsonGetLbsLocationInData.length() > 0) {
                        Time = JsonGetLbsLocationInData.getLong("UtcTime");
                        Lat = JsonGetLbsLocationInData.getDouble("Lat");
                        Lon = JsonGetLbsLocationInData.getDouble("Lon");
                        Alt = JsonGetLbsLocationInData.getDouble("Alt");
                        Accuracy = (float) JsonGetLbsLocationInData.getDouble("Accuracy");
                        // insert location to DB
                        //int timeZoneId = sharedInstance().getIntValByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_TIME_ZONE);
                        updateActivityListener.addLbsLocation(new EntityGpsPoint(System.currentTimeMillis(), Lat, Lon, Alt, Accuracy/*, timeZoneId*/));
                    }
                }
            }
        } catch (JSONException e) {
            Log.i(TAG, "\n\n" + "Error in " + TAG);
            LoggingUtil.updateNetworkLog(TAG + "\n\n" + "Error in " + TAG, true);
            String messageToUpload = "Error in " + TAG;
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                    DebugInfoModuleId.Exceptions.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

        }

        // continue with "send locations"
        NetworkRepository.getInstance().sendNewGpsPoints();

    }

}
