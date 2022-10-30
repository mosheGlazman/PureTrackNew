package com.supercom.puretrack.data.source.remote.parsers;


import static com.supercom.puretrack.util.constants.database_defaults.DefaultOffenderStatusValues.DEFAULT_START_NETWORK_STATUS_COUNTER;

import android.util.Log;

import com.supercom.puretrack.data.source.local.local_managers.hardware.DeviceJammingManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.CURRENT_COMM_NETWORK_FAILURE_RESET_STATE;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager.OFFENDER_STATUS_CONS;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.remote.requests_listeners.GetAuthenticationTokenListener;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkRequestName;
import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkStateType;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.constants.OffenderActivation;
import com.supercom.puretrack.util.general.LoggingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class GetAuthenticationTokenResultParser implements GetAuthenticationTokenListener {
    public static final String TAG = "AuthTokenReqHandler";

    @Override
    public void handleResponse(String response) {
        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.RECEIVE_AUTHENTICATION);

        JSONObject jObjectGetAuthenticationTokenResult;
        String afterDecode;
        int status = -1;
        String token;

        if (response == null || response.isEmpty()) {
            onAuthenticationError();
        } else {
            handleSucceededToConnectServer();
        }

        afterDecode = response;
        afterDecode = afterDecode.replace("\\", "");
        afterDecode = afterDecode.replace("\"{", "{");
        afterDecode = afterDecode.replace("}\"", "}");

        try {
            jObjectGetAuthenticationTokenResult = new JSONObject(afterDecode);
            jObjectGetAuthenticationTokenResult = jObjectGetAuthenticationTokenResult.getJSONObject("GetAuthenticationTokenResult");

            status = jObjectGetAuthenticationTokenResult.getInt("status");
            if (status != 0) {
                onAuthenticationError();
                return;
            }
            JSONObject data = jObjectGetAuthenticationTokenResult.getJSONObject("data");
            token = data.getString("Token");

            if (token.equals(""))
                onAuthenticationError();
            else {
                onAuthenticationSuccess(token);
            }

        } catch (JSONException e) {
            if (status == 0)
                onAuthenticationError();
            else
                onAuthenticationError();
        }
    }

    private void onAuthenticationError() {
        DeviceJammingManager.getInstance().handleJamming(false);
        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.IDLE_STAGE);
    }

    private void onAuthenticationSuccess(String token) {
        DeviceJammingManager.getInstance().handleJamming(true);
        NetworkRepository.getInstance().setTOKEN_KEY(token);
        NetworkRepositoryConstants.setCurrentCommunicationState(NetworkStateType.AUTHENTICATION_TOKEN_IS_OK);


        int offenderActivateStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_ACTIVATE_STATUS);

        if (offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_UNALLOCATED) {
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_DID_OFFENDER_GET_VALID_AUTHENTICATION_FOR_FIRST_TIME, 1);
            NetworkRepository.getInstance().getOffenderRequest(true);
        } else if (offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_PENDING_ENROLMENT
                || offenderActivateStatus == OffenderActivation.OFFENDER_STATUS_PENDING_ACTIVATION_ENROLLMENT) {
            NetworkRepository.getInstance().sendNewEventArray(NetworkRequestName.DebugInfo);
            return;
        }
        // Go for the full Network Cycle
        boolean isScheudleCycle = DatabaseAccess.getInstance().tableOffStatus.shouldStartScheduleCycle();
        NetworkRepository.getInstance().setIsInScheduleCycle(isScheudleCycle);

        if (!isScheudleCycle) {
            NetworkRepository.getInstance().continueCycleAfterAuth();
            return;
        }
        Log.i(TAG, "------ special schedule cycle ------");
        LoggingUtil.updateNetworkLog(TAG + "------ special schedule cycle ------", false);
        String messageToUpload = "Special schedule cycle";
        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Zones.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);

       int getCurrentZoneVersion = DatabaseAccess.getInstance().tableOffStatus.Get().OffZoneVersion;
        NetworkRepository.getInstance().httpGetOffenderZones(String.valueOf(getCurrentZoneVersion));
    }


    private void handleSucceededToConnectServer() {
        int startNetworkTestCounter = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_START_NETWORK_STATUS_COUNTER);
        if (startNetworkTestCounter != DEFAULT_START_NETWORK_STATUS_COUNTER) {
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_START_NETWORK_STATUS_COUNTER,
                    DEFAULT_START_NETWORK_STATUS_COUNTER);
        }

        int currentCommNetworkFailureStatus = TableOffenderStatusManager.sharedInstance().getIntValueByColumnName(OFFENDER_STATUS_CONS.OFF_CURRENT_COMM_NETWORK_TEST_STATUS);
        if (currentCommNetworkFailureStatus != CURRENT_COMM_NETWORK_FAILURE_RESET_STATE.NORMAL) {
            TableOffenderStatusManager.sharedInstance().updateColumnInt(OFFENDER_STATUS_CONS.OFF_CURRENT_COMM_NETWORK_TEST_STATUS,
                    CURRENT_COMM_NETWORK_FAILURE_RESET_STATE.NORMAL);
        }
    }

}
