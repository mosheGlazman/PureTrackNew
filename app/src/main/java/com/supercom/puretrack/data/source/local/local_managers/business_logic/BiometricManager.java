package com.supercom.puretrack.data.source.local.local_managers.business_logic;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.data.source.local.local_managers.hardware.HardwareUtilsManager;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.model.business_logic_models.shielding.CellularStrengthModel;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.ui.dialog.FingerPrintDialog;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.hardware.VoiceManager;

import java.util.concurrent.TimeUnit;

public class BiometricManager implements AndroidXAppManager.BiometricsListener, FingerPrintDialog.FingerPrintDialogListener {
    private static BiometricManager _instance;

    public static BiometricManager getInstance() {
        if (_instance == null) {
            _instance = new BiometricManager();
        }

        return _instance;
    }

    boolean inAuthenticateProcess;
    BiometricManagerListener listener;

    public interface BiometricManagerListener{
        void onOpenDialog();
        void onCloseDialog();
    }

    public void setListener(BiometricManagerListener listener) {
        this.listener = listener;
    }

    FingerPrintDialog fingerPrintDialog;
    MainActivity activity;
    int failedCounter;

    public void startAuthenticate(MainActivity activity) {
        this.activity = activity;
        failedCounter = 0;
        inAuthenticateProcess = true;
        AndroidXAppManager.getInstance().setBiometricsListener(this);
        AndroidXAppManager.getInstance().getStatus();
    }

    @Override
    public void onAuthenticate() {
        TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.biometricTestPassed, -1, -1);
        closeFingerPrintDialog();
    }

    @Override
    public void onAuthenticateFailed() {
        //TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.biometricTestFailed, -1, -1);
        //closeFingerPrintDialog();
    }

    @Override
    public void onError(String error, int errorCode) {
        Log.i("FingerPringError","errorCode:"+errorCode+"   error:"+error);

        // 13=cancel

        if(errorCode==AndroidXAppManager.ERROR_authenticationFailed){
            failedCounter++;
        }
    }

    private void closeFingerPrintDialog() {
        if(fingerPrintDialog!=null) {
            timedOutTimer.cancel();
            try {
                fingerPrintDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStatusReceived(AndroidXAppManager.BiometricStatus status) {
        if (inAuthenticateProcess) {
            nextAuthenticate(status);
            return;
        }
    }

    private void nextAuthenticate(final AndroidXAppManager.BiometricStatus status) {
        if (status == AndroidXAppManager.BiometricStatus.BIOMETRIC_ERROR_NONE_ENROLLED) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.noFingerprintRegistered, -1, -1);
            inAuthenticateProcess = false;
            return;
        }

        if (status == AndroidXAppManager.BiometricStatus.BIOMETRIC_SUCCESS){
            startTimedOutTimer();
            VoiceManager.getInstance(activity).runSoundAndVibrate(TableEventConfig.EventsAlarmsType.DEVICE_SETTINGS);
            fingerPrintDialog = new FingerPrintDialog(activity, this );
            fingerPrintDialog.show();
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getContext(), "status is: " + status, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private CountDownTimer timedOutTimer;
    private void startTimedOutTimer() {
        long biometricTimeOut = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DETAILS_OFF_BIOMETRIC_TIMEOUT);
        timedOutTimer = new CountDownTimer(TimeUnit.SECONDS.toMillis(biometricTimeOut), TimeUnit.SECONDS.toMillis(biometricTimeOut)) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                AndroidXAppManager.getInstance().cancelAuthenticate();
                closeFingerPrintDialog();
                if(failedCounter==0) {
                     TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.biometricTestTimeOut, -1, -1);
                }else{
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.biometricTestFailed, -1, -1);
                }
            }
        }.start();
    }

    @Override
    public void onFingerPrintDialogSwipe() {
        AndroidXAppManager.getInstance().startAuthenticate();
    }

}
