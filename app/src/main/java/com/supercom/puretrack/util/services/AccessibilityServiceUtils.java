package com.supercom.puretrack.util.services;

import static android.content.Context.WINDOW_SERVICE;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.supercom.puretrack.data.R;


@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilityServiceUtils {

    public static FrameLayout configureAccessibilityUI(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        FrameLayout frameLayout = new FrameLayout(context);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.action_bar, frameLayout);
        windowManager.addView(frameLayout, layoutParams);
        return frameLayout;
    }


    /**
     * Fixed default delay between any 2 actions
     */
    public static void waitBeforeNextAccessibilityAction() {
        try {
            Thread.sleep(600);
        } catch (Exception ignored) {
        }
    }

    /**
     * @param nextActionDelay - We might want to use a custom delay depending on our needs
     */
    public static void waitBeforeNextAccessibilityAction(long nextActionDelay) {
        try {
            Thread.sleep(nextActionDelay);
        } catch (Exception ignored) {
        }
    }

    public static void clearRecentTasks(AccessibilityService accessibilityService) {
        for (int i = 0; i < 10; i++) {
            AccessibilityClickUtils.performScreenClick("Back Press", accessibilityService,862,2345, 300);
        }
    }

    public static void pressBackButtonOnce(AccessibilityService accessibilityService){
        AccessibilityClickUtils.performScreenClick("Back Press", accessibilityService,862,2345, 300);
    }
}
