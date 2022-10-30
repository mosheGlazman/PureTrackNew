package com.supercom.puretrack.util.services;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayDeque;
import java.util.Deque;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AccessibilityScrollUtils {

    public static void performScroll(AccessibilityService accessibilityService) {
        AccessibilityNodeInfo scrollable = findScrollableNode(accessibilityService.getRootInActiveWindow());
        if (scrollable != null) {
            scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
        }
        AccessibilityServiceUtils.waitBeforeNextAccessibilityAction();
    }

    public static AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return null;
    }
}
