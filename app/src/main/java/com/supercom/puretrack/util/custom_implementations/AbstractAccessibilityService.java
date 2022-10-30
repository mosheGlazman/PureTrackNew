package com.supercom.puretrack.util.custom_implementations;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public abstract class AbstractAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
