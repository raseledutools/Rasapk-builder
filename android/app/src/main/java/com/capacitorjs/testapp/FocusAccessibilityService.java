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
    
    // টাইপিংয়ের পর অ্যাপ ক্লোজ ঠেকানোর জন্য একটি টাইমার
    private long lastTypingTime = 0; 
    
    private final Random random = new Random();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private final List<String> quotes = Arrays.asList(
            "“মুমিনদের বলুন, তারা যেন তাদের দৃষ্টি নত রাখে এবং যৌনাঙ্গের হেফাজত করে।”\n- (সূরা আন-নূর: ৩০)",
            "“এমন দুটি নেয়ামত আছে, যে বিষয়ে অনেক মানুষ ধোঁকার মধ্যে রয়েছে। তা হলো- সুস্থতা এবং অবসর সময়।”\n- (সহিহ বুখারি)",
            "“লজ্জাশীলতা ঈমানের অঙ্গ।”\n- (সহিহ মুসলিম)",
            "“যে ব্যক্তি মন্দ কাজ থেকে বিরত থাকে, সে যেন ভালো কাজই করল।”",
            "“আল্লাহ তার বান্দার তওবা কবুল করেন যতক্ষণ না তার মৃত্যুযন্ত্রণা শুরু হয়।”",
            "“যে মনকে নিয়ন্ত্রণ করতে পারে মহাশক্তিধর।”\n- (ভগবদ্গীতা)",
            "“কাম, ক্রোধ এবং লোভ—এই তিনটি নরকের দ্বার।”\n- (ভগবদ্গীতা)",
            "“সফলতা আসে ফোকাস থেকে, ডিস্ট্রাকশন থেকে নয়।”",
            "“আজকের সময় নষ্ট মানে, কালকের স্বপ্ন নষ্ট।”",
            "“বড় কিছু পেতে হলে ছোট আনন্দগুলো ত্যাগ করতে হয়।”",
            "“যে নিজের মনকে নিয়ন্ত্রণ করতে পারে, সে পৃথিবী জয় করতে পারে।”"
    );

    private final List<String> hardcoreKeywords = Arrays.asList(
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
            "baal", "vodai", "bokachoda", "kuttar bacha", "shuarer bacha", "kharap video",
            "gud", "dhon", "bara", "banchod", "maderchod", "chuida", "chudte", "mutte", 
            "hastamaithun", "birjo", "choti golpo", "desi sex", "boudi", "kachi", "khamcha",
            "khamchi", "hot boudi", "bangla mms", "gandi video", "nangta meye"
    );

    private final List<String> romanticKeywords = Arrays.asList(
            "hot dance", "seductive dance", "item song", "belly dance", "hot romance", 
            "kissing scene", "bikini", "swimsuit", "sexy dance", "cleavage", "hot scene", 
            "romantic kiss", "bedroom scene", "bath scene", "rain dance", "bold scene", 
            "semi nude", "lingerie", "erotic", "hot song", "romantic video hot", 
            "navel show", "deep neck", "short dress sexy", "unfaithful scene",
            "makeout", "lip lock", "passionate kiss", "hot figure", "figure show", 
            "wet saree", "desi hot", "navel kissing", "thigh show", "micro mini skirt", 
            "transparent dress", "bra", "panty", "thong", "bikini try on", 
            "clothing haul transparent", "yoga pants sexy", "gym shorts hot", "twarking", 
            "booty shake", "nip slip", "wardrobe malfunction", "adult web series", 
            "ullu web series", "altbalaji hot", "kooku", "hot photoshoot", "boudoir",
            "sensual", "steamy", "seduction", "love making", "french kiss", "romantic status", 
            "hot whatsapp status", "first night scene", "suhaag raat", "honeymoon vlog", 
            "couple romance", "deep kiss", "neck kiss", "love bite", "hickey", "open shirt", 
            "shirtless", "backless", "saree draping hot", "saree pallu drop", "hot model", 
            "sexy model", "swimwear", "beachwear", "pool party hot", "hot shower", 
            "steamy shower", "tub bath", "massage hot", "body massage sensual", "try on haul sexy", 
            "plunging neckline", "sideboob", "underboob", "cameltoe", "bouncing", "jiggling", 
            "slow motion hot", "desi boudi hot", "hot aunty", "saree lover", "desi romance", 
            "romantic night", "candlelight romance", "bed romance", "cuddling hot", "intimacy", 
            "intimate scene", "love scene", "passion", "desire", "flirting", "teasing",
            "web series 18+", "18+ natok", "hot short film", "adult short movie", "prime video hot",
            "netflix hot scene", "mx player hot", "ullu app", "primeshots", "hotshots",
            "strip tease", "pole dance", "lap dance", "dirty talk", "roleplay",
            "হট সিন", "রোমান্টিক ভিডিও", "গরম গান", "আইটেম সং", "শাড়ি পরা", "খোলামেলা", 
            "বোল্ড সিন", "রোমান্স", "বাশর রাত", "হানিমুন", "প্রেমের সিন", "চুমু",
            "hot boudi saree", "desi girl hot", "bangla hot song", "kolkata hot actress", 
            "hot natok scene", "bangla bold scene", "basor raat", "ful sojja", 
            "boudi debor romance", "jamai bou romance", "shari pora", "bija shari",
            "chumu khawa", "ador kora", "joriye dhora", "hot dance bangla"
    );

    private final List<String> adultWebsites = Arrays.asList(
            "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com",
            "youporn.com", "beeg.com", "brazzers.com", "spankbang.com", "eporner.com",
            "chaturbate.com", "thumbzilla.com", "txxx.com", "tnaflix.com", "tube8.com",
            "youjizz.com", "motherless.com", "porndig.com", "vporn.com", "drtuber.com",
            "hdporn.com", "pornhd.com", "heavy-r.com", "faproulette.com", "4tube.com",
            "nuvid.com", "pornbox.com", "sunporno.com", "cliphunter.com", "cumlouder.com",
            "avgle.com", "javmost.com", "javfree.me", "missav.com", "7mmtv.tv", "jable.tv",
            "pornpics.com", "imagefap.com", "rule34.xxx", "e621.net",
            "xhamsterlive.com", "bongaCams.com", "livejasmin.com", "cam4.com", "camsoda.com",
            "jerkmate.com", "imlive.com", "anyporn.com", "goodporn.to", "hqporner.com", 
            "empflix.com", "tubev.com", "tubegalore.com", "faphouse.com", "yespornplease.com",
            "pornflip.com", "pornhat.com", "hitomi.la", "nhentai.net", "fakku.net", 
            "gelbooru.com", "sankakucomplex.com", "luscious.net", "porntrex.com", "letdoit.com",
            "porncov.com", "babehub.com", "pornky.com", "3movs.com", "whoreshub.com",
            "bangbros.com", "realitykings.com", "naughtyamerica.com", "mofos.com", "evilangel.com",
            "onlyfans.com", "fansly.com", "manyvids.com", "modelhub.com", "adulttime.com"
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
        String CHANNEL_ID = "RasSilent_Guard_Ultra";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RasSilent Ultimate Blocker", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("RasSilent Guard is Active")
                .setContentText("Ultimate Protection ON. No Escape Route.")
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

        // ১. টাইপিং প্রোটেকশন: ইভেন্ট টাইপ টেক্সট চেঞ্জ হলে সাথে সাথে ক্লিয়ার করবে। 
        // এর ফলে অ্যাপ কখনোই ক্লোজ হবে না।
        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            lastTypingTime = System.currentTimeMillis(); // টাইপ করার সময়টা নোট করে রাখলাম
            SharedPreferences prefs = getSharedPreferences("RasFocusPrefs", MODE_PRIVATE);
            boolean isBlockingActive = prefs.getBoolean("is_blocking_active", true);
            if (isBlockingActive) {
                checkAndClearTyping(event, prefs);
            }
            return; 
        }

        backgroundExecutor.execute(() -> {
            SharedPreferences prefs = getSharedPreferences("RasFocusPrefs", MODE_PRIVATE);
            
            // ===================================================================
            // লজিক 0: "Take a Break" প্রোটেকশন (ফোন অ্যাপ ছাড়া সব লক)
            // ===================================================================
            long breakUntil = prefs.getLong("break_until", 0);
            
            if (System.currentTimeMillis() < breakUntil) {
                boolean isPhoneApp = packageName.contains("dialer") || 
                                     packageName.contains("telecom") || 
                                     packageName.contains("incallui") || 
                                     packageName.contains("contacts") ||
                                     packageName.equals("com.capacitorjs.app.testapp"); 

                if (!isPhoneApp) {
                    mainHandler.post(() -> {
                        performGlobalAction(GLOBAL_ACTION_HOME);
                        showSafetyOverlayCustom("Focus Mode Active!\n\nPlease wait until your break time ends. Stay productive.", false);
                    });
                    return; 
                }
            }

            // মাস্টার টগল চেক
            boolean isBlockingActive = prefs.getBoolean("is_blocking_active", true);
            if (!isBlockingActive) return; 

            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) return;

            // ===================================================================
            // লজিক ৪: Shorts & Reels প্রোটেকশন
            // ===================================================================
            String shortReelType = detectShortsOrReels(rootNode, packageName);
            if (shortReelType != null) {
                mainHandler.post(() -> {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    
                    if (shortReelType.equals("youtube")) {
                        showSmallWarning("Shorts waste your time!");
                    } else if (shortReelType.equals("facebook")) {
                        showSmallWarning("Reels kill you from productivity!");
                    }
                });
                rootNode.recycle();
                return; 
            }

            // ===================================================================
            // লজিক ৩: আনইনস্টল এবং ফ্রিজার অ্যাপ প্রোটেকশন ("Not Responding" লজিক)
            // ===================================================================
            if (checkDangerZoneProtection(rootNode, packageName)) {
                mainHandler.post(() -> {
                    performGlobalAction(GLOBAL_ACTION_HOME);
                    showFakeCrashOverlay(); 
                });
                rootNode.recycle();
                return;
            } 
            
            // ===================================================================
            // লজিক ২: স্ক্রিন এবং ওয়েবসাইট স্ক্যানিং (টাইপিং কুলডাউন সহ)
            // ===================================================================
            // টাইপ করার অন্তত ২ সেকেন্ড পার হলে তবেই স্ক্রিন স্ক্যানার কাজ করবে।
            // এতে করে টাইপ করার সময় সাজেশন আসলে অ্যাপ ক্লোজ হবে না।
            if (!isSettingsApp(packageName) && (System.currentTimeMillis() - lastTypingTime > 2000)) {
                if (scanForHardcoreScreen(rootNode, prefs)) {
                    mainHandler.post(() -> {
                        performGlobalAction(GLOBAL_ACTION_HOME);
                        showSafetyOverlay();
                    });
                }
            }

            rootNode.recycle();
        });
    }

    // =========================================================================
    // হেল্পার মেথডস 
    // =========================================================================

    private String detectShortsOrReels(AccessibilityNodeInfo node, String pkg) {
        if (node == null) return null;

        String viewId = node.getViewIdResourceName() != null ? node.getViewIdResourceName().toLowerCase() : "";
        CharSequence desc = node.getContentDescription();
        String contentDesc = desc != null ? desc.toString().toLowerCase() : "";

        if (pkg.contains("com.google.android.youtube")) {
            if (viewId.contains("reel") || 
                viewId.contains("shorts_player") || 
                viewId.contains("short_video_") || 
                (contentDesc.contains("shorts") && node.isSelected()) || 
                (contentDesc.contains("short") && node.isSelected())) {
                return "youtube";
            }
        } else if (pkg.contains("facebook")) {
            if (viewId.contains("reel_viewer") || 
                viewId.contains("reels_viewer") || 
                viewId.contains("reels_swipe_pager") || 
                viewId.contains("reels_video_player") || 
                viewId.contains("clips_viewer")) {
                return "facebook";
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            String res = detectShortsOrReels(node.getChild(i), pkg);
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

        String[] words = typedText.split("[\\s\\p{Punct}]+"); 
        Set<String> customWords = prefs.getStringSet("blocked_words", new HashSet<>());

        for (String word : words) {
            if (word.isEmpty()) continue;

            for (String k : hardcoreKeywords) {
                if (word.equals(k.toLowerCase())) { matchFound = true; break; }
            }
            if (matchFound) break;

            for (String k : romanticKeywords) {
                if (word.equals(k.toLowerCase())) { matchFound = true; break; }
            }
            if (matchFound) break;

            for (String k : customWords) {
                if (word.equals(k.toLowerCase())) { matchFound = true; break; }
            }
            if (matchFound) break;
        }

        // যদি শব্দ ম্যাচ করে, তবে শুধু লেখা মুছে দেবে। অ্যাপ ক্লোজ করবে না!
        if (matchFound) {
            mainHandler.post(() -> {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                
                String randomHadith = quotes.get(random.nextInt(quotes.size()));
                showSmallWarning(randomHadith); 
            });
        }
        source.recycle();
    }

    private boolean scanForHardcoreScreen(AccessibilityNodeInfo node, SharedPreferences prefs) {
        if (node == null) return false;

        // **ম্যাজিক লাইন:** যদি ইউজার কোনো বক্সে টাইপ করার জন্য ক্লিক করে থাকে, তবে সেই বক্স স্ক্যানার ইগনোর করবে!
        if (node.isEditable()) return false; 

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        
        String content = (text != null ? text.toString().toLowerCase() : "") + " " + 
                         (desc != null ? desc.toString().toLowerCase() : "");

        for (String site : adultWebsites) {
            if (content.contains(site)) return true;
        }

        for (String k : hardcoreKeywords) {
            if (content.contains(" " + k + " ")) return true;
        }

        Set<String> customWords = prefs.getStringSet("blocked_words", new HashSet<>());
        for (String k : customWords) {
            if (content.contains(" " + k.toLowerCase() + " ")) return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (scanForHardcoreScreen(node.getChild(i), prefs)) return true;
        }
        return false;
    }

    private boolean checkDangerZoneProtection(AccessibilityNodeInfo rootNode, String packageName) {
        String fullText = extractAllText(rootNode).toLowerCase();

        boolean isMyAppVisible = fullText.contains("testapp") || 
                                 fullText.contains("rasfocus") || 
                                 fullText.contains("rassilent");

        if (!isMyAppVisible) return false;

        boolean isSettingsDanger = isSettingsApp(packageName) && (
                                   fullText.contains("uninstall") || 
                                   fullText.contains("force stop") || 
                                   fullText.contains("clear data") || 
                                   fullText.contains("app info"));

        boolean isFreezerDanger = fullText.contains("freeze") || 
                                  fullText.contains("hide app") || 
                                  fullText.contains("add app") || 
                                  fullText.contains("add apps") ||
                                  fullText.contains("app hider") ||
                                  fullText.contains("suspend");

        return isSettingsDanger || isFreezerDanger;
    }

    private boolean isSettingsApp(String pkg) {
        return pkg.equals("com.android.settings") || 
               pkg.contains("packageinstaller") || 
               pkg.equals("com.miui.securitycenter");
    }

    private String extractAllText(AccessibilityNodeInfo node) {
        if (node == null) return "";
        StringBuilder sb = new StringBuilder();
        if (node.getText() != null) sb.append(node.getText().toString()).append(" ");
        for (int i = 0; i < node.getChildCount(); i++) {
            sb.append(extractAllText(node.getChild(i)));
        }
        return sb.toString();
    }

    // =========================================================================
    // UI ওভারলে 
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
            } catch (Exception e) { 
                isSmallOverlayShowing = false; 
            }
        });
    }

    private void removeSmallOverlay() {
        if (smallOverlayView != null && isSmallOverlayShowing) {
            try {
                windowManager.removeView(smallOverlayView);
            } catch (Exception ignored) {}
            smallOverlayView = null;
            isSmallOverlayShowing = false;
        }
    }

    private void showFakeCrashOverlay() {
        showSafetyOverlayCustom("System Process Not Responding\n\nPlease wait or close the app.", true);
    }

    private void showSafetyOverlay() {
        showSafetyOverlayCustom(quotes.get(random.nextInt(quotes.size())), false);
    }

    private void showSafetyOverlayCustom(String message, boolean isCrashScreen) {
        if (isOverlayShowing) return;
        isOverlayShowing = true;

        mainHandler.post(() -> {
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            LinearLayout layout = new LinearLayout(this);
            
            if(isCrashScreen) {
                layout.setBackgroundColor(Color.BLACK);
            } else {
                layout.setBackgroundColor(Color.parseColor("#FB0F172A")); 
            }
            
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
                closeButton.setBackgroundColor(Color.parseColor("#E53935")); 
                closeButton.setPadding(40, 20, 40, 20);
                
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
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
            } catch (Exception e) { 
                isOverlayShowing = false; 
            }
        });
    }

    private void removeOverlay() {
        if (overlayView != null && isOverlayShowing) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
            overlayView = null;
            isOverlayShowing = false;
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
