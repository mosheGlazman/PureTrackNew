package com.supercom.puretrack.data.service;


import android.os.Build;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.supercom.puretrack.data.R;
import com.supercom.puretrack.util.custom_implementations.AbstractAccessibilityService;
import com.supercom.puretrack.util.services.AccessibilityCombinationsA31Android10;
import com.supercom.puretrack.util.services.AccessibilityFinlandA31;
import com.supercom.puretrack.util.services.AccessibilityServiceUtils;
import com.supercom.puretrack.util.services.AccessibilitySwipeUtils;

@RequiresApi(api = Build.VERSION_CODES.N)
public class DeviceConfigurationAccessibilityService extends AbstractAccessibilityService {

    //Class Variables - Views
    private Button customActionButton;
    private Button fullConfigurationButton;
    private Button currentConfigurationButton;

    @Override
    protected void onServiceConnected() {

        initViews();
        initListeners();
    }

    private void initViews() {
        FrameLayout rootUILayout = AccessibilityServiceUtils.configureAccessibilityUI(this);
        customActionButton = rootUILayout.findViewById(R.id.custom_action);
        fullConfigurationButton = rootUILayout.findViewById(R.id.fullConfiguration);
        currentConfigurationButton = rootUILayout.findViewById(R.id.currentConfig);
    }

    private void initListeners() {
        customActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AccessibilityScrollUtils.performScroll(DeviceConfigurationAccessibilityService.this);
                AccessibilitySwipeUtils.swipeListDown(DeviceConfigurationAccessibilityService.this,
                        1000, 1000, 1000, 700, 100);
            }
        });
        fullConfigurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessibilityFinlandA31.performFullAccessibilityCombinations(DeviceConfigurationAccessibilityService.this);
            }
        });
        currentConfigurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccessibilityCombinationsA31Android10.setLockScreenToNone(DeviceConfigurationAccessibilityService.this);

            }
        });
    }
}
