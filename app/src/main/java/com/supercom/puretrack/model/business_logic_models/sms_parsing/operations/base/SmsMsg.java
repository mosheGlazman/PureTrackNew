package com.supercom.puretrack.model.business_logic_models.sms_parsing.operations.base;

public class SmsMsg {
    private String msgBody;
    private String senderNumber;


    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }
}
