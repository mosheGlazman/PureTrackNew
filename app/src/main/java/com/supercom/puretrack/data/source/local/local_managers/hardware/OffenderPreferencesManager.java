package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.util.Log;

import com.google.gson.Gson;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.util.application.App;

import java.util.Date;

public class OffenderPreferencesManager extends PreferencesBase{
    public int setLastBatteryPercentCounter=0;
    public boolean isCheckedBatteryPercentForSuddenShutDown;

    private static OffenderPreferencesManager instance;
    public static OffenderPreferencesManager getInstance() {
        if (instance == null) {
            instance = new OffenderPreferencesManager();
        }
        return instance;
    }
    private OffenderPreferencesManager(){
        super(App.getContext(),"OffenderPreferences");
    }

    private SuddenShutDownParams suddenShutDownParams;
    private TagVibrateOnDisconnectParams tagVibrateOnDisconnectParams;

    public int getLastBatteryPercent(){
        return get("BatteryPercent",0);
    }

    public void setLastBatteryPercent(int percent){
        Log.i("BatteryPercentT",percent+"");
        setLastBatteryPercentCounter++;

        put("BatteryPercent",percent);
        put("BatteryPercentDate",new Date().getTime());
    }

    public Date getLastBatteryPercentDate(){
        return new Date(get("BatteryPercentDate",new Date().getTime()));
    }


    public void setSuddenShutDownParams(String json){
        suddenShutDownParams = new Gson().fromJson(json,SuddenShutDownParams.class);
        put("ShutDownParams",json);
    }
    public SuddenShutDownParams getSuddenShutDownParams(){
        if(suddenShutDownParams==null) {
            String j = get("ShutDownParams", "");
            if (j.length() == 0) {
                suddenShutDownParams = new SuddenShutDownParams();
            } else {
                suddenShutDownParams = new Gson().fromJson(j, SuddenShutDownParams.class);
            }
        }

        return suddenShutDownParams;
    }
    public class SuddenShutDownParams{
       public int enabled =1;
       public int minbatterylevel=3;
       public int threshold=20;
    }

    public void setTagVibrateOnDisconnectParams(String json){
        tagVibrateOnDisconnectParams = new Gson().fromJson(json,TagVibrateOnDisconnectParams.class);
        put("TagVibrateParams",json);
    }
    public TagVibrateOnDisconnectParams getTagVibrateOnDisconnectParams(){
        if(tagVibrateOnDisconnectParams==null) {
            String j = get("TagVibrateParams", "");
            if (j.length() == 0) {
                tagVibrateOnDisconnectParams = new TagVibrateOnDisconnectParams();
            } else {
                tagVibrateOnDisconnectParams = new Gson().fromJson(j, TagVibrateOnDisconnectParams.class);
            }
        }

        if(tagVibrateOnDisconnectParams==null) {
            tagVibrateOnDisconnectParams = new TagVibrateOnDisconnectParams();
        }

        return tagVibrateOnDisconnectParams;
    }

    public class TagVibrateOnDisconnectParams{
        public int enabled =0;
        public int hbinterval=15;
        public int timeouttovibrate=100;
    }


    public void checkForSuddenShutDown() {
        Log.i("bug652","checkForSuddenShutDown");
        if(isCheckedBatteryPercentForSuddenShutDown){
            Log.i("bug652","checkForSuddenShutDown return");
            return;
        }

        try {
            OffenderPreferencesManager.SuddenShutDownParams params=OffenderPreferencesManager.getInstance().getSuddenShutDownParams();
            int lastPercent = OffenderPreferencesManager.getInstance().getLastBatteryPercent();
            if (lastPercent < params.minbatterylevel) {
                Log.i("bug652", "lastPercent: " + lastPercent);
                return;
            }

            Date lastPercentDate = OffenderPreferencesManager.getInstance().getLastBatteryPercentDate();
            long passTime = new Date().getTime() - lastPercentDate.getTime();
            if (passTime < (params.threshold * 1000)) {
                Log.i("bug652", "lastPercent pass: " + passTime);
                return;
            }

            Log.i("bug652", "addPowerOnAfterSuddenlyShutDownEvent");
            TableEventsManager.sharedInstance().addPowerOnAfterSuddenlyShutDownEventToLog(lastPercent);
        }finally {
            isCheckedBatteryPercentForSuddenShutDown=true;
        }
    }
}
