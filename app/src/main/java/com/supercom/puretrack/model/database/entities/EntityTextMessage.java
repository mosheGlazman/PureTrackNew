package com.supercom.puretrack.model.database.entities;

public class EntityTextMessage extends DatabaseEntity {
    public int Type;        // 0 - MSG from officer, 1- MSG from offender
    public long Time;
    public String UiTime;
    public String Sender="";
    public String Text;
    public int Read;
    public int Id; // event ID

    public EntityTextMessage(int Type, long Time, String UiTime, String Sender, String Text, int ID) {
        this.Type = Type;
        this.Time = Time;
        this.UiTime = UiTime;
        this.Sender = Sender;
        this.Text = Text;
        this.Read = 0;
        this.Id = ID;
    }

    public void SetRead(int Read) {
        this.Read = Read;
    }

}
