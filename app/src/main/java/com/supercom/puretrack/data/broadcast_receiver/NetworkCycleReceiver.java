package com.supercom.puretrack.data.broadcast_receiver;

import android.content.Context;
import android.content.Intent;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.constants.network.NetworkRepositoryConstants;
import com.supercom.puretrack.util.general.LoggingUtil;
import com.supercom.puretrack.util.date.TimeUtil;

public class NetworkCycleReceiver extends BaseAlarmManagerBroadcastReciever {

    private static final String NETWORK_CYCLE_RECEIVER_CONST = "networkCycleReceiverConst";

    public static String FIVE_SECONDS_TO_START_CYCLE_CONST = "fiveSecondsToStartCycleConst";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String stringExtra = intent.getStringExtra(NETWORK_CYCLE_RECEIVER_CONST);
        if (stringExtra != null) {
            if (stringExtra.equals(FIVE_SECONDS_TO_START_CYCLE_CONST)) {
                LoggingUtil.updateNetworkLog("\nCalling 'start new cycle' - onReceive START_CYCLE_CONST, state = " + NetworkRepositoryConstants.getCurrentCommunicationState() + "\n", false);
//                NetworkRepository.getInstance().handleStartCycleReceiver();
            }
        }
    }


    @Override
    protected void writeToLogs(long triggerlMillis, int requestAlaramCode) {
        String timeToWakeUp = TimeUtil.GetTimeString(triggerlMillis, TimeUtil.SIMPLE);
        String messageToUpload = "";
        if (requestAlaramCode == 10) {
            messageToUpload = "Next network cycle minus 5 seconds: " + timeToWakeUp;
        } else if (requestAlaramCode == 11) {
            messageToUpload = "Next real network cycle: " + timeToWakeUp;
        }
        App.writeToNetworkLogsAndDebugInfo(getClass().getSimpleName(), messageToUpload, DebugInfoModuleId.Network);
    }


    @Override
    protected Intent getIntent(Context context, Class<?> callbackReceiverClass, String type, String action) {
        Intent intent = super.getIntent(context, callbackReceiverClass, type, action);
        intent.putExtra(NETWORK_CYCLE_RECEIVER_CONST, type);
        return intent;
    }

}
