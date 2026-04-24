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

    // আপনার C++ কোড থেকে নেওয়া বিশাল কিওয়ার্ড এবং ওয়েবসাইটের লিস্ট (বাংলা ও ইংরেজি)
    private final List<String> badKeywords = Arrays.asList(
            "porn", "xxx", "sex", "nude", "nsfw", "sexy", "hentai", "rule34", "milf", 
            "blowjob", "tits", "boobs", "pussy", "dick", "cock", "escort", "bdsm", 
            "fetish", "erotica", "dildo", "webcam", "camgirls", "xvideos", "pornhub", 
            "xnxx", "xhamster", "brazzers", "onlyfans", "playboy", "chaturbate", 
            "stripchat", "eporner", "spankbang", "redtube", "youporn", "mia khalifa", 
            "sunny leone", "dani daniels", "johnny sins", "kendra lust",
            "চটি", "পর্ণ", "সেক্স", "নগ্ন", "উলঙ্গ", "বেশ্যা", "মাগি", "খানকি", 
            "যৌন", "পর্ণগ্রাফি", "রেন্ডি", "চোদাচুতি", "গরম ভিডিও", "খারাপ ছবি",
            "যৌন মিলন", "যৌনাঙ্গ", "চুদো", "নগ্নতা",
            "bhabi", "chudai", "bangla choti", "panu", "desi bhabi", "mms", "magi", 
            "choda", "chodachudi", "khanki", "besha", "randi", "nengta", "nangta", 
            "baal", "vodai", "bokachoda", "kuttar bacha", "shuarer bacha",
            "hot dance", "seductive dance", "item song", "belly dance", "hot romance", 
            "kissing scene", "bikini", "swimsuit", "sexy dance", "cleavage", "hot scene", 
            "romantic kiss", "bedroom scene", "bath scene", "rain dance", "bold scene", 
            "semi nude", "lingerie", "erotic", "hot song", "romantic video hot", 
            "navel show", "deep neck", "short dress sexy", "unfaithful scene",
            "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com",
            "brazzers.com", "spankbang.com", "eporner.com", "chaturbate.com"
    );

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        
        // সব ধরনের ইভেন্ট শোনা হবে (ওপেন হওয়া, টাইপ করা, স্ক্রল করা)
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED 
                        | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED 
                        | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                        | AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        this.setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        String packageName = event.getPackageName().toString();

        // ইভেন্ট থেকে সব লেখা (Text এবং Description) একসাথে বের করা
        StringBuilder eventTextBuilder = new StringBuilder();
        if (event.getText() != null) {
            for (CharSequence charSequence : event.getText()) {
                eventTextBuilder.append(charSequence.toString().toLowerCase()).append(" ");
            }
        }
        if (event.getContentDescription() != null) {
            eventTextBuilder.append(event.getContentDescription().toString().toLowerCase());
        }
        
        String eventText = eventTextBuilder.toString();
        if (eventText.isEmpty()) return;

        boolean shouldBlock = false;

        // লজিক ১: যেকোনো জায়গায় খারাপ শব্দ টাইপ বা সার্চ করলে সাথে সাথে ব্লক!
        for (String keyword : badKeywords) {
            if (eventText.contains(keyword)) {
                shouldBlock = true;
                break;
            }
        }

        // লজিক ২: ইউটিউব শর্টস (YouTube Shorts) ব্লক!
        // (ইউটিউব চলবে, কিন্তু Shorts ট্যাবে গেলে বা Shorts লেখা স্ক্রিনে আসলেই ব্লক)
        if (!shouldBlock && packageName.equals("com.google.android.youtube")) {
            if (eventText.contains("shorts") || eventText.contains("short") || eventText.contains("শর্টস")) {
                shouldBlock = true;
            }
        }

        // লজিক ৩: ফেসবুক রিলস (Facebook Reels) ব্লক!
        if (!shouldBlock && packageName.equals("com.facebook.katana")) {
            if (eventText.contains("reels") || eventText.contains("reel") || eventText.contains("রিলস")) {
                shouldBlock = true;
            }
        }

        // ব্লক করার নির্দেশ পেলে লাথি মেরে হোমে পাঠানো
        if (shouldBlock) {
            showBlockOverlay();
        } else {
            hideBlockOverlay();
        }
    }

    private void showBlockOverlay() {
        if (isOverlayShowing) return;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = new View(this);
        overlayView.setBackgroundColor(Color.parseColor("#E11D48")); // কড়া লাল রঙ (Danger Red)

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
            
            // সাথে সাথে হোম স্ক্রিনে পাঠিয়ে দেওয়া
            performGlobalAction(GLOBAL_ACTION_HOME); 
            
            // ২ সেকেন্ড পর ওভারলে সরিয়ে নেওয়া
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
