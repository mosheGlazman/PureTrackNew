package com.supercom.puretrack.data.source.remote.parsers;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetDateTimeListener;
import com.supercom.puretrack.util.date.TimezoneUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class GetDateTimeListenerResultParser implements GetDateTimeListener {


    public static final String TAG = "DateTimeReqHandler";

    @Override
    public void handleResponse(String response) {
        if (response == null || response.isEmpty()) {
            NetworkRepository.getInstance().handleErrorDuringCycle(TAG + " " + "response is empty!");
            NetworkRepository.getInstance().getOffenderInformation();
//            NetworkCycleManager.getInstance().checkForPostOnDemandPhoto(RegularFlow);
            return;
        }

        String afterDecode;

        afterDecode = response.replace("\\n", "\n");
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");
        afterDecode = afterDecode.replace("\"[", "[");
        afterDecode = afterDecode.replace("]\"", "]");
        afterDecode = afterDecode.replace("\"/Date(", "");
        afterDecode = afterDecode.replace(")/\"", "");

        try {
            JSONObject JsonDateTime = new JSONObject(afterDecode);
            JSONObject JsonDateTimeIn = JsonDateTime.getJSONObject("GetDateTimeResult");

            int timeZoneId = TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.DEVICE_CONFIG_TIME_ZONE);
            String timeZone = new TimezoneUtil().getTimeZone(timeZoneId);
            if (timeZone != null) {
                KnoxUtil.getInstance().getKnoxSDKImplementation().setTimeZone(timeZone);
            }

            long time = JsonDateTimeIn.getLong("data");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            KnoxUtil.getInstance().getKnoxSDKImplementation().setDateTime(calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));
        } catch (JSONException e) {
            String error = ((App) App.getContext()).printStuckTraceToFile(e, false);
            NetworkRepository.getInstance().handleErrorDuringCycle(error);

            App.writeToNetworkLogsAndDebugInfo(TAG, "Error in " + TAG, DebugInfoModuleId.Exceptions);
        }
        NetworkRepository.getInstance().getOffenderInformation();
//        NetworkCycleManager.getInstance().checkForPostOnDemandPhoto(RegularFlow);
    }

}
