/**
 *
 */
package com.supercom.puretrack.data.source.local.local_managers.parsing;

import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.factory.SmsOperationFactory;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsMsg;
import com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base.SmsOperation;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SmsOperationManager {
    private static final String SYSTEM_SMS_OPERATION_PREFIX = "@@";
    private final SmsOperationFactory _smsOperationFactory = new SmsOperationFactory();
    private long _lastSmsOperationIndex = -1;


    private static final SmsOperationManager INSTANCE = new SmsOperationManager();


    private SmsOperationManager() {
    }

    public static SmsOperationManager getInstance() {

        return INSTANCE;
    }


    /**
     * Create concrete SmsOperation object from the SmsMsg body and execute the concrete SmsOperation functionality: 
     <br>"@@{\"Crc\":3691476465,\"OpData\":\"nF5qjvkF6NErQ3Et3o0h6qrvVd6KIkfuYt6Mui8lAsk=\"}"
     <p>1) 	Check if the SmsMsg has system format @@ prefix.
     <br><br>2) 	Parse SmsMsg.msgBody to smsOperationEntityJsonStr obj. 
     <br>"@@{\"Crc\":3691476465,\"OpData\":\"nF5qjvkF6NErQ3Et3o0h6qrvVd6KIkfuYt6Mui8lAsk=\"}"  -->  <br>{\"Crc\":3691476465,\"OpData\":\"nF5qjvkF6NErQ3Et3o0h6qrvVd6KIkfuYt6Mui8lAsk=\"}
     <br><br>3)	 Decrypt SmsOperationEntity.OpData to the base SmsOperation JSON string.
     <br>"OpData":"nF5qjvkF6NErQ3Et3o0h6qrvVd6KIkfuYt6Mui8lAsk="  -->  smsOperationJsonStr
     <br><br>4) Verify the CRC check of the decrypted smsOperationJsonStr with the SmsOperationEntity.Crc
     <br><br>5) 	Parse the smsOperationJsonStr to the base SmsOperation object.
     <br>smsOperationJsonStr --> smsOperation
     <br>Parse the base SmsOperation JSON string to the concrete Sms operation class according to the SmsOperation.OpCode
     <br>smsOperation --> SmsOpStartEarlyCycle
     *
     * @param smsMessage Received Sms message , should of the System SMS message format. 
     * <br>Example of SmsMsg.msgBody: <br>"@@{\"Crc\":3691476465,\"OpData\":\"nF5qjvkF6NErQ3Et3o0h6qrvVd6KIkfuYt6Mui8lAsk=\"}"
     */
    public void handleSmsOperationMessage(SmsMsg smsMessage) {
        if (hasSystemSmsOperationFormat(smsMessage.getMsgBody())) {
            SmsOperation smsOperation = _smsOperationFactory.parseSmsOperation(smsMessage);
            validateAndHandelSmsOperationObj(smsOperation);
        } else {
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                    "SmsOperationManager: Regular SMS Received - Not SystemOperation SMS",
                    DebugInfoModuleId.Receivers.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        }
    }

    /**
     * Check the validity of the concrete SmsOperation object and if allowed then execute it's functionality: 
     <br><br>1) Check if isSmsOperationSenderAllowed, according to the white numbers list numbers received from PM.
     <br><br>2) Check if the SMS operation valid: isSmsOperationValid 
     <br> a. The SmsOperation Index is not the same as the previous one.
     <br> b. The SmsOperation code is valid.
     * @param smsOperation Base SmsOperation object to validate and handle. 
     */
    public void validateAndHandelSmsOperationObj(SmsOperation smsOperation) {
        if (smsOperation == null) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.sysSmsConditionsNotMet, -1, -1);
            String messageToUpload = "SmsOperationManager: SystemSMS Received - SmsOperationError: Failed to parse the SystemSmsOperation";
            TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                    messageToUpload, DebugInfoModuleId.Receivers.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
        } else {
            boolean isAllIncomingCallsAllowed = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName
                    (TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONES_ACTIVE) == 0;
            if (isAllIncomingCallsAllowed || isSmsOperationSenderAllowed(smsOperation.getSenderNumber())) {
                if (isSmsOperationValid(smsOperation)) {
                    smsOperation.onSmsOperationSuccess("SmsOperationManager: SystemSMS Received - SmsOperationSuccess: Sender Allowed & Valid SystemOp Format.");
                    _lastSmsOperationIndex = smsOperation.getIndex();
                } else {
                    smsOperation.onSmsOperationError("SmsOperationManager: SystemSMS Received - SmsOperationError: The SystemOp format is not valid.");
                }
            } else {
                smsOperation.onSmsOperationError("SmsOperationManager: SystemSMS Received - SmsOperationError: Sender Not Allowed");
            }
        }
    }

    //	check validity for the OpCode and operation Index
    private boolean isSmsOperationValid(SmsOperation smsOpretaion) {
        return smsOpretaion.getIndex() != _lastSmsOperationIndex && isOpCodeValid(smsOpretaion.getOpCode());
    }

    private boolean isOpCodeValid(int opCode) {
        return opCode >= SmsOperationFactory.SmsOperationType.TYPES_RANGE.x && opCode <= SmsOperationFactory.SmsOperationType.TYPES_RANGE.y;
    }

    private boolean hasSystemSmsOperationFormat(String smsMsgBody) {
        String smsOpFormatPrefix = null;
        int systemSmsOperationPrefixLength = SYSTEM_SMS_OPERATION_PREFIX.length();
        if (smsMsgBody.length() >= systemSmsOperationPrefixLength) {
            smsOpFormatPrefix = smsMsgBody.substring(0, systemSmsOperationPrefixLength);
        }
        return smsOpFormatPrefix != null && smsOpFormatPrefix.equals(SYSTEM_SMS_OPERATION_PREFIX);
    }

    private boolean isSmsOperationSenderAllowed(String senderNumber) {
        ArrayList<String> allowedIncomingList = DatabaseAccess.getInstance().tableOffenderDetails.getAllowedIncomingList();
        return allowedIncomingList.contains(senderNumber);
    }
}
