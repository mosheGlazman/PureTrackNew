package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base;

import android.util.Log;

import com.supercom.puretrack.data.source.local.table_managers.TableDebugInfoManager;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table.TableDebugInfo.DebugInfoPriority;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.model.database.enums.DebugInfoModuleId;
import com.supercom.puretrack.util.date.TimeUtil;

import java.util.concurrent.TimeUnit;

public class SmsOperation {
    protected static String TAG;

    private long Index;
    private int OpCode;
    private String senderNumber;


    public SmsOperation() {
        super();
        TAG = this.getClass().getSimpleName();
    }

    protected void performSmsOperation() {
        Log.i(TAG, "performSmsOperation()");
    }

    public void onSmsOperationSuccess(String successInfoMsg) {
        String additionalInfo = "System SMS received at " + TimeUtil.getCurrentTimeStr() + ", Opcode = " + OpCode;
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.sysSmsReceived, -1, -1, additionalInfo);
        sendDebugInfo(successInfoMsg);
        this.performSmsOperation();
    }

    public void onSmsOperationError(String errInfoMsg) {
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.sysSmsConditionsNotMet, -1, -1);
        sendDebugInfo(errInfoMsg);
    }

    private void sendDebugInfo(String infoMsg) {
        String messageToUpload = "SMS Op Class: " + this.getClass().getSimpleName() +
                ", Info Message: " + infoMsg +
                ", SmsOperation Type: " + OpCode +
                ", Sender number: " + getSenderNumber();

        TableDebugInfoManager.sharedInstance().addNewRecordToDB(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()), messageToUpload,
                DebugInfoModuleId.Receivers.ordinal(), DebugInfoPriority.NORMAL_PRIORITY);
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public long getIndex() {
        return Index;
    }

    public void setIndex(long index) {
        Index = index;
    }

    public int getOpCode() {
        return OpCode;
    }

    public void setOpCode(int opCode) {
        OpCode = opCode;
    }
}
