package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.supercom.puretrack.util.application.App;

public class AndroidXAppManager {
    private static AndroidXAppManager _instance;
    public static AndroidXAppManager getInstance() {
        if(_instance ==  null){
            _instance =  new AndroidXAppManager(App.getContext());
        }

        return _instance;
    }

    private AndroidXAppManager biometricsAuth;
    BroadcastReceiver biometricReceiver;

    public static final String ACTION_Status =  "com.supercom.androidxapp.biometric.status";
    public static final String ACTION_Status_Result =  "com.supercom.androidxapp.biometric.status.result";
    public static final String ACTION_Enroll =  "com.supercom.androidxapp.biometric.enroll";
    public static final String ACTION_Authenticate =  "com.supercom.androidxapp.biometric.Authenticate";
    public static final String ACTION_Authenticate_cancel =  "com.supercom.androidxapp.biometric.Authenticate.cancel";
    public static final String ACTION_Authenticate_success =  "com.supercom.androidxapp.biometric.Authenticate.success";
    public static final String ACTION_Authenticate_failed =  "com.supercom.androidxapp.biometric.Authenticate.failed";
    public static final String ACTION_error =  "com.supercom.androidxapp.biometric.Authenticate.error";
    public static final String EXTRA_error_message =  "error_message";
    public static final String EXTRA_error_code =  "error_code";
    public static final String EXTRA_status =  "status";
    public static final String EXTRA_action =  "action";
    public static final int    ERROR_authenticationFailed =  -1;
    public static final int    ERROR_enroll =  -3;
    public static final int    ERROR_initialize =  -4;
    public static final int    ERROR_no_status =  -5;

    Context context;
    private BiometricsListener biometricsListener;
    public void setBiometricsListener(BiometricsListener biometricsListener) {
        this.biometricsListener = biometricsListener;
    }

    private AndroidXAppManager(Context context){
        this.context = context;
        registerToBiometricBroadcasts();
    }

    private void registerToBiometricBroadcasts() {
        biometricReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                 if(intent.getAction().equals(ACTION_Authenticate_failed)){
                   biometricsListener.onAuthenticateFailed();
                }else if(intent.getAction().equals(ACTION_Authenticate_success)){
                     biometricsListener.onAuthenticate();
                 }else if(intent.getAction().equals(ACTION_error)){
                     biometricsListener.onError(
                             intent.getStringExtra(EXTRA_error_message),
                             intent.getIntExtra(EXTRA_error_code,0)
                     );
                 } else if(intent.getAction().equals(ACTION_Status_Result)){
                      int statusIndex = intent.getIntExtra(EXTRA_status,-1);
                      if(statusIndex >=0) {
                          biometricsListener.onStatusReceived(BiometricStatus.values()[statusIndex]);
                      }
                 }
            }
        };

        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_Authenticate_failed);
        filter.addAction(ACTION_Authenticate_success);
        filter.addAction(ACTION_error);
        filter.addAction(ACTION_Status_Result);

        context.registerReceiver(biometricReceiver,filter);
    }
    public void unregisterToBiometricBroadcasts() {
        context.unregisterReceiver(biometricReceiver);
    }

    public interface BiometricsListener{
        void onAuthenticate();
        void onAuthenticateFailed();
        void onError(String error,int errorCode);
        void onStatusReceived(BiometricStatus status);
    }

    public enum BiometricStatus {
        BIOMETRIC_SUCCESS,
        BIOMETRIC_ERROR_NO_HARDWARE,
        BIOMETRIC_ERROR_HW_UNAVAILABLE,
        BIOMETRIC_UNKNOWN_ERROR,
        BIOMETRIC_ERROR_NONE_ENROLLED,
    }
    public void getStatus(){
        startBiometricActivity(ACTION_Status);
    }

    public void startEnroll(){
         startBiometricActivity(ACTION_Enroll);
    }
    public void startAuthenticate(){
        startBiometricActivity(ACTION_Authenticate);
    }
    public void cancelAuthenticate(){
        context.sendBroadcast(new Intent(ACTION_Authenticate_cancel));
    }

    private void startBiometricActivity(String action) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.supercom.androidxapp", "com.supercom.androidxapp.biometrics.BiometricServiceActivity");
            intent.putExtra(EXTRA_action, action);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startApp() {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.supercom.androidxapp");
        if (launchIntent != null) {
            context.startActivity(launchIntent);
        }
    }
}
