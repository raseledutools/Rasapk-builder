package com.capacitorjs.app.testapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.TextView;
import java.util.Arrays;
import java.util.List;

public class FocusAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayShowing = false;

    // আমরা কোন কোন অ্যাপ ব্লক করতে চাই তার তালিকা
    private final List<String> reelsShortsApps = Arrays.asList(
            "com.facebook.katana", // Facebook
            "com.instagram.android", // Instagram
            "com.google.android.youtube" // YouTube
    );

    private final List<String> adultApps = Arrays.asList(
            "com.tinder",
            "com.badoo.mobile",
            // আপনি চাইলে এখানে আরও প্যাকেজ নাম যোগ করতে পারেন
            "com.example.adult" 
    );

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED; // যখনই নতুন স্ক্রিন/অ্যাপ ওপেন হবে
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        this.setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            
            // ড্যাশবোর্ড থেকে সেভ করা সেটিংগুলো পড়া
            SharedPreferences prefs = getSharedPreferences("FocusSettings", Context.MODE_PRIVATE);
            boolean blockReelsShorts = prefs.getBoolean("blockReelsShorts", false);
            boolean blockAdult = prefs.getBoolean("adultContent", false);

            boolean shouldBlock = false;
            String warningMessage = "Focus Mode is ON!";

            // লজিক ১: Reels/Shorts ব্লক
            if (blockReelsShorts && reelsShortsApps.contains(packageName)) {
                shouldBlock = true;
                warningMessage = "Social Media is Blocked for Now!";
            }

            // লজিক ২: Adult Apps ব্লক
            if (blockAdult && adultApps.contains(packageName)) {
                shouldBlock = true;
                warningMessage = "This content is Restricted!";
            }

            // যদি ব্লক করার কন্ডিশন মিলে যায়
            if (shouldBlock) {
                showBlockOverlay(warningMessage);
            } else {
                hideBlockOverlay();
            }
        }
    }

    private void showBlockOverlay(String message) {
        if (isOverlayShowing) return;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // ওভারলে স্ক্রিনের ডিজাইন তৈরি (Java কোড দিয়ে সিম্পল UI)
        overlayView = new View(this);
        overlayView.setBackgroundColor(0xFA0F172A); // গাঢ় নেভি ব্লু/ডার্ক থিম কালার
        
        // এখানে আমরা একটি ফুল-স্ক্রিন ব্লক তৈরি করছি
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, // স্ক্রিনের একদম ওপরে বসবে
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        try {
            windowManager.addView(overlayView, params);
            isOverlayShowing = true;
            
            // ইউজারের মনোযোগ সরাতে তাকে সরাসরি হোম স্ক্রিনে পাঠিয়ে দেওয়া
            performGlobalAction(GLOBAL_ACTION_HOME); 
            
            // একটু পর ওভারলে সরিয়ে নেওয়া
            overlayView.postDelayed(this::hideBlockOverlay, 2000); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideBlockOverlay() {
        if (isOverlayShowing && windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
            isOverlayShowing = false;
        }
    }

    @Override
    public void onInterrupt() {
        // সার্ভিস কোনো কারণে বাধাগ্রস্ত হলে
    }
}
