package com.supercom.puretrack.data.source.local.local_managers.hardware;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;


import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.ui.dialog.FingerPrintDialog;
import com.supercom.puretrack.ui.dialog.FingerPrintDialog.FingerPrintDialogListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FingerprintManager{
    public boolean isFeatureEnabled() {
        return false;
    }

    public interface FingerPrintManagerListener {
        void onFinshedToConductFingerPrint(int notificationId);

        void onFinshedToRegisterFingerPrint();
    }

    public FingerprintManager(Context context, FingerPrintManagerListener fingerPrintManagerListener) {

    }
}

/*public class FingerprintManager implements FingerPrintDialogListener {

    public static final String TAG = "FingerPrint";

    private boolean onReadyIdentify = false;
    private boolean onReadyEnroll = false;
    private boolean isFeatureEnabled;
    private final Context context;
    private SpassFingerprint mSpassFingerprint;
    private CountDownTimer timedOutTimer;
    private ArrayList<FingerPrintDialog> fingerPrintDialogsArray;

    private final FingerPrintManagerListener fingerPrintManagerListener;

    public FingerprintManager(Context context, FingerPrintManagerListener fingerPrintManagerListener) {
        this.context = context;
        this.fingerPrintManagerListener = fingerPrintManagerListener;
        init();
    }

    public interface FingerPrintManagerListener {
        void onFinshedToConductFingerPrint(int notificationId);

        void onFinshedToRegisterFingerPrint();
    }

    private void init() {
        Spass mSpass = new Spass();
        boolean isSpassInitialized = true;

        try {
            mSpass.initialize(context);
        } catch (SsdkUnsupportedException e) {
            Log.i(TAG, "Exception: " + e);
            isSpassInitialized = false;
        } catch (UnsupportedOperationException e) {
            isSpassInitialized = false;
            Log.i(TAG, "Fingerprint Service is not supported in the device");
        }

        if (isSpassInitialized) {
            isFeatureEnabled = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);

            if (isFeatureEnabled) {
                mSpassFingerprint = new SpassFingerprint(context);
                Log.i(TAG, "Fingerprint Service is supported in the device.");
                Log.i(TAG, "SDK version : " + mSpass.getVersionName());
                fingerPrintDialogsArray = new ArrayList<FingerPrintDialog>();
                registerBroadcastReceiver();
            } else {
                Log.i(TAG, "Fingerprint Service is not supported in the device.");
            }
        }
    }

    public boolean isFeatureEnabled() {
        return isFeatureEnabled;
    }

    public void createBiometricDialog(Activity activity) {
        startTimedOutTimer();

        FingerPrintDialog fingerPrintDialog = new FingerPrintDialog(activity, this);
        fingerPrintDialogsArray.add(fingerPrintDialog);
        fingerPrintDialog.show();
    }

    public void registerFingerPrint() {
        try {
            if (!onReadyIdentify) {
                if (!onReadyEnroll) {
                    onReadyEnroll = true;
                    mSpassFingerprint.registerFinger(context, mRegisterListener);
                    Log.i(TAG, "Jump to the Enroll screen");
                } else {
                    Log.i(TAG, "Please wait and try to register again");
                }
            } else {
                Log.i(TAG, "Please cancel Identify first");
            }
        } catch (UnsupportedOperationException e) {
            Log.i(TAG, "Fingerprint Service is not supported in the device");
        }
    }

    private final SpassFingerprint.RegisterListener mRegisterListener = new SpassFingerprint.RegisterListener() {

        @Override
        public void onFinished() {
            onReadyEnroll = false;
            Log.i(TAG, "RegisterListener.onFinished()");
            fingerPrintManagerListener.onFinshedToRegisterFingerPrint();

        }
    };

    private void identifyWithoutPassword() {
        try {
            if (!mSpassFingerprint.hasRegisteredFinger()) {
                Log.i(TAG, "Please register finger first");
            } else {
                if (!onReadyIdentify) {
                    onReadyIdentify = true;
                    try {
                        mSpassFingerprint.startIdentifyWithDialog(context, listener, false);
                        Log.i(TAG, "Please identify finger to verify you");
                    } catch (IllegalStateException e) {
                        onReadyIdentify = false;
                        Log.i(TAG, "Exception: " + e);
                    }
                } else {
                    Log.i(TAG, "Please cancel Identify first");
                }
            }
        } catch (UnsupportedOperationException e) {
            Log.i(TAG, "Fingerprint Service is not supported in the device");
        }
    }

    private final SpassFingerprint.IdentifyListener listener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            Log.i(TAG, "identify finished : reason=" + getEventStatusName(eventStatus));
            onReadyIdentify = false;
            int FingerprintIndex = 0;
            try {
                FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
                Log.i(TAG, ise.getMessage());
            }

            boolean shouldCloseDialogAndCancelTimeOut = true;
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                Log.i(TAG, "onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.biometricTestPassed, -1, -1);
            } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED || eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
                Log.i(TAG, "onFinished() : Authentification canceled");
                shouldCloseDialogAndCancelTimeOut = false;
            } else {
                Log.i(TAG, "onFinished() : Authentification Fail for identify");
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.biometricTestFailed, -1, -1);
            }

            if (shouldCloseDialogAndCancelTimeOut) {
                if (!fingerPrintDialogsArray.isEmpty()) {
                    fingerPrintDialogsArray.get(fingerPrintDialogsArray.size() - 1).cancel();
                    fingerPrintManagerListener.onFinshedToConductFingerPrint(fingerPrintDialogsArray.size() - 1);
                    fingerPrintDialogsArray.remove(fingerPrintDialogsArray.size() - 1);
                }
                timedOutTimer.cancel();
            }
        }

        @Override
        public void onReady() {
            Log.i(TAG, "identify state is ready");
        }

        @Override
        public void onStarted() {
            Log.i(TAG, "User touched fingerprint sensor!");
        }
    };

    private static String getEventStatusName(int eventStatus) {
        switch (eventStatus) {
            case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                return "STATUS_AUTHENTIFICATION_SUCCESS";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                return "STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS";
            case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                return "STATUS_TIMEOUT";
            case SpassFingerprint.STATUS_SENSOR_FAILED:
                return "STATUS_SENSOR_ERROR";
            case SpassFingerprint.STATUS_USER_CANCELLED:
                return "STATUS_USER_CANCELLED";
            case SpassFingerprint.STATUS_QUALITY_FAILED:
                return "STATUS_QUALITY_FAILED";
            case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                return "STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
            default:
                return "STATUS_AUTHENTIFICATION_FAILED";
        }

    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_RESET);
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_REMOVED);
        filter.addAction(SpassFingerprint.ACTION_FINGERPRINT_ADDED);
        context.registerReceiver(mPassReceiver, filter);
    }

    private final BroadcastReceiver mPassReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SpassFingerprint.ACTION_FINGERPRINT_RESET.equals(action)) {
                Toast.makeText(context, "all fingerprints are removed", Toast.LENGTH_SHORT).show();
            } else if (SpassFingerprint.ACTION_FINGERPRINT_REMOVED.equals(action)) {
                int fingerIndex = intent.getIntExtra("fingerIndex", 0);
                Toast.makeText(context, fingerIndex + " fingerprints is removed", Toast.LENGTH_SHORT).show();
            } else if (SpassFingerprint.ACTION_FINGERPRINT_ADDED.equals(action)) {
                int fingerIndex = intent.getIntExtra("fingerIndex", 0);
                Toast.makeText(context, fingerIndex + " fingerprints is added", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void unregisterBroadcastReceiver() {
        try {
            if (context != null) {
                context.unregisterReceiver(mPassReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFingerPrintDialogSwipe() {

        boolean hasRegisteredFinger = mSpassFingerprint.hasRegisteredFinger();
        if (hasRegisteredFinger) {
            identifyWithoutPassword();
        } else {
            Log.i(TAG, "Please register finger first");
        }
    }

    public boolean hasRegisterFingerPrint() {
        try {
            return mSpassFingerprint.hasRegisteredFinger();
        }catch (Exception ex){
            return false;
        }
    }

    private void startTimedOutTimer() {
        long biometricTimeOut = TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(OFFENDER_DETAILS_CONS.DETAILS_OFF_BIOMETRIC_TIMEOUT);
        timedOutTimer = new CountDownTimer(TimeUnit.SECONDS.toMillis(biometricTimeOut), TimeUnit.SECONDS.toMillis(biometricTimeOut)) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                fingerPrintDialogsArray.get(fingerPrintDialogsArray.size() - 1).cancel();
                fingerPrintManagerListener.onFinshedToConductFingerPrint(fingerPrintDialogsArray.size() - 1);
                fingerPrintDialogsArray.remove(fingerPrintDialogsArray.size() - 1);
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.biometricTestTimeOut, -1, -1);
            }
        }.start();
    }

    public int getFingerPrintDialogsArraySize() {
        if (fingerPrintDialogsArray != null) {
            return fingerPrintDialogsArray.size();
        }
        return -1;
    }

}*/
