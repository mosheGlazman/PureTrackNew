package com.supercom.puretrack.ui.call;

import com.supercom.puretrack.model.database.enums.CallState;

public class CallManager {

    private CallRingingView callRingingView;
    private OffHookView offHookView;

    private static final CallManager INSTANCE = new CallManager();

    private CallManager() {

    }

    public static CallManager getInstance() {
        return INSTANCE;
    }

    public void createCallView(CallState callState) {
        createCallView(callState, "");
    }

    public void createCallView(CallState callState, String incomingNumber) {
        switch (callState) {
            case RINGING:
                callRingingView = new CallRingingView(incomingNumber);
                callRingingView.createCallView();
                break;
            case OFF_HOOK:
                offHookView = new OffHookView();
                offHookView.createCallView();
                break;
            case IDLE:
                //in case offender didn't answer the phone we remove ringing view
                if (callRingingView != null) {
                    callRingingView.removeCallView();
                }

                // in case offender answer the phone we remove off hook view
                if (offHookView != null) {
                    offHookView.removeCallView();
                    offHookView.stopDurationTimeTimer();
                }
                break;
        }
    }

}
