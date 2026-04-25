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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View overlayView; 
    private View smallOverlayView; 
    
    private boolean isOverlayShowing = false;
    private boolean isSmallOverlayShowing = false;
    private long lastTypingTime = 0; 
    
    private final Random random = new Random();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    // ==========================================
    // QUOTES DATABASES (From tab_adult & tab_blocks)
    // ==========================================
    private final List<String> motivationalQuotes = Arrays.asList(
            "সময়ের মূল্য বোঝো, জীবন তোমার মূল্য বুঝবে।",
            "সফলতা আসে ফোকাস থেকে, ডিস্ট্রাকশন থেকে নয়।",
            "আজকের সময় নষ্ট মানে, কালকের স্বপ্ন নষ্ট।",
            "যে নিজের মনকে নিয়ন্ত্রণ করতে পারে, সে পৃথিবী জয় করতে পারে।",
            "বড় কিছু পেতে হলে ছোট আনন্দগুলো ত্যাগ করতে হয়।"
    );

    private final List<String> religiousQuotes = Arrays.asList(
            "“মুমিনদের বলুন, তারা যেন তাদের দৃষ্টি নত রাখে এবং যৌনাঙ্গের হেফাজত করে।” - (সূরা আন-নূর: ৩০)",
            "“লজ্জাশীলতা ঈমানের অঙ্গ।” - (সহিহ মুসলিম)",
            "“যে মনকে নিয়ন্ত্রণ করতে পারে না, তার মন তার সবচেয়ে বড় শত্রু।” - (ভগবদ্গীতা)",
            "“কাম, ক্রোধ এবং লোভ—এই তিনটি নরকের দ্বার।” - (ভগবদ্গীতা)",
            "“খারাপ সাহচর্য ভালো চরিত্র নষ্ট করে।” - (১ করিন্থীয় ১৫:৩৩)"
    );

    // ==========================================
    // KEYWORDS DATABASES
    // ==========================================
    private final List<String> hardcoreKeywords = Arrays.asList(
            "porn", "xxx", "sex", "nude", "nsfw", "sexy", "hentai", "rule34", "milf", 
            "blowjob", "tits", "boobs", "pussy", "dick", "cock", "escort", "bdsm", 
            "xvideos", "pornhub", "xnxx", "xhamster", "brazzers", "onlyfans", "chaturbate",
            "চটি", "পর্ণ", "সেক্স", "নগ্ন", "উলঙ্গ", "বেশ্যা", "মাগি", "খানকি", 
            "যৌন", "পর্ণগ্রাফি", "চোদাচুতি", "bhabi", "chudai", "bangla choti", "panu"
    );

    private final List<String> romanticKeywords = Arrays.asList(
            "hot dance", "seductive dance", "item song", "belly dance", "hot romance", 
            "kissing scene", "bikini", "swimsuit", "sexy dance", "cleavage", "hot scene", 
            "romantic kiss", "bedroom scene", "bath scene", "erotic", "hot song"
    );

    private final List<String> adultWebsites = Arrays.asList(
            "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com",
            "youporn.com", "spankbang.com", "eporner.com", "chaturbate.com"
    );

    private final List<String> browserPackages = Arrays.asList(
            "com.android.chrome", "org.mozilla.firefox", "com.brave.browser", 
            "com.opera.browser", "com.microsoft.emmx", "com.duckduckgo.mobile.android"
    );

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        setupNotification(); 

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | 
                         AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS 
                   | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS 
                   | AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        
        this.setServiceInfo(info);
    }

    private void setupNotification() {
        String CHANNEL_ID = "RasFocus_Ultra_Core";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RasFocus System Core", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("RasFocus Core Active")
                .setContentText("Strict Protocols & AI Filter Running.")
                .setSmallIcon(android.R.drawable.ic_secure)
                .setOngoing(true) 
                .setPriority(NotificationCompat.PRIORITY_MAX) 
                .build();
        startForeground(1005, notification);
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        return START_STICKY; 
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        String packageName = event.getPackageName().toString().toLowerCase();
        int eventType = event.getEventType();

        SharedPreferences prefs = getSharedPreferences("RasFocusPrefs", MODE_PRIVATE);

        // ===================================================================
        // 1. GLOBAL KEYLOGGER (TYPING PROTECTION) - Like WH_KEYBOARD_LL
        // ===================================================================
        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            lastTypingTime = System.currentTimeMillis(); 
            checkAndClearTyping(event, prefs);
            return; 
        }

        backgroundExecutor.execute(() -> {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) return;

            long breakUntil = prefs.getLong("break_until", 0);
            boolean isFocusActive = System.currentTimeMillis() < breakUntil;

            // ===================================================================
            // 2. PANIC LOCKDOWN & APP BLOCKER (tab_strict & tab_blocks)
            // ===================================================================
            if (isFocusActive) {
                boolean isPhoneApp = packageName.contains("dialer") || 
                                     packageName.contains("telecom") || 
                                     packageName.contains("incallui") || 
                                     packageName.contains("contacts") ||
                                     packageName.equals("com.capacitorjs.app.testapp");

                // Panic Mode: Kill Browsers Instantly
                if (browserPackages.contains(packageName)) {
                    triggerGlobalBlockAlert(rootNode, "PANIC LOCKDOWN ACTIVE: Browsers are locked!");
                    return;
                }

                // Custom App Blocker
                Set<String> blockedApps = prefs.getStringSet("blocked_apps", new HashSet<>());
                for(String app : blockedApps) {
                    if(packageName.contains(app.toLowerCase().replace(".exe", ""))) {
                        triggerGlobalBlockAlert(rootNode, "Focus Active: App is blocked.");
                        return;
                    }
                }

                // Full Device Lock (If Not Phone App)
                if (!isPhoneApp && !packageName.equals("com.android.systemui")) {
                    // Allowed apps logic can be expanded here
                }
            }

            // ===================================================================
            // 3. STRICT MODE (ANTI-BYPASS) - tab_strict
            // ===================================================================
            if (checkAntiBypassProtection(rootNode, packageName)) {
                mainHandler.post(() -> {
                    performGlobalAction(GLOBAL_ACTION_HOME);
                    showFakeCrashOverlay(); 
                });
                rootNode.recycle();
                return;
            } 

            // ===================================================================
            // 4. INCOGNITO / PRIVATE TAB BLOCKER - tab_strict
            // ===================================================================
            boolean isIncognitoBlocked = prefs.getBoolean("chkIncognito", false);
            if (isIncognitoBlocked && browserPackages.contains(packageName)) {
                if (detectIncognitoMode(rootNode)) {
                    triggerGlobalBlockAlert(rootNode, "Strict Protocol: Incognito Tabs are Blocked!");
                    return;
                }
            }

            // ===================================================================
            // 5. SHORTS & REELS PROTECTION
            // ===================================================================
            boolean blockReels = prefs.getBoolean("chkReels", true);
            boolean blockShorts = prefs.getBoolean("chkShorts", true);
            
            if (blockReels || blockShorts) {
                String shortReelType = detectShortsOrReels(rootNode, packageName, blockShorts, blockReels);
                if (shortReelType != null) {
                    mainHandler.post(() -> {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        showSmallWarning(shortReelType.equals("youtube") ? "Shorts waste your time!" : "Reels kill productivity!");
                    });
                    rootNode.recycle();
                    return; 
                }
            }

            // ===================================================================
            // 6. SCREEN & URL SCANNING (UIAutomation Alternative)
            // ===================================================================
            if (!isSettingsApp(packageName) && (System.currentTimeMillis() - lastTypingTime > 2000)) {
                if (scanScreenContent(rootNode, prefs)) {
                    triggerGlobalBlockAlert(rootNode, ""); // Show motivational/religious quote
                    return;
                }
            }

            rootNode.recycle();
        });
    }

    // =========================================================================
    // HELPER METHODS (LOCKED LOGICS)
    // =========================================================================

    private void triggerGlobalBlockAlert(AccessibilityNodeInfo rootNode, String customMsg) {
        mainHandler.post(() -> {
            performGlobalAction(GLOBAL_ACTION_HOME);
            if (customMsg.isEmpty()) {
                showSafetyOverlay(); // Shows random quote
            } else {
                showSafetyOverlayCustom(customMsg, false);
            }
        });
        if(rootNode != null) rootNode.recycle();
    }

    private boolean detectIncognitoMode(AccessibilityNodeInfo node) {
        if (node == null) return false;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String content = (text != null ? text.toString().toLowerCase() : "") + " " + 
                         (desc != null ? desc.toString().toLowerCase() : "");

        if (content.contains("incognito") || content.contains("inprivate") || 
            content.contains("private browsing") || content.contains("close all incognito")) {
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (detectIncognitoMode(node.getChild(i))) return true;
        }
        return false;
    }

    private String detectShortsOrReels(AccessibilityNodeInfo node, String pkg, boolean chkShorts, boolean chkReels) {
        if (node == null) return null;
        String viewId = node.getViewIdResourceName() != null ? node.getViewIdResourceName().toLowerCase() : "";
        CharSequence desc = node.getContentDescription();
        String contentDesc = desc != null ? desc.toString().toLowerCase() : "";

        if (chkShorts && pkg.contains("com.google.android.youtube")) {
            if (viewId.contains("reel") || viewId.contains("shorts_player") || 
                viewId.contains("short_video_") || 
                (contentDesc.contains("shorts") && node.isSelected()) || 
                (contentDesc.contains("short") && node.isSelected())) {
                return "youtube";
            }
        } else if (chkReels && pkg.contains("facebook")) {
            if (viewId.contains("reel_viewer") || viewId.contains("reels_viewer") || 
                viewId.contains("reels_swipe_pager") || viewId.contains("reels_video_player") || 
                viewId.contains("clips_viewer")) {
                return "facebook";
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            String res = detectShortsOrReels(node.getChild(i), pkg, chkShorts, chkReels);
            if (res != null) return res;
        }
        return null;
    }

    private void checkAndClearTyping(AccessibilityEvent event, SharedPreferences prefs) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;
        CharSequence rawText = source.getText();
        if (rawText == null) return;
        
        String typedText = rawText.toString().toLowerCase().trim();
        boolean matchFound = false;

        boolean chkHardcore = prefs.getBoolean("chkHardcore", true);
        boolean chkRomantic = prefs.getBoolean("chkRomantic", true);
        Set<String> customWords = prefs.getStringSet("blocked_words", new HashSet<>());

        String[] words = typedText.split("[\\s\\p{Punct}]+"); 

        for (String word : words) {
            if (word.isEmpty()) continue;
            if (chkHardcore) { for (String k : hardcoreKeywords) if (word.equals(k)) matchFound = true; }
            if (!matchFound && chkRomantic) { for (String k : romanticKeywords) if (word.equals(k)) matchFound = true; }
            if (!matchFound) { for (String k : customWords) if (word.equals(k.toLowerCase())) matchFound = true; }
            if (matchFound) break;
        }

        if (matchFound) {
            mainHandler.post(() -> {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                
                String randomHadith = religiousQuotes.get(random.nextInt(religiousQuotes.size()));
                showSmallWarning(randomHadith); 
            });
        }
        source.recycle();
    }

    private boolean scanScreenContent(AccessibilityNodeInfo node, SharedPreferences prefs) {
        if (node == null) return false;
        if (node.isEditable()) return false; 

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String content = (text != null ? text.toString().toLowerCase() : "") + " " + 
                         (desc != null ? desc.toString().toLowerCase() : "");

        boolean chkAdult = prefs.getBoolean("chkAdult", true);
        boolean chkHardcore = prefs.getBoolean("chkHardcore", true);
        Set<String> customWords = prefs.getStringSet("blocked_words", new HashSet<>());

        if (chkAdult) {
            for (String site : adultWebsites) {
                if (content.contains(site)) return true;
            }
        }
        if (chkHardcore) {
            for (String k : hardcoreKeywords) {
                if (content.contains(" " + k + " ")) return true;
            }
        }
        for (String k : customWords) {
            if (content.contains(" " + k.toLowerCase() + " ") || content.contains(k.toLowerCase() + ".com")) return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (scanScreenContent(node.getChild(i), prefs)) return true;
        }
        return false;
    }

    private boolean checkAntiBypassProtection(AccessibilityNodeInfo rootNode, String packageName) {
        String fullText = extractAllText(rootNode).toLowerCase();

        boolean isMyAppVisible = fullText.contains("testapp") || fullText.contains("rasfocus");
        if (!isMyAppVisible) return false;

        boolean isSettingsDanger = isSettingsApp(packageName) && (
                                   fullText.contains("uninstall") || fullText.contains("force stop") || 
                                   fullText.contains("clear data") || fullText.contains("app info"));

        boolean isFreezerDanger = fullText.contains("freeze") || fullText.contains("hide app") || 
                                  fullText.contains("app hider") || fullText.contains("suspend");

        return isSettingsDanger || isFreezerDanger;
    }

    private boolean isSettingsApp(String pkg) {
        return pkg.equals("com.android.settings") || pkg.contains("packageinstaller") || pkg.equals("com.miui.securitycenter");
    }

    private String extractAllText(AccessibilityNodeInfo node) {
        if (node == null) return "";
        StringBuilder sb = new StringBuilder();
        if (node.getText() != null) sb.append(node.getText().toString()).append(" ");
        for (int i = 0; i < node.getChildCount(); i++) sb.append(extractAllText(node.getChild(i)));
        return sb.toString();
    }

    // =========================================================================
    // UI OVERLAYS (POP-UPS)
    // =========================================================================

    private void showSmallWarning(String message) {
        if (isSmallOverlayShowing) return;
        isSmallOverlayShowing = true;

        mainHandler.post(() -> {
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            LinearLayout layout = new LinearLayout(this);
            layout.setBackgroundColor(Color.parseColor("#E6B71C1C")); 
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(30, 30, 30, 30);

            TextView tv = new TextView(this);
            tv.setText(message);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(16); 
            tv.setGravity(Gravity.CENTER);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            layout.addView(tv);

            smallOverlayView = layout;
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL; 

            try {
                windowManager.addView(smallOverlayView, params);
                mainHandler.postDelayed(this::removeSmallOverlay, 3000); 
            } catch (Exception e) { isSmallOverlayShowing = false; }
        });
    }

    private void removeSmallOverlay() {
        if (smallOverlayView != null && isSmallOverlayShowing) {
            try { windowManager.removeView(smallOverlayView); } catch (Exception ignored) {}
            smallOverlayView = null; isSmallOverlayShowing = false;
        }
    }

    private void showFakeCrashOverlay() {
        showSafetyOverlayCustom("System Process Not Responding\n\nPlease wait or close the app.", true);
    }

    private void showSafetyOverlay() {
        // Randomly pick a quote (Mixing Motivational and Religious)
        String quote = random.nextBoolean() ? 
                       motivationalQuotes.get(random.nextInt(motivationalQuotes.size())) : 
                       religiousQuotes.get(random.nextInt(religiousQuotes.size()));
        showSafetyOverlayCustom(quote, false);
    }

    private void showSafetyOverlayCustom(String message, boolean isCrashScreen) {
        if (isOverlayShowing) return;
        isOverlayShowing = true;

        mainHandler.post(() -> {
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            LinearLayout layout = new LinearLayout(this);
            
            if(isCrashScreen) layout.setBackgroundColor(Color.BLACK);
            else layout.setBackgroundColor(Color.parseColor("#FB0F172A")); 
            
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(80, 80, 80, 80);

            TextView tv = new TextView(this);
            tv.setText(message);
            
            if(isCrashScreen) {
                tv.setTextColor(Color.LTGRAY);
                tv.setTextSize(18);
            } else {
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(24);
                tv.setLineSpacing(0, 1.5f);
            }
            
            tv.setGravity(Gravity.CENTER);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            layout.addView(tv);

            if (!isCrashScreen) {
                Button closeButton = new Button(this);
                closeButton.setText("Close / বন্ধ করুন");
                closeButton.setTextColor(Color.WHITE);
                closeButton.setBackgroundColor(Color.parseColor("#0DA4A6")); 
                closeButton.setPadding(40, 20, 40, 20);
                
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                btnParams.setMargins(0, 80, 0, 0); 
                closeButton.setLayoutParams(btnParams);
                closeButton.setOnClickListener(v -> removeOverlay());
                layout.addView(closeButton);
            }

            overlayView = layout;
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);

            try {
                windowManager.addView(overlayView, params);
                mainHandler.postDelayed(this::removeOverlay, 5000); 
            } catch (Exception e) { isOverlayShowing = false; }
        });
    }

    private void removeOverlay() {
        if (overlayView != null && isOverlayShowing) {
            try { windowManager.removeView(overlayView); } catch (Exception ignored) {}
            overlayView = null; isOverlayShowing = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdown();
        removeOverlay();
        removeSmallOverlay();
    }

    @Override public void onInterrupt() {}
}
