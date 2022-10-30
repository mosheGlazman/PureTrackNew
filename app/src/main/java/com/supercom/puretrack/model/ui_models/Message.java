package com.supercom.puretrack.model.ui_models;

import com.supercom.puretrack.model.database.entities.EntityTextMessage;

public class Message {

    private final int MsgType;
    private final long MsgTime;
    private final String MsgUiTime;
    private final String MsgSender;
    private final String MsgText;
    private int MsgRead;
    private final int MsgID;


    public Message(EntityTextMessage recordTextMessage) {

        this.MsgType = recordTextMessage.Type; // 0 - MSG from officer, 1- MSG from offender
        this.MsgTime = recordTextMessage.Time;
        this.MsgUiTime = recordTextMessage.UiTime;
        this.MsgSender = recordTextMessage.Sender;
        this.MsgText = recordTextMessage.Text;
        this.MsgRead = recordTextMessage.Read;
        this.MsgID = recordTextMessage.Id;
    }

    public String getMsgText() {
        return MsgText;
    }

    public int getMsgType() {
        return MsgType;
    }

    public long getMsgTime() {
        return MsgTime;
    }

    public int getMsgRead() {
        return MsgRead;
    }

    public String getMsgSender() {
        return MsgSender;
    }

    public long getDay() {
        if (MsgTime == 0) return 0;

        return MsgTime / 86400000;
    }
}
