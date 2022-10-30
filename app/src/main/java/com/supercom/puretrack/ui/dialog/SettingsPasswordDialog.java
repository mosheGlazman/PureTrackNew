package com.supercom.puretrack.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager;
import com.supercom.puretrack.data.source.local.table_managers.TableOffenderDetailsManager.OFFENDER_DETAILS_CONS;
import com.supercom.puretrack.data.R;
import com.supercom.puretrack.util.ui.DeveloperUtil;

public class SettingsPasswordDialog extends Dialog {

    private final Context context;

    public interface IUnlockDialogCallbackListener {
        void onTryUnlockWithCorrectPassword();

        void onTryUnlockWithWrongPassword();

        void onBackButtonPressed();
    }

    public SettingsPasswordDialog(Context context) {
        super(context, android.R.style.Theme_Dialog);
        this.context = context;
    }

    public void createUnlockDialog(String enetrPassTitleMsg, final IUnlockDialogCallbackListener iUnlockDialogCallbackListener) {

        setContentView(R.layout.dialog_settings_password);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        final TextView enterTextPassword = findViewById(R.id.enter_password_text);
        enterTextPassword.setText(enetrPassTitleMsg);

        final EditText editTextPassword = findViewById(R.id.editTextPassword);
        editTextPassword.setText(DeveloperUtil.getSettingsDialogPassword());

        Button buttonUnlock = findViewById(R.id.buttonUnlock);
        buttonUnlock.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                String settingsPassword = TableOffenderDetailsManager.sharedInstance().getStringValueByColumnName
                        (OFFENDER_DETAILS_CONS.LAUNCHER_CONFIG_SETTINGS_PASSWORD);
                if (editTextPassword.getText().toString().equals(settingsPassword)) {
                    iUnlockDialogCallbackListener.onTryUnlockWithCorrectPassword();
                    dismiss();
                } else {
                    Toast.makeText(context, "Incorrect Password", Toast.LENGTH_LONG).show();
                    iUnlockDialogCallbackListener.onTryUnlockWithWrongPassword();
                }
            }
        });

        setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
                    Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    context.sendBroadcast(closeDialog);
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    iUnlockDialogCallbackListener.onBackButtonPressed();
                }

                return false;
            }
        });

        show();
    }

}
