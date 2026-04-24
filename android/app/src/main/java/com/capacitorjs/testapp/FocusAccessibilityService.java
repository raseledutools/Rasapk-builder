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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long serviceStartTime;

    // --- মেমোরি ধরে রাখার জন্য Strong Reference (যাতে UI এর কল মিস না হয়) ---
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    private boolean isKwOn = false;
    private boolean isAdultOn = false;
    private boolean isShortsOn = false;
    private List<String> customKeywordsList = new ArrayList<>();

    private final List<String> quotes = Arrays.asList(
            "“দৃষ্টি অবনত রাখুন এবং চরিত্র হেফাজত করুন।”\n- আল কুরআন",
            "“সময়ের সঠিক ব্যবহারই সফলতার চাবিকাঠি।”",
            "“লজ্জাশীলতা ঈমানের অঙ্গ।”\n- হাদিস",
            "“আজকের ত্যাগের বিনিময়ে আগামীকালের সফলতা আসবে।”",
            "“বড় কিছু পেতে হলে ছোট আনন্দগুলো ত্যাগ করতে হয়।”"
    );

    private final List<String> defaultAdultKeywords = Arrays.asList(
            "porn", "xxx", "sex", "nude", "nsfw", "hentai", "xvideos", "pornhub", "xnxx", "xhamster", "brazzers",
            "choti", "panu", "চটি", "পর্ণ", "সেক্স", "নগ্ন", "উলঙ্গ", "মাগি", "খানকি", "যৌন"
    );

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        prefs = getSharedPreferences("FocusSettings", Context.MODE_PRIVATE);
        
        long savedStartTime = prefs.getLong("serviceStartTime", 0);
        if (savedStartTime == 0) {
            serviceStartTime = System.currentTimeMillis();
            prefs.edit().putLong("serviceStartTime", serviceStartTime).apply();
        } else {
            serviceStartTime = savedStartTime;
        }

        setupNotification();
        loadSettingsFromMemory();

        // অ্যান্ড্রয়েডের Garbage Collector যাতে লিসেনার মুছতে না পারে, তাই গ্লোবাল ভেরিয়েবলে রাখা হলো
        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                loadSettingsFromMemory(); // UI থেকে ডেটা আসামাত্রই আপডেট হবে
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        this.setServiceInfo(info);
    }

    private void loadSettingsFromMemory() {
        isKwOn = prefs.getBoolean("blockKeywords", false);
        isAdultOn = prefs.getBoolean("adultContent", false);
        isShortsOn = prefs.getBoolean("blockReelsShorts", false);
        
        customKeywordsList.clear();
        try {
            String customKwJson = prefs.getString("customKeywordsList", "[]");
            JSONArray jsonArray = new JSONArray(customKwJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                String word = jsonArray.getString(i).toLowerCase().trim();
                if (!word.isEmpty()) customKeywordsList.add(word);
            }
        } catch (Exception ignored) {}
    }

    private void setupNotification() {
        String CHANNEL_ID = "RasFocus_Guard";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RasFocus Persistent Guard", NotificationManager.IMPORTANCE_LOW);
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
                        .setContentTitle("RasFocus Pro is Active")
                        .setContentText("Active Time: " + timeText)
                        .setSmallIcon(android.R.drawable.ic_secure)
                        .setOngoing(true) 
                        .build();
                
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(1002, notification);
                
                mainHandler.postDelayed(this, 60000); 
            }
        };
        mainHandler.post(updateTimeRunnable);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isKwOn && !isAdultOn && !isShortsOn) return;

        if (event.getPackageName() == null) return;
        String packageName = event.getPackageName().toString();

        // Uninstall Protection
        if (packageName.equals("com.android.settings") && (isAdultOn || isKwOn)) {
            showSafetyOverlay();
            return;
        }

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        if (scanRecursive(rootNode, packageName)) {
            showSafetyOverlay();
        }
        
        rootNode.recycle();
    }

    private boolean scanRecursive(AccessibilityNodeInfo node, String pkg) {
        if (node == null) return false;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();
        
        String content = (text != null ? text.toString() : "") + " " + (desc != null ? desc.toString() : "");
        content = content.toLowerCase();

        if (isAdultOn || isKwOn) {
            for (String k : defaultAdultKeywords) {
                if (content.contains(k)) return true;
            }
        }

        if (isKwOn) {
            for (String k : customKeywordsList) {
                if (content.contains(k)) return true;
            }
        }

        if (isShortsOn) {
            if (pkg.contains("youtube") && (content.contains("shorts") || (viewId != null && viewId.contains("shorts")))) return true;
            if (pkg.contains("facebook") && (content.contains("reels") || (viewId != null && viewId.contains("reel")))) return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (scanRecursive(node.getChild(i), pkg)) return true;
        }
        return false;
    }

    private void showSafetyOverlay() {
        if (isOverlayShowing) return;
        isOverlayShowing = true;

        mainHandler.post(() -> {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            LinearLayout layout = new LinearLayout(this);
            layout.setBackgroundColor(Color.parseColor("#FB0F172A")); 
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(80, 80, 80, 80);

            TextView tv = new TextView(this);
            tv.setText(quotes.get(random.nextInt(quotes.size())));
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(24);
            tv.setGravity(Gravity.CENTER);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setLineSpacing(0, 1.5f);

            layout.addView(tv);
            overlayView = layout;

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);

            try {
                windowManager.addView(overlayView, params);
                performGlobalAction(GLOBAL_ACTION_HOME); 
                
                mainHandler.postDelayed(() -> {
                    if (overlayView != null) {
                        windowManager.removeView(overlayView);
                        overlayView = null;
                        isOverlayShowing = false;
                    }
                }, 3500); 
            } catch (Exception e) { isOverlayShowing = false; }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (prefs != null && prefsListener != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
    }

    @Override public void onInterrupt() {}
}
