package com.supercom.puretrack.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.Window;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.util.general.KnoxUtil;
import com.supercom.puretrack.ui.dialog.SettingsPasswordDialog;
import com.supercom.puretrack.ui.dialog.SettingsPasswordDialog.IUnlockDialogCallbackListener;

public class EnterPasswordActivity extends Activity {

    private int wrongPasswordCounterForSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        String titleMsg = getString(R.string.dialog_text_enter_the_password, DatabaseAccess.getInstance().tableDevDetails.getDeviceDetailsRecord().getDeviceSerialNumber());
        SettingsPasswordDialog settingsPasswordDialog = new SettingsPasswordDialog(this);
        settingsPasswordDialog.createUnlockDialog(titleMsg, new IUnlockDialogCallbackListener() {
            @Override
            public void onTryUnlockWithCorrectPassword() {

                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.settingsMenuLoginPerformed, -1, -1);
                wrongPasswordCounterForSettingsButton = 0;


                KnoxUtil.getInstance().enterOfficerMode(false);

                Intent intent = new Intent(LauncherActivity.LAUNCHER_ACTIVITY_MESSAGE_RECEIVER);
                intent.putExtra(LauncherActivity.LAUNCHER_ACTIVITY_EXTRA, LauncherActivity.ENTER_OFFICER_MODE_CLICK_EXTRA);
                LocalBroadcastManager.getInstance(EnterPasswordActivity.this).sendBroadcast(intent);

                finish();
            }

            @Override
            public void onTryUnlockWithWrongPassword() {
                wrongPasswordCounterForSettingsButton++;
                if (wrongPasswordCounterForSettingsButton > 1) {
                    TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.settingsMenuLoginFailure, -1, -1);
                }
            }

            @Override
            public void onBackButtonPressed() {
                finish();
            }
        });
    }

}
