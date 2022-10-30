package com.supercom.puretrack.ui.lockscreen;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.view.GestureDetectorCompat;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.broadcast_receiver.IncomingCallReceiver;
import com.supercom.puretrack.data.repositories.NetworkRepository;
import com.supercom.puretrack.ui.activity.MainActivity;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderStatusManager;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.util.general.LocaleUtil;


public class LockScreenActivity extends Activity implements GestureDetector.OnGestureListener {

    private static final int DISTANCE_ON_SCROLL = 80;
    private GestureDetectorCompat detectMe;
    private int pinCodeAttempts = 5;   // number of attempts before lock
    private int pinCodeLockTime = 1000;// lock timeout when entering wrong password
    private int currentAttempts = 1;

    private boolean isThreadRunning = false;
    EditText passwordEditTxt = null;
    TextView txtView = null;
     public static boolean cancelNextLock;

    public static void start(Context context) {
        if(IncomingCallReceiver.getLastPhoneState()!= TelephonyManager.CALL_STATE_IDLE){
            return;
        }

        Intent intent = new Intent(context, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        App.IS_PINCODE_TYPED = false;
        if (TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_ENABLE) == 1) {
            pinCodeAttempts = (int) TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_ATTEMPTS);
            pinCodeLockTime = (int) TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_LOCK_TIME);
            setContentView(R.layout.lockscreen_pincode);

            passwordEditTxt = findViewById(R.id.editText);
            txtView = findViewById(R.id.textView);

