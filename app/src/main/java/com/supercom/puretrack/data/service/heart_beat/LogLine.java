package com.supercom.puretrack.data.service.heart_beat;

import java.io.Serializable;
import java.util.Date;

public class LogLine implements Serializable {
    Date time =new Date();
    String text;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(Boolean actionStatus) {
        this.actionStatus = actionStatus;
    }

    String tag="";
    Boolean actionStatus;
}
