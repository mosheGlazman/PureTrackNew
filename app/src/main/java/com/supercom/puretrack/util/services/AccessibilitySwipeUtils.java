package com.supercom.puretrack.util.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilitySwipeUtils {

    public static void swipeNotificationsDown(AccessibilityService accessibilityService) {
        Path swipePath = new Path();
        swipePath.moveTo(1000, 0);
        swipePath.lineTo(1000, 1500);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
        accessibilityService.dispatchGesture(gestureBuilder.build(), null, null);
        AccessibilityServiceUtils.waitBeforeNextAccessibilityAction();
    }

    public static void swipeListDown(AccessibilityService accessibilityService) {
        Path swipePath = new Path();
        swipePath.moveTo(1000, 1000);
        swipePath.lineTo(1000, 200);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 1));
        accessibilityService.dispatchGesture(gestureBuilder.build(), null, null);
        AccessibilityServiceUtils.waitBeforeNextAccessibilityAction(100);
    }

    public static void swipeListDown(AccessibilityService accessibilityService, int originX, int originY, int targetX, int targetY, int duration) {
        Path swipePath = new Path();
        swipePath.moveTo(originX, originY);
        swipePath.lineTo(targetX, targetY);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));
        accessibilityService.dispatchGesture(gestureBuilder.build(), null, null);
        AccessibilityServiceUtils.waitBeforeNextAccessibilityAction(100);
    }

    public static void swipeUp(AccessibilityService accessibilityService) {
        Path swipePath = new Path();
        swipePath.moveTo(1000, 1000);
        swipePath.lineTo(1000, 0);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
        accessibilityService.dispatchGesture(gestureBuilder.build(), null, null);
        AccessibilityServiceUtils.waitBeforeNextAccessibilityAction();
    }

}
