package com.supercom.puretrack.util.constants.network;

import android.os.PowerManager;
import android.util.Log;

import com.supercom.puretrack.model.business_logic_models.network.network_repository.NetworkStateType;

import java.util.Date;

public class NetworkRepositoryConstants {

    public static PowerManager.WakeLock screenLockX;
    public static int OFFENDER_REQUEST_TYPE_TREATED = 0;
    public static final int REQUEST_RESULT_OK = 0;
    public static final int REQUEST_RESULT_IN_PROGRESS = 1;
    public static final int REQUEST_RESULT_ERR = 3;
    private static int currentCommunicationState = setCurrentCommunicationState(NetworkStateType.IDLE_STAGE);
    public static boolean isGpsProximityViolationOpened = false;
    public static boolean isGpsProximityWarningOpened = false;
    public static final boolean IS_NETWORK_LOG_ON = true;
    public static int TIME_TO_START_CYCLE = 5;
    public static int MAX_GPS_POINTS_PER_CHUNK = 90;
    public static Date lastStartCycleDate;

    public static int getCurrentCommunicationState(){
        return currentCommunicationState;
    }

    public static int setCurrentCommunicationState(int state) {
        Log.i("bug70", "setCurrentCommunicationState(" + state + ")");

        if(state == NetworkStateType.SEND_GET_AUTHENTICATION){
            lastStartCycleDate = new Date();
        }

        currentCommunicationState = state;
        return currentCommunicationState;
    }
}
