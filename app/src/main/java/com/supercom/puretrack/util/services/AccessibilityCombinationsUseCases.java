package com.supercom.puretrack.util.services;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.supercom.puretrack.model.business_logic_models.service.DeviceLanguage;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilityCombinationsUseCases {

    public static void clickAddLanguageButton(DeviceLanguage deviceLanguage, AccessibilityService accessibilityService) {
        switch (deviceLanguage) {
            case SWEDISH:
                AccessibilityClickUtils.performScreenClick("Add language", accessibilityService, 450, 830, 300);
                break;
            case FINNISH:
                AccessibilityClickUtils.performScreenClick("Add language", accessibilityService, 522, 1027, 300);
                break;
        }
    }

    public static void clickSvenskaRegion(DeviceLanguage deviceLanguage, AccessibilityService accessibilityService) {
        switch (deviceLanguage) {
            case SWEDISH:
                AccessibilityClickUtils.performScreenClick("Svenska - Finland region", accessibilityService, 458, 752, 600);
                break;
            case FINNISH:
                AccessibilityClickUtils.performScreenClick("Svenska - Sweden region", accessibilityService, 184, 425, 600);
                break;
        }
    }


}
