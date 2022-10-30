package com.supercom.puretrack.data.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.model.database.enums.CallState;
import com.supercom.puretrack.ui.call.CallManager;

public class OutgoingCallReceiver extends BroadcastReceiver {

    private Context context;
    private final OutgoingPhoneStateListener outgoingPhoneStateListener = new OutgoingPhoneStateListener();
    private int lastState = TelephonyManager.CALL_STATE_IDLE;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {

            this.context = context;

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            tm.listen(createCustomPhoneStateClass(), PhoneStateListener.LISTEN_CALL_STATE);

        }
    }

    protected Context getReceiverContext() {
        return context;
    }

    protected PhoneStateListener createCustomPhoneStateClass() {
        return outgoingPhoneStateListener;
    }

    public class OutgoingPhoneStateListener extends PhoneStateListener {


        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    lastState = TelephonyManager.CALL_STATE_RINGING;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:

                    boolean shouldCreateCustomCallInterface = (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.CUSTOM_CALL_INTERFACE)
                            == 1);
                    if (shouldCreateCustomCallInterface) {
                        CallManager.getInstance().createCallView(CallState.OFF_HOOK);
                    }
                    lastState = TelephonyManager.CALL_STATE_OFFHOOK;


                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {

                        shouldCreateCustomCallInterface = (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.CUSTOM_CALL_INTERFACE)
                                == 1);
                        if (shouldCreateCustomCallInterface) {
                            CallManager.getInstance().createCallView(CallState.IDLE);
                        }

                        TelephonyManager tm1 = (TelephonyManager) getReceiverContext().getSystemService(Context.TELEPHONY_SERVICE);
                        tm1.listen(outgoingPhoneStateListener, PhoneStateListener.LISTEN_NONE);

                    }
                    break;
                default:
                    break;
            }
        }
    }

}