            passwordEditTxt.setFocusable(true);
            passwordEditTxt.setFocusableInTouchMode(true);
            passwordEditTxt.requestFocus();
            if (TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS) < System.currentTimeMillis()
                    && TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS) != 0) {
                passwordEditTxt.setVisibility(View.VISIBLE);
                txtView.setVisibility(View.GONE);
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(passwordEditTxt.getWindowToken(), 0);

            passwordEditTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId,
                                              KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (v.getText().toString().equals(TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_PIN))) {
                            App.IS_PINCODE_TYPED = true;
                            TableOffenderStatusManager.sharedInstance().updateColumnLong(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS, 0);
                            handled = true;
                            currentAttempts = 1;
                            finish();
                        } else {

                            v.setText("");

                            if (currentAttempts >= pinCodeAttempts) {
                                // update table: the device is locked! two many attempts,
                                // set the lock time so will be locked after restart

                                TableOffenderStatusManager.sharedInstance().updateColumnLong(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS, System.currentTimeMillis() + pinCodeLockTime * 1000);
                                // send event - lock period started
                                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.lockAfterPincodeAttemptsStarted, -1, -1);
                                NetworkRepository.getInstance().startNewCycle();


                                passwordEditTxt.setVisibility(View.GONE);
                                txtView.setText(R.string.wrong_pincode_deviceLock);
                                txtView.setVisibility(View.VISIBLE);

                                if (!isThreadRunning) {
                                    new Thread() {
                                        public void run() {
                                            try {
                                                this.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                                                isThreadRunning = true;
                                                try {
                                                    Thread.sleep(pinCodeLockTime * 10000L);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                    Thread.currentThread().interrupt();
                                                }
                                                LockScreenActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        passwordEditTxt.setVisibility(View.VISIBLE);
                                                        txtView.setVisibility(View.GONE);
                                                    }
                                                });
                                                currentAttempts = 1;
                                                TableOffenderStatusManager.sharedInstance().updateColumnLong(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS, System.currentTimeMillis());
                                                isThreadRunning = false;
                                                // send event - lock period ended
                                                checkAndSendLockEndedEventIfNeeded();
                                                Thread.currentThread().interrupt();

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                isThreadRunning = false;
                                                Thread.currentThread().interrupt();
                                            }
                                        }
                                    }.start();
                                }
                            } else {
                                txtView.setVisibility(View.VISIBLE);
                                currentAttempts++;
                                txtView.setText(R.string.wrong_pincode);
                                txtView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtView.setVisibility(View.GONE);
                                    }
                                }, 2000); //2000
                            }

                        }
                    } else if (actionId == EditorInfo.IME_ACTION_SEND
                            || actionId == EditorInfo.IME_ACTION_SEARCH
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        /* handle action here */
                        handled = true;
                    }

                    return handled;
                }
            });

        } else {
            setContentView(R.layout.lockscreen);
            detectMe = new GestureDetectorCompat(this, this);
        }

        if (getIntent() != null && getIntent().hasExtra("kill") && getIntent().getExtras().getInt("kill") == 1) {
            finish();
        }
        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onResume() {
        if(cancelNextLock){
            cancelNextLock =false;
            super.onResume();
            finish();
            return;
        }

        if (TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS) > System.currentTimeMillis()
                && TableOffenderStatusManager.sharedInstance().getLongValueByColumnName(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS) != 0) {
            passwordEditTxt.setVisibility(View.GONE);
            txtView.setText(R.string.wrong_pincode_deviceLock);
            txtView.setVisibility(View.VISIBLE);
            if (!isThreadRunning) {
                new Thread() {
                    public void run() {
                        try {
                            this.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                            isThreadRunning = true;
                            Thread.sleep(300);
                            try {
                                Thread.sleep(pinCodeLockTime * 1000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Thread.currentThread().interrupt();
                            }
                            LockScreenActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    passwordEditTxt.setVisibility(View.VISIBLE);
                                    txtView.setVisibility(View.GONE);
                                }
                            });
                            currentAttempts = 1;
                            TableOffenderStatusManager.sharedInstance().updateColumnLong(TableOffenderStatusManager.OFFENDER_STATUS_CONS.COLUMN_DEVICE_STATUS_LOCKED_ON_ATTEMPTS, System.currentTimeMillis());
                            // send event - lock period ended
                            checkAndSendLockEndedEventIfNeeded();
                            isThreadRunning = false;
                            Thread.currentThread().interrupt();

                        } catch (Exception e) {
                            e.printStackTrace();
                            isThreadRunning = false;
                            Thread.currentThread().interrupt();
                        }
                    }
                }.start();
            }
        } else {
            if (passwordEditTxt != null) {
                passwordEditTxt.setVisibility(View.VISIBLE);
            }
            if (txtView != null) {
                txtView.setVisibility(View.GONE);
            }
            currentAttempts = 1;
            checkAndSendLockEndedEventIfNeeded();
        }
        super.onResume();
    }

    private void checkAndSendLockEndedEventIfNeeded() {
        boolean isLockScreenEventOpened = TableEventsManager.sharedInstance().hasOpenEventInViolationCategory(TableEventConfig.ViolationCategoryTypes.PINCODE_ATTEMPTS);
        if (isLockScreenEventOpened) {
            TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.lockAfterPincodeAttemptsEnded, -1, -1);
            NetworkRepository.getInstance().startNewCycle();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        new LocaleUtil().changeApplicationLanguageIfNeeded();

    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
        return;
    }

    //only used in lockdown mode
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_POWER) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
            //this is where I can do my stuff
            return true; //because I handled the event
        }
        return keyCode == KeyEvent.KEYCODE_HOME;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
            return false;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

            return true;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            if (passwordEditTxt != null && passwordEditTxt.length() > 0)
                return super.dispatchKeyEvent(event);
        }
        return false;
    }

    public void onDestroy() {
        isThreadRunning = false;
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //if (TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_ENABLE) != 1) {
        if (detectMe != null) {
            this.detectMe.onTouchEvent(event);
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //if (TableOffenderDetailsManager.sharedInstance().getLongValueByColumnName(TableOffenderDetailsManager.OFFENDER_DETAILS_CONS.DEVICE_CONFIG_PINCODE_ENABLE) != 1) {
        if (detectMe!= null) {
            if (Math.abs(distanceX) > DISTANCE_ON_SCROLL || Math.abs(distanceY) > DISTANCE_ON_SCROLL) {
                finish();
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}