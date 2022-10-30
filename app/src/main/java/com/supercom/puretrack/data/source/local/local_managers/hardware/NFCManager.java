package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;

import com.supercom.puretrack.util.application.App;

import java.util.ArrayList;

public class NFCManager {
    private static NFCManager instance;

    public static NFCManager getInstance() {
        if (instance == null) {
            instance = new NFCManager();
        }

        return instance;
    }

    String EXTRA_INSTANCE_ID="INSTANCE_ID";
    String ACTION_NFC="com.supercom.googleapitestapp.ACTION_NFC";
    NfcAdapter nfcAdapter;
    boolean active=false;
    boolean available=false;
    String error="";
    IntentFilter[]  writeTagFilters;

    BroadcastReceiver mReceiver;
    PendingIntent pendingIntent;
    SensorListener listener;
    Context context;

    private NFCManager(){
        context = App.applicationContext;

        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            error = "This device doesn't support NFC.";
            available = false;
            return;
        }

        available = true;

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{
                tagDetected
        };

        pendingIntent = createBroadcastIntent(SensorDataSource.getInstance().getInstanceId());

        mReceiver = new NFCReceiver();
        context.registerReceiver(mReceiver,new IntentFilter(ACTION_NFC));
    }

    public void setListener(SensorListener listener) {
        this.listener = listener;
    }

    private PendingIntent createBroadcastIntent(int instanceId) {
        Intent intent = new Intent(ACTION_NFC).setPackage(context.getPackageName());
        intent.putExtra(ACTION_NFC, instanceId);
        return PendingIntent.getBroadcast(
                context, instanceId, intent, PendingIntent.FLAG_CANCEL_CURRENT
        );
    }

    public void turnOn(Activity activity) {
        active = true;
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, writeTagFilters, null);
    }

    public void turnOff(Activity activity) {
        active = false;
        nfcAdapter.disableForegroundDispatch(activity);
    }

    class NFCReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            SensorData data =new SensorData(SensorData.E_SensorType.NFC);
            SensorDataSource.getInstance().add(data);
            if(listener!=null) {
                listener.onReceivedSensorData(data);
            }
        }
    }
}
