package com.supercom.puretrack.util.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilityClickUtils {

    /**
     * @param clickedButtonName - Practically not used (could be used for logging),
     *                          though important in order to have a naming path throughout a complex set of
     *                          click combinations so we know exactly what buttons we expect to be clicked.
     */
    public static void performScreenClick(String clickedButtonName, AccessibilityService accessibilityService, int x, int y) {
        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, 1);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        accessibilityService.dispatchGesture(clickBuilder.build(), null, null);
        clickBuilder.build();
        AccessibilityServiceUtils.waitBeforeNextAccessibilityAction();
    }

    public static void performScreenClick(String clickedButtonName, AccessibilityService accessibilityService, int x, int y, long nextActionDelay) {
        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, 1);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        accessibilityService.dispatchGesture(clickBuilder.build(), null, null);
        clickBuilder.build();
        AccessibilityServiceUtils.waitBeforeNextAccessibilityAction(nextActionDelay);
    }
}
