package com.supercom.puretrack.util.services;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.supercom.puretrack.model.business_logic_models.service.DeviceLanguage;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilityCombinationsA31Android10 {

    /**
     * @param deviceLanguage - Selected language to be added to device
     */
    public static void addDeviceLanguage(AccessibilityService accessibilityService, DeviceLanguage deviceLanguage) {
        AccessibilitySpecificSettingsAccess.goToLanguageAndInput(accessibilityService);
        AccessibilityClickUtils.performScreenClick("Language", accessibilityService, 170, 350, 600);
        AccessibilityCombinationsUseCases.clickAddLanguageButton(deviceLanguage, accessibilityService);
        for (int i = 0; i < 3; i++) {
            AccessibilityScrollUtils.performScroll(accessibilityService);
        }
        AccessibilityClickUtils.performScreenClick("Svenska (language)", accessibilityService, 305, 1222, 300);
        AccessibilityCombinationsUseCases.clickSvenskaRegion(deviceLanguage, accessibilityService);
        AccessibilityClickUtils.performScreenClick("Keep current default language", accessibilityService, 313, 2140, 2500);
        AccessibilityServiceUtils.clearRecentTasks(accessibilityService);
        AccessibilityServiceUtils.waitBeforeNextAccessibilityAction(300);
    }

    public static void addDeviceKeyboardLanguage(AccessibilityService accessibilityService) {
        AccessibilitySpecificSettingsAccess.goToLanguageAndInput(accessibilityService);
        AccessibilityClickUtils.performScreenClick("On-screen Keyboard", accessibilityService, 158, 636);
        AccessibilityClickUtils.performScreenClick("Samsung Keyboard", accessibilityService, 144, 659);
        AccessibilityClickUtils.performScreenClick("Languages and Types", accessibilityService, 183, 1048);
        AccessibilityScrollUtils.performScroll(accessibilityService);
        AccessibilityClickUtils.performScreenClick("Manage input Languages", accessibilityService, 220, 525);
        AccessibilityClickUtils.performScreenClick("Search text", accessibilityService, 895, 153);
        AccessibilityClickUtils.performScreenClick("s (keyboard character)", accessibilityService, 259, 1880);
        AccessibilityClickUtils.performScreenClick("v (keyboard character)", accessibilityService, 555, 2027);
        AccessibilityClickUtils.performScreenClick("e (keyboard character)", accessibilityService, 297, 1724, 1000);
        AccessibilityClickUtils.performScreenClick("Download Svenska Language", accessibilityService, 991, 458, 15000);
        AccessibilityClickUtils.performScreenClick("Clear search text", accessibilityService, 981, 175);
        AccessibilityClickUtils.performScreenClick("f (keyboard character)", accessibilityService, 440, 1884);
        AccessibilityClickUtils.performScreenClick("i (keyboard character)", accessibilityService, 791, 1751);
        AccessibilityClickUtils.performScreenClick("n (keyboard character)", accessibilityService, 750, 2004, 1000);
        AccessibilityClickUtils.performScreenClick("Download Suomi Language", accessibilityService, 991, 458, 15000);
        AccessibilityServiceUtils.clearRecentTasks(accessibilityService);
    }

    public static void disableKeyboardToolbarAndSmartTyping(AccessibilityService accessibilityService) {
        AccessibilitySpecificSettingsAccess.goToLanguageAndInput(accessibilityService);
        AccessibilityClickUtils.performScreenClick("On-screen Keyboard", accessibilityService, 158, 636);
        AccessibilityClickUtils.performScreenClick("Samsung Keyboard", accessibilityService, 144, 659);
        AccessibilityClickUtils.performScreenClick("Style and Layout", accessibilityService, 250, 1575);
        AccessibilityClickUtils.performScreenClick("Keyboard toolbar", accessibilityService, 984, 347);
        AccessibilityServiceUtils.pressBackButtonOnce(accessibilityService);
        AccessibilityClickUtils.performScreenClick("Smart typing", accessibilityService, 206, 1239);
        AccessibilityClickUtils.performScreenClick("Suggest Stickers while typing (toggle)", accessibilityService, 968, 803);
        AccessibilityClickUtils.performScreenClick("Auto capitalize (toggle)", accessibilityService, 971, 1400);
        AccessibilityServiceUtils.clearRecentTasks(accessibilityService);
    }

    public static void setLockScreenToNone(AccessibilityService accessibilityService) {
        AccessibilitySpecificSettingsAccess.enterSettingsThroughNotifications(accessibilityService);
        AccessibilitySwipeUtils.swipeListDown(accessibilityService);
        for (int i = 0; i < 2; i++) {
            AccessibilitySwipeUtils.swipeListDown(accessibilityService, 1000, 1000, 1000, 700, 100);
        }
        AccessibilityClickUtils.performScreenClick("Lock Screen", accessibilityService, 206, 1239);
        AccessibilityClickUtils.performScreenClick("None", accessibilityService, 206, 1239);
    }


}
