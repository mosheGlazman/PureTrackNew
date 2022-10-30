package com.supercom.puretrack.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.WindowManager;


import androidx.fragment.app.FragmentActivity;

import com.supercom.puretrack.data.BuildConfig;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.model.business_logic_models.knox.KnoxSettingsModel;
import com.supercom.puretrack.ui.lockscreen.LockScreenService;
import com.supercom.puretrack.util.application.App;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.util.general.LocaleUtil;

public abstract class BaseActivity extends FragmentActivity {

    public static boolean isCameFromLauncherActivityCode;
    public static boolean isCameFromRegularActivityCode;

    protected static boolean isAllDangerousPermissionsGranted = false; // this cause issue, no default value

    protected void init() {
        isAllDangerousPermissionsGranted = true;
        handleLockScreen();
    }

    public void handleLockScreen() {
        KnoxSettingsModel KnoxSettingsModel = TableOffenderDetailsManager.sharedInstance().getCurKNOXSettingsConfiguration();
        if (KnoxSettingsModel.getLockScreen() == 1) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            stopService(new Intent(App.getContext(), LockScreenService.class));
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        try {
            startService(new Intent(App.getContext(), LockScreenService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setFullScreen() {
        if(!BuildConfig.DEBUG){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isCameFromLauncherActivityCode = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus && isAllDangerousPermissionsGranted) {
            boolean isKnoxLicenceActivated = KnoxUtil.getInstance().isKnoxActivated();

            if (shouldBringCurrentTaskToTheFront() &&
                    ((!isKnoxLicenceActivated && TableOffenderDetailsManager.sharedInstance().isDeviceLocked()) ||
                            (KnoxUtil.getInstance().isInInitializedOffenderFlightMode()))) {
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(closeDialog);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        // prevent from user to turn off the device
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

        isCameFromRegularActivityCode = true;
    }

    private boolean shouldBringCurrentTaskToTheFront() {
        return !isCameFromLauncherActivityCode && !isCameFromRegularActivityCode;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        new LocaleUtil().changeApplicationLanguageIfNeeded();

    }


}
