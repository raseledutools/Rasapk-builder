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
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FocusAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayShowing = false;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long serviceStartTime;

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
        
        // অ্যাপ ইউজেস টাইমের জন্য স্টার্ট টাইম সেট
        SharedPreferences prefs = getSharedPreferences("FocusSettings", Context.MODE_PRIVATE);
        long savedStartTime = prefs.getLong("serviceStartTime", 0);
        if (savedStartTime == 0) {
            serviceStartTime = System.currentTimeMillis();
            prefs.edit().putLong("serviceStartTime", serviceStartTime).apply();
        } else {
            serviceStartTime = savedStartTime;
        }

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
        
        // নোটিফিকেশনে রিয়েল-টাইম (প্রতি মিনিটে) সময় আপডেট করার থ্রেড
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - serviceStartTime;
                long hours = diff / (1000 * 60 * 60);
                long mins = (diff % (1000 * 60 * 60)) / (1000 * 60);
                String timeText = hours + "h " + mins + "m";

                Notification notification = new NotificationCompat.Builder(FocusAccessibilityService.this, CHANNEL_ID)
                        .setContentTitle("RasFocus Pro is Monitoring")
                        .setContentText("Active Time: " + timeText)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                        .setOngoing(true)
                        .build();
                startForeground(1, notification);
                
                handler.postDelayed(this, 60000); // ১ মিনিট পর পর আপডেট
            }
        };
        handler.post(updateTimeRunnable); // প্রথমবার চালু করা
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // SharedPreferences থেকে index.html এর সুইচ এবং কাস্টম কিওয়ার্ড চেক করা
        SharedPreferences prefs = getSharedPreferences("FocusSettings", Context.MODE_PRIVATE);
        boolean isKeywordBlocked = prefs.getBoolean("blockKeywords", false);
        boolean isAdultBlocked = prefs.getBoolean("adultContent", false);
        boolean isShortsReelsBlocked = prefs.getBoolean("blockReelsShorts", false);

        // HTML থেকে পাওয়া কাস্টম কিওয়ার্ড লিস্ট বের করা
        String customKwJson = prefs.getString("customKeywordsList", "[]");
        List<String> customKeywordsList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(customKwJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                customKeywordsList.add(jsonArray.getString(i).toLowerCase());
            }
        } catch (Exception ignored) {}

        // যদি কোনো সুইচই অন না থাকে, তবে সার্ভিস স্ক্যান করবে না
        if (!isKeywordBlocked && !isAdultBlocked && !isShortsReelsBlocked) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // নোড স্ক্যান করে ব্লক করার সিদ্ধান্ত নেওয়া
        if (scanAndBlock(rootNode, isKeywordBlocked, isAdultBlocked, isShortsReelsBlocked, customKeywordsList)) {
            showHadithOverlay();
        }
        rootNode.recycle();
    }

    private boolean scanAndBlock(AccessibilityNodeInfo node, boolean kw, boolean adult, boolean shorts, List<String> customKwList) {
        if (node == null) return false;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();
        String pkg = node.getPackageName() != null ? node.getPackageName().toString() : "";

        String content = (text != null ? text.toString() : "") + " " + (desc != null ? desc.toString() : "");
        content = content.toLowerCase();

        // ১. অ্যাডাল্ট কন্টেন্ট বা ডিফল্ট কিওয়ার্ড চেক
        if (kw || adult) {
            for (String k : badKeywords) {
                if (content.contains(k)) return true;
            }
        }

        // ২. ইউজার ড্যাশবোর্ডে যেসব কাস্টম কিওয়ার্ড দিয়েছে তা চেক (শুধুমাত্র কিওয়ার্ড টগল অন থাকলে)
        if (kw) {
            for (String k : customKwList) {
                if (!k.isEmpty() && content.contains(k)) return true;
            }
        }

        // ৩. ইউটিউব শর্টস এবং ফেসবুক রিলস চেক
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
            if (scanAndBlock(node.getChild(i), kw, adult, shorts, customKwList)) return true;
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
                
                // ১ মিলি-সেকেন্ডের মধ্যে হোম স্ক্রিনে পাঠিয়ে দেওয়া (ফ্লিকারিং বন্ধ করার জন্য)
                performGlobalAction(GLOBAL_ACTION_HOME); 
                
                // ৩ সেকেন্ড পর হাদিসটি মিলিয়ে যাবে
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
