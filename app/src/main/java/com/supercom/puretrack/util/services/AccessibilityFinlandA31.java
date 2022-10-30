package com.supercom.puretrack.util.services;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilityFinlandA31 {

    public static void performFullAccessibilityCombinations(AccessibilityService accessibilityService){
        AccessibilityCombinationsA31Android10.addDeviceKeyboardLanguage(accessibilityService);
        AccessibilityCombinationsA31Android10.disableKeyboardToolbarAndSmartTyping(accessibilityService);
        AccessibilityCombinationsA31Android10.setLockScreenToNone(accessibilityService);
    }




}
