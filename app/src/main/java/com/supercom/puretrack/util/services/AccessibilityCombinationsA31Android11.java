package com.supercom.puretrack.util.services;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.supercom.puretrack.model.business_logic_models.service.DeviceLanguage;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilityCombinationsA31Android11 {

    public static void addDeviceLanguage(AccessibilityService accessibilityService, DeviceLanguage deviceLanguage) {
        AccessibilitySwipeUtils.swipeNotificationsDown(accessibilityService);
        AccessibilityClickUtils.performScreenClick("Notifications Settings Button", accessibilityService, 949, 174);
        for (int i = 0; i < 5; i++) {
            AccessibilitySwipeUtils.swipeListDown(accessibilityService);
        }
        AccessibilityClickUtils.performScreenClick("General Management", accessibilityService, 395, 882);
        AccessibilityClickUtils.performScreenClick("Language", accessibilityService, 147, 275, 600);
        AccessibilityCombinationsUseCases.clickAddLanguageButton(deviceLanguage, accessibilityService);
        for (int i = 0; i < 2; i++) {
            AccessibilityScrollUtils.performScroll(accessibilityService);
        }
        AccessibilityClickUtils.performScreenClick("Svenska (language)", accessibilityService, 232, 1924, 300);
        AccessibilityCombinationsUseCases.clickSvenskaRegion(deviceLanguage, accessibilityService);
        AccessibilityClickUtils.performScreenClick("Keep current default language", accessibilityService, 313, 2140, 2500);
        AccessibilityServiceUtils.clearRecentTasks(accessibilityService);
    }






}
