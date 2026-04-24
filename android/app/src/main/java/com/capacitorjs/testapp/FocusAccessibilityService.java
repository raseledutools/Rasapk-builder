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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayShowing = false;
    private final Random random = new Random();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long serviceStartTime;
    private NotificationManager notificationManager;

    // ক্র্যাশ রোধ করার জন্য ব্যাকগ্রাউন্ড থ্রেড
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    // পপআপে দেখানোর জন্য হাদিস ও উক্তি
    private final List<String> quotes = Arrays.asList(
            "“দৃষ্টি অবনত রাখুন এবং চরিত্র হেফাজত করুন।”\n- আল কুরআন",
            "“সময়ের সঠিক ব্যবহারই সফলতার চাবিকাঠি।”",
            "“লজ্জাশীলতা ঈমানের অঙ্গ।”\n- হাদিস",
            "“আজকের ত্যাগের বিনিময়ে আগামীকালের সফলতা আসবে।”",
            "“বড় কিছু পেতে হলে ছোট আনন্দগুলো ত্যাগ করতে হয়।”",
            "“যে নিজের মনকে নিয়ন্ত্রণ করতে পারে, সে পৃথিবী জয় করতে পারে।”"
    );

    // ডিফল্ট খারাপ শব্দের লিস্ট
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
        
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        SharedPreferences prefs = getSharedPreferences("FocusSettings", Context.MODE_PRIVATE);
        
        long savedStartTime = prefs.getLong("serviceStartTime", 0);
        if (savedStartTime == 0) {
            serviceStartTime = System.currentTimeMillis();
            prefs.edit().putLong("serviceStartTime", serviceStartTime).apply();
        } else {
            serviceStartTime = savedStartTime;
        }

        setupNotification();

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

    private void setupNotification() {
        String CHANNEL_ID = "RasFocus_Monitor";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RasFocus Monitoring", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - serviceStartTime;
                long hours = diff / (1000 * 60 * 60);
                long mins = (diff % (1000 * 60 * 60)) / (1000 * 60);
                String timeText = hours + "h " + mins + "m";

                Notification notification = new NotificationCompat.Builder(FocusAccessibilityService.this, CHANNEL_ID)
                        .setContentTitle("RasFocus Pro is Active")
                        .setContentText("Monitoring Time: " + timeText)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                        .setOngoing(true) // এই লাইনটি নোটিফিকেশন আটকে রাখবে
                        .build();
                
                // ক্র্যাশ এড়াতে startForeground এর বদলে notify ব্যবহার করা হয়েছে
                notificationManager.notify(1001, notification);
                
                mainHandler.postDelayed(this, 60000); 
            }
        };
        mainHandler.post(updateTimeRunnable);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // মেইন থ্রেড ব্লক এড়াতে সব কাজ ব্যাকগ্রাউন্ডে পাঠানো হলো
        backgroundExecutor.execute(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("FocusSettings", Context.MODE_PRIVATE);
                boolean isKeywordBlocked = prefs.getBoolean("blockKeywords", false);
                boolean isAdultBlocked = prefs.getBoolean("adultContent", false);
                boolean isShortsReelsBlocked = prefs.getBoolean("blockReelsShorts", false);

                if (!isKeywordBlocked && !isAdultBlocked && !isShortsReelsBlocked) return;

                String customKwJson = prefs.getString("customKeywordsList", "[]");
                List<String> customKeywordsList = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(customKwJson);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        customKeywordsList.add(jsonArray.getString(i).toLowerCase());
                    }
                } catch (Exception ignored) {}

                if (scanAndBlock(rootNode, isKeywordBlocked, isAdultBlocked, isShortsReelsBlocked, customKeywordsList)) {
                    mainHandler.post(this::showHadithOverlay);
                }
            } finally {
                rootNode.recycle(); // মেমোরি লিক বন্ধ করতে
            }
        });
    }

    private boolean scanAndBlock(AccessibilityNodeInfo node, boolean kw, boolean adult, boolean shorts, List<String> customKwList) {
        if (node == null) return false;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();
        String pkg = node.getPackageName() != null ? node.getPackageName().toString() : "";

        // StringBuilder ব্যবহার করে মেমোরি ওভারফ্লো ঠেকানো
        StringBuilder contentBuilder = new StringBuilder();
        if (text != null) contentBuilder.append(text.toString().toLowerCase()).append(" ");
        if (desc != null) contentBuilder.append(desc.toString().toLowerCase());
        String content = contentBuilder.toString();

        if (kw || adult) {
            for (String k : badKeywords) {
                if (content.contains(k)) return true;
            }
        }

        if (kw) {
            for (String k : customKwList) {
                if (!k.isEmpty() && content.contains(k)) return true;
            }
        }

        if (shorts) {
            if (pkg.equals("com.google.android.youtube") && (content.contains("shorts") || (viewId != null && viewId.contains("shorts")))) return true;
            if (pkg.equals("com.facebook.katana") && (content.contains("reels") || (viewId != null && viewId.contains("reel")))) return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (scanAndBlock(node.getChild(i), kw, adult, shorts, customKwList)) return true;
        }
        return false;
    }

    private void showHadithOverlay() {
        if (isOverlayShowing) return;
        isOverlayShowing = true;

        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            
            LinearLayout layout = new LinearLayout(this);
            layout.setBackgroundColor(Color.parseColor("#EE0F172A")); 
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

            windowManager.addView(overlayView, params);
            performGlobalAction(GLOBAL_ACTION_HOME); 
            
            mainHandler.postDelayed(() -> {
                if (overlayView != null) {
                    windowManager.removeView(overlayView);
                    overlayView = null;
                    isOverlayShowing = false;
                }
            }, 3000);
        } catch (Exception e) {
            isOverlayShowing = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdown();
    }

    @Override
    public void onInterrupt() {}
}
