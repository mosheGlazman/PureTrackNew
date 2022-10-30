package com.supercom.puretrack.model.database.objects;

public class VoipSettings {
    public static final int VOICE_CALL = 1; //without Sinch
    public static final int VOIP_CALL = 2;  //app-to-landline
    int Enable;
    int OutgoingCalls;

    public int getEnable() {
        return Enable;
    }

    public int getOutgoingCalls() {
        return OutgoingCalls;
    }
}