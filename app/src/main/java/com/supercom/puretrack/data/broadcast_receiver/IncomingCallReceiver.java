package com.supercom.puretrack.data.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.database.enums.CallState;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.ui.call.CallManager;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.hardware.VoiceManager;

public class IncomingCallReceiver extends BroadcastReceiver {

    private Context context;
    public static final String TAG = "IncomingCallReciever";

    private final IncomingPhoneStateListener incomingPhoneStateListener = new IncomingPhoneStateListener();

    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isFirstReceiverForRinging = true; //every call we want ringing mode to get only once
    static int soundActionId=0;
    private static int static_phone_state=TelephonyManager.CALL_STATE_IDLE;
    public static int getLastPhoneState(){
        return static_phone_state;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        try {
            String  e = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.i("Bug2","event:"+e);

            if (e.equals("RINGING")){
                static_phone_state=TelephonyManager.CALL_STATE_RINGING;
            }else if (e.equals("OFFHOOK")){
                static_phone_state=TelephonyManager.CALL_STATE_OFFHOOK;
            }else{
                static_phone_state=TelephonyManager.CALL_STATE_IDLE;
            }

            if (e.equals("RINGING")){
                soundActionId = VoiceManager.getInstance(context).turnOnVolume();
                VoiceManager.getInstance(context).playWav(VoiceManager.e_files.CallReceive);
            }else{
                VoiceManager.getInstance(context).stopWav();
            }

            if (e.equals("IDLE")){
                VoiceManager.getInstance(context).turnOffVolume(soundActionId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {

            ((App) App.getContext()).wakeUpApplicationIfNeeded();

            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            boolean isAllIncomingCallsAllowed = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                    (OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONES_ACTIVE) == 0;
            if (number != null) {
                String event = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if (event.equals("RINGING") && isFirstReceiverForRinging) {
                    isFirstReceiverForRinging = false;
                    boolean isNumInWhiteList = DatabaseAccess.getInstance().tableOffenderDetails.isNumberInWhiteList(number);

                    // checks if number appears on white list or all incoming calls allowed
                    String messageToUpload = "";
                    if (isNumInWhiteList || isAllIncomingCallsAllowed) {
                        messageToUpload = "Incoming call from allowed device " + number;
                        App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Receivers);
                        TelephonyManager telephonyManager = (TelephonyManager) getReceiverContext().getSystemService(Context.TELEPHONY_SERVICE);
                        telephonyManager.listen(createCustomPhoneStateClass(), PhoneStateListener.LISTEN_CALL_STATE);
                    } else {
                        messageToUpload = "Incoming call from not allowed device " + number;
                        App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Receivers);
                        isFirstReceiverForRinging = true;
                    }
                }
            } else {
                String messageToUpload = "Incoming call from empty number - allowed = " + ((isAllIncomingCallsAllowed) ? "yes" : "no");
                App.writeToNetworkLogsAndDebugInfo(TAG, messageToUpload, DebugInfoModuleId.Receivers);
                if (!isAllIncomingCallsAllowed) {

                    isFirstReceiverForRinging = true;
                }
            }
        }
    }

    protected Context getReceiverContext() {
        return context;
    }

    protected PhoneStateListener createCustomPhoneStateClass() {
        return incomingPhoneStateListener;
    }

    public class IncomingPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        allowVideoCallIfInKioskMode();
                        boolean shouldCreateCustomCallInterface = (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.CUSTOM_CALL_INTERFACE) == 1);
                        if (shouldCreateCustomCallInterface) {
                            CallManager.getInstance().createCallView(CallState.RINGING, incomingNumber);
                        }
                    }
                    lastState = TelephonyManager.CALL_STATE_RINGING;
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    lastState = TelephonyManager.CALL_STATE_OFFHOOK;
                    boolean shouldCreateCustomCallInterface = (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.CUSTOM_CALL_INTERFACE) == 1);
                    if (shouldCreateCustomCallInterface) {
                        CallManager.getInstance().createCallView(CallState.OFF_HOOK);
                    }
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    shouldCreateCustomCallInterface = (TableOffenderDetailsManager.sharedInstance().getIntValueByColumnName(OFFENDER_DETAILS_CONS.CUSTOM_CALL_INTERFACE) == 1);
                    if (shouldCreateCustomCallInterface) {
                        CallManager.getInstance().createCallView(CallState.IDLE);
                    }
                    isFirstReceiverForRinging = true;
                    lastState = TelephonyManager.CALL_STATE_IDLE;
                    TelephonyManager tm1 = (TelephonyManager) getReceiverContext().getSystemService(Context.TELEPHONY_SERVICE);
                    tm1.listen(incomingPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                    break;
            }
        }
    }

    // workaround to solve a problem while PT receive video call and kiosk mode is activated, then device not open video call
    private void allowVideoCallIfInKioskMode() {
        boolean isKnoxActivated = KnoxUtil.getInstance().isKnoxActivated();
        boolean inKioskMode = KnoxUtil.getInstance().getKnoxSDKImplementation().isInKioskMode();
        if (isKnoxActivated && inKioskMode) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    KnoxUtil.getInstance().getKnoxSDKImplementation().setKioskModeState(true);
                }
            }, 2500);
            KnoxUtil.getInstance().getKnoxSDKImplementation().setKioskModeState(false);
        }
    }
}
