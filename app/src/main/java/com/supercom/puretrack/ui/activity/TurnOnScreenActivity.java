package com.supercom.puretrack.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.data.repositories.NetworkRepository;

public class TurnOnScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON );

        setContentView(R.layout.activity_turn_on_screen);

        NetworkRepository.turnOnScreenX();
        NetworkRepository.turnOnScreenZ();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}