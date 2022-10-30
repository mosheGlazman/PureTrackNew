package com.supercom.puretrack.util.hardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.gson.Gson;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.util.application.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AppsSharedDataManager extends BroadcastReceiver {
    private static String ACTION_ASK_WHITE_LIST="com.supercom.puretrack.util.hardware.AppsSharedDataManager.ask";
    private static String ACTION_SEND_WHITE_LIST="com.supercom.puretrack.util.hardware.AppsSharedDataManager.send";

    public static AppsSharedDataManager instance;
    public static String TAG = "SharedDataManager";

    public static AppsSharedDataManager getInstance() {
        if (instance == null) {
            instance = new AppsSharedDataManager();
        }
        return instance;
    }

    private AppsSharedDataManager() {

    }
    public void listenToAskAllowedIncomingList() {
        App.applicationContext.registerReceiver(this,new IntentFilter(ACTION_ASK_WHITE_LIST));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        sendDataToDialer();
    }

    public void sendDataToDialer() {
        Intent i=new Intent(ACTION_SEND_WHITE_LIST);
        WhiteListFileObject whiteListFileObject=new WhiteListFileObject();
        whiteListFileObject.numbers = getAllowedIncomingList();
        whiteListFileObject.isEnabled  = isWhiteListEnabled();
        String json = new Gson().toJson(whiteListFileObject);
        i.putExtra("json",json);
        App.applicationContext.sendBroadcast(i);
    }

    private Boolean isWhiteListEnabled() {
       return TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONES_ACTIVE) == 1;
    }

    private ArrayList<String> getAllowedIncomingList() {
        String deviceConfigIncomingCallsWhiteList = DatabaseAccess.getInstance().tableOffenderDetails
                .getRecordOffDetails().DeviceConfigIncomingCallsWhiteList;
        ArrayList<String> incomingCallsWhiteListArrayList = new ArrayList<String>();
        try {
            if (!deviceConfigIncomingCallsWhiteList.isEmpty()) {
                JSONObject jsonObject = new JSONObject(deviceConfigIncomingCallsWhiteList);
                JSONArray incomingCallsWhiteListJsonArray = jsonObject.getJSONArray("phones");
                for (int i = 0; i < incomingCallsWhiteListJsonArray.length(); i++) {
                    if(incomingCallsWhiteListJsonArray.getString(i).length()>0) {
                        incomingCallsWhiteListArrayList.add(incomingCallsWhiteListJsonArray.getString(i));
                    }
                }
            }

            //String OfficerNumber = DatabaseAccess.getInstance().tableOffenderDetails.getRecordOffDetails().DeviceConfigPhoneOfficer;
            //if(OfficerNumber!= null && OfficerNumber.length() > 0){
            //    incomingCallsWhiteListArrayList.add(OfficerNumber);
            //}

            //String AgencyNumber = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.OFFENDER_CONFIG_PHONE_AGENCY);
            //if(AgencyNumber!= null && AgencyNumber.length() > 0){
            //    incomingCallsWhiteListArrayList.add(AgencyNumber);
            //}

            //String phoneEmergency = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PHONE_EMERGENCY);
            //if(phoneEmergency!= null && phoneEmergency.length() > 0){
            //    incomingCallsWhiteListArrayList.add(phoneEmergency);
            //}
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return incomingCallsWhiteListArrayList;
    }
}