package com.capacitorjs.app.testapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import java.util.Arrays;
import java.util.List;

public class FocusAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayShowing = false;

    // টেস্টিংয়ের জন্য সরাসরি এই অ্যাপগুলোর প্যাকেজ নাম দিয়ে দিলাম
    private final List<String> testAppsToBlock = Arrays.asList(
            "com.facebook.katana",      // Facebook
            "com.google.android.youtube", // YouTube
            "com.android.chrome"        // Google Chrome
    );

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        this.setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() == null) return;
            
            String packageName = event.getPackageName().toString();

            // এখানে আর SharedPreferences বা টগল চেক করছি না
            // সরাসরি চেক করছি ওপেন করা অ্যাপটি আমাদের টেস্ট লিস্টে আছে কি না
            if (testAppsToBlock.contains(packageName)) {
                showBlockOverlay();
            } else {
                hideBlockOverlay();
            }
        }
    }

    private void showBlockOverlay() {
        if (isOverlayShowing) return;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = new View(this);
        overlayView.setBackgroundColor(Color.parseColor("#E11D48")); // টেস্ট করার জন্য কড়া লাল রঙ (Danger Red)

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        try {
            windowManager.addView(overlayView, params);
            isOverlayShowing = true;
            
            // সাথে সাথে হোম স্ক্রিনে পাঠিয়ে দেওয়া
            performGlobalAction(GLOBAL_ACTION_HOME); 
            
            // ২ সেকেন্ড পর ওভারলে সরিয়ে নেওয়া
            overlayView.postDelayed(this::hideBlockOverlay, 2000); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideBlockOverlay() {
        if (isOverlayShowing && windowManager != null && overlayView != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
            isOverlayShowing = false;
        }
    }

    @Override
    public void onInterrupt() {}
}
