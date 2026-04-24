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

    // ব্যাকগ্রাউন্ডে কাজ করার জন্য Executor (যাতে অ্যাপ ক্র্যাশ না করে)
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    // ডেটা লোডের জন্য ভেরিয়েবল
    private boolean isKwOn = false;
    private boolean isAdultOn = false;
    private boolean isShortsOn = false;
    private List<String> activeCustomKeywords = new ArrayList<>();

    private final List<String> quotes = Arrays.asList(
            "“দৃষ্টি অবনত রাখুন এবং চরিত্র হেফাজত করুন।”\n- আল কুরআন",
            "“সময়ের সঠিক ব্যবহারই সফলতার চাবিকাঠি।”",
            "“লজ্জাশীলতা ঈমানের অঙ্গ।”\n- হাদিস",
            "“আজকের ত্যাগের বিনিময়ে আগামীকালের সফলতা আসবে।”",
            "“বড় কিছু পেতে হলে ছোট আনন্দগুলো ত্যাগ করতে হয়।”"
    );

    private final List<String> defaultAdultKeywords = Arrays.asList(
            "porn", "xxx", "sex", "nude", "xvideos", "pornhub", "xnxx", "choti", "panu", "চটি", "পর্ণ"
    );

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("FocusSettings", Context.MODE_PRIVATE);
        
        long savedStartTime = prefs.getLong("serviceStartTime", 0);
        if (savedStartTime == 0) {
            serviceStartTime = System.currentTimeMillis();
            prefs.edit().putLong("serviceStartTime", serviceStartTime).apply();
        } else {
            serviceStartTime = savedStartTime;
        }

        setupForegroundService();
        loadSettingsFromMemory(prefs);

        // লিসেনার সেট করা যাতে ড্যাশবোর্ড থেকে আপডেট হলে সাথে সাথে লোড হয়
        prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            loadSettingsFromMemory(sharedPreferences);
        });

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

    private void loadSettingsFromMemory(SharedPreferences prefs) {
        isKwOn = prefs.getBoolean("blockKeywords", false);
        isAdultOn = prefs.getBoolean("adultContent", false);
        isShortsOn = prefs.getBoolean("blockReelsShorts", false);

        String customKwJson = prefs.getString("customKeywordsList", "[]");
        activeCustomKeywords.clear();
        try {
            JSONArray jsonArray = new JSONArray(customKwJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                activeCustomKeywords.add(jsonArray.getString(i).toLowerCase());
            }
        } catch (Exception ignored) {}
    }

    private void setupForegroundService() {
        String CHANNEL_ID = "RasFocus_Monitor";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RasFocus Active Monitoring", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        
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
                
                mainHandler.postDelayed(this, 60000); 
            }
        };
        mainHandler.post(updateTimeRunnable);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isKwOn && !isAdultOn && !isShortsOn) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // ক্র্যাশ রোধ করার জন্য স্ক্যানিংয়ের কাজ ব্যাকগ্রাউন্ড থ্রেডে পাঠানো হলো
        backgroundExecutor.execute(() -> {
            if (scanNodes(rootNode)) {
                mainHandler.post(this::showHadithOverlay);
            }
        });
    }

    private boolean scanNodes(AccessibilityNodeInfo node) {
        if (node == null) return false;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();
        String pkg = node.getPackageName() != null ? node.getPackageName().toString() : "";

        StringBuilder contentBuilder = new StringBuilder();
        if (text != null) contentBuilder.append(text.toString().toLowerCase()).append(" ");
        if (desc != null) contentBuilder.append(desc.toString().toLowerCase());
        
        String content = contentBuilder.toString();

        if (isAdultOn || isKwOn) {
            for (String k : defaultAdultKeywords) {
                if (content.contains(k)) return true;
            }
        }

        if (isKwOn) {
            for (String k : activeCustomKeywords) {
                if (!k.isEmpty() && content.contains(k)) return true;
            }
        }

        if (isShortsOn) {
            if (pkg.equals("com.google.android.youtube") && (content.contains("shorts") || (viewId != null && viewId.contains("shorts")))) return true;
            if (pkg.equals("com.facebook.katana") && (content.contains("reels") || (viewId != null && viewId.contains("reel")))) return true;
        }

        // ম্যাক্সিমাম ডেপথ লিমিট সেট করা (ক্র্যাশ রোধ করতে)
        for (int i = 0; i < node.getChildCount(); i++) {
            if (scanNodes(node.getChild(i))) return true;
        }
        
        return false;
    }

    private void showHadithOverlay() {
        if (isOverlayShowing) return;
        isOverlayShowing = true;

        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            
            LinearLayout layout = new LinearLayout(this);
            layout.setBackgroundColor(Color.parseColor("#F20F172A")); // Premium Dark Blue
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
            
            // অ্যান্টি-ফ্লিকার ট্রিক
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
        backgroundExecutor.shutdown(); // মেমোরি লিক বন্ধ করা
    }

    @Override
    public void onInterrupt() {}
}
