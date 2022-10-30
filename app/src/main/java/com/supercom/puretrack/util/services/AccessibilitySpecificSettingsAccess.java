package com.supercom.puretrack.util.services;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilitySpecificSettingsAccess {

    public static void goToLanguageAndInput(AccessibilityService accessibilityService) {
        enterSettingsThroughNotifications(accessibilityService);
        for (int i = 0; i < 5; i++) {
            AccessibilitySwipeUtils.swipeListDown(accessibilityService);
        }
        AccessibilityClickUtils.performScreenClick("General Management", accessibilityService, 496, 1104);
        AccessibilityClickUtils.performScreenClick("Language and input", accessibilityService, 910, 347);
    }

    public static void enterSettingsThroughNotifications(AccessibilityService accessibilityService) {
        AccessibilitySwipeUtils.swipeNotificationsDown(accessibilityService);
        AccessibilityClickUtils.performScreenClick("Notifications Settings Button", accessibilityService, 1000, 210);
    }

}
