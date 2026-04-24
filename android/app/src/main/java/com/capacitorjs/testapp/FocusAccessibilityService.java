package com.capacitorjs.app.testapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FocusAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayShowing = false;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // পপআপে দেখানোর জন্য হাদিস ও উক্তি
    private final List<String> quotes = Arrays.asList(
            "“মুমিনদের বলুন, তারা যেন তাদের দৃষ্টি নত রাখে এবং যৌনাঙ্গের হেফাজত করে।”\n- সূরা আন-নূর: ৩০",
            "“এমন দুটি নেয়ামত আছে, যে বিষয়ে অনেক মানুষ ধোঁকার মধ্যে রয়েছে। তা হলো- সুস্থতা এবং অবসর সময়।”\n- সহিহ বুখারি",
            "“লজ্জাশীলতা ঈমানের অঙ্গ।”\n- সহিহ মুসলিম",
            "“আজকের সময় নষ্ট মানে, কালকের স্বপ্ন নষ্ট।”",
            "“বড় কিছু পেতে হলে ছোট আনন্দগুলো ত্যাগ করতে হয়।”",
            "“যে নিজের মনকে নিয়ন্ত্রণ করতে পারে, সে পৃথিবী জয় করতে পারে।”"
    );

    // কিওয়ার্ড এবং ওয়েবসাইটের বিশাল লিস্ট
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
        setupForegroundService(); // নোটিফিকেশন বার একটিভ রাখার জন্য

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED 
                        | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED 
                        | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS 
                   | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS 
                   | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        this.setServiceInfo(info);
    }

    private void setupForegroundService() {
        String CHANNEL_ID = "RasFocus_Monitor";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RasFocus Active Monitoring", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("RasFocus Pro is Monitoring")
                .setContentText("Your focus journey is active...")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setOngoing(true)
                .build();
        startForeground(1, notification);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // SharedPreferences থেকে index.html এর সুইচগুলোর স্ট্যাটাস চেক করা
        SharedPreferences prefs = getSharedPreferences("FocusSettings", Context.MODE_PRIVATE);
        boolean isKeywordBlocked = prefs.getBoolean("blockKeywords", false);
        boolean isAdultBlocked = prefs.getBoolean("adultContent", false);
        boolean isShortsReelsBlocked = prefs.getBoolean("blockReelsShorts", false);

        // যদি কোনো সুইচই অন না থাকে, তবে সার্ভিস কাজ করবে না
        if (!isKeywordBlocked && !isAdultBlocked && !isShortsReelsBlocked) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        if (scanAndBlock(rootNode, isKeywordBlocked, isAdultBlocked, isShortsReelsBlocked)) {
            showHadithOverlay();
        }
        rootNode.recycle();
    }

    private boolean scanAndBlock(AccessibilityNodeInfo node, boolean kw, boolean adult, boolean shorts) {
        if (node == null) return false;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();
        String pkg = node.getPackageName() != null ? node.getPackageName().toString() : "";

        String content = (text != null ? text.toString() : "") + " " + (desc != null ? desc.toString() : "");
        content = content.toLowerCase();

        // ১. কিওয়ার্ড এবং অ্যাডাল্ট কন্টেন্ট চেক (সুইচ অন থাকলে)
        if (kw || adult) {
            for (String k : badKeywords) {
                if (content.contains(k)) return true;
            }
        }

        // ২. ইউটিউব শর্টস এবং ফেসবুক রিলস চেক (সুইচ অন থাকলে)
        if (shorts) {
            if (pkg.equals("com.google.android.youtube")) {
                if (content.contains("shorts") || (viewId != null && viewId.contains("shorts"))) return true;
            }
            if (pkg.equals("com.facebook.katana")) {
                if (content.contains("reels") || (viewId != null && viewId.contains("reel"))) return true;
            }
        }

        // রিকার্সিভলি সব এলিমেন্ট চেক করা
        for (int i = 0; i < node.getChildCount(); i++) {
            if (scanAndBlock(node.getChild(i), kw, adult, shorts)) return true;
        }
        return false;
    }

    private void showHadithOverlay() {
        if (isOverlayShowing) return;
        isOverlayShowing = true;

        handler.post(() -> {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            
            LinearLayout layout = new LinearLayout(this);
            layout.setBackgroundColor(Color.parseColor("#EE0F172A")); // Premium Dark Blue (Transparent)
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(60, 60, 60, 60);

            TextView tv = new TextView(this);
            tv.setText(quotes.get(random.nextInt(quotes.size())));
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(22);
            tv.setGravity(Gravity.CENTER);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setLineSpacing(0, 1.4f);

            layout.addView(tv);
            overlayView = layout;

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            try {
                windowManager.addView(overlayView, params);
                
                // ১ মিলি-সেকেন্ডের মধ্যে হোম স্ক্রিনে পাঠিয়ে দেওয়া (ফ্লিকারিং বন্ধ করার জন্য)
                performGlobalAction(GLOBAL_ACTION_HOME); 
                
                // ৩ সেকেন্ড পর হাদিসটি মিলিয়ে যাবে
                handler.postDelayed(() -> {
                    if (overlayView != null) {
                        windowManager.removeView(overlayView);
                        overlayView = null;
                        isOverlayShowing = false;
                    }
                }, 3000);
            } catch (Exception e) {
                isOverlayShowing = false;
            }
        });
    }

    @Override
    public void onInterrupt() {}
}
