package com.supercom.puretrack.data.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.local_managers.parsing.SmsOperationManager;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsMsg;

public class SmsReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER";

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED) || intent.getAction().equals(SMS_DELIVER)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                SmsMsg smsMsg = constructSmsMsg(messages);
                handleSmsOperation(smsMsg);
            }
        }
    }

    public void simulateSmsReceived(String smsMsgBodyStr, String senderNum) {
        SmsMsg smsMsg = new SmsMsg();
        smsMsg.setMsgBody(smsMsgBodyStr);
        smsMsg.setSenderNumber(senderNum);
        SmsOperationManager.getInstance().handleSmsOperationMessage(smsMsg);
    }

    private SmsMsg constructSmsMsg(SmsMessage[] messages) {
        SmsMsg smsMsg = null;

        if (messages.length > -1) {
            smsMsg = new SmsMsg();
            StringBuilder msgBodyStrBuilder = new StringBuilder();

            for (int i = 0; i < messages.length; i++) {
                msgBodyStrBuilder.append(messages[i].getMessageBody());
            }

            smsMsg.setMsgBody(msgBodyStrBuilder.toString());
            smsMsg.setSenderNumber(messages[0].getOriginatingAddress());
        }

        return smsMsg;
    }

    private void handleSmsOperation(SmsMsg smsMsg) {
        if (smsMsg == null) {
            String msgToUpload = "SmsReceiver : Failed to construct SmsMsg.";
            App.writeToNetworkLogsAndDebugInfo(TAG, msgToUpload, DebugInfoModuleId.Receivers);
        } else {
            SmsOperationManager.getInstance().handleSmsOperationMessage(smsMsg);
        }
    }
}
