package com.capacitorjs.app.testapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayShowing = false;
    private final Random random = new Random();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    // Quotes List (Bengali + English combined)
    private final List<String> quotes = Arrays.asList(
            "“দৃষ্টি অবনত রাখুন এবং চরিত্র হেফাজত করুন।”\n- আল কুরআন",
            "“সফলতা আসে ফোকাস থেকে, ডিস্ট্রাকশন থেকে নয়।”\nSuccess comes from focus, not from distraction.",
            "“আজকের সময় নষ্ট মানে, কালকের স্বপ্ন নষ্ট।”\nWasting time today means ruining tomorrow's dreams.",
            "“বড় কিছু পেতে হলে ছোট আনন্দগুলো ত্যাগ করতে হয়।”\nTo achieve something big, you have to sacrifice small pleasures.",
            "“যে নিজের মনকে নিয়ন্ত্রণ করতে পারে, সে পৃথিবী জয় করতে পারে।”\nHe who can control his mind can conquer the world.",
            "“যেখানে মনোযোগ যায়, সেখানেই শক্তি প্রবাহিত হয়।”\nWhere focus goes, energy flows.",
            "“শৃঙ্খলা হলো এখন তুমি কী চাও এবং জীবনের সবচেয়ে বেশি কী চাও তার মধ্যে বেছে নেওয়া।”\nDiscipline is choosing between what you want now and what you want most.",
            "“তোমার ভবিষ্যৎ তৈরি হয় তুমি আজ কি করছো তার দ্বারা, আগামীকাল নয়।”\nYour future is created by what you do today, not tomorrow.",
            "“স্ক্রিন থেকে চোখ সরান, নিজের লক্ষ্যের দিকে তাকান।”\nTake your eyes off the screen, look at your goals.",
            "“যতক্ষণ না তুমি নিজের উপর গর্ববোধ করছো, থেমো না।”\nDon't stop until you're proud.",
            "“সাময়িক সুখের জন্য দীর্ঘমেয়াদী লক্ষ্য নষ্ট করবেন না।”\nDo not trade your long-term goals for temporary pleasure.",
            "“মনোযোগ হলো একটি পেশীর মতো, যত ব্যবহার করবেন তত শক্তিশালী হবে।”\nFocus is like a muscle, the more you use it, the stronger it gets.",
            "“আপনি যা দেখেন, আপনি তাই হয়ে ওঠেন। তাই নিজের চোখকে নিয়ন্ত্রণ করুন।”\nYou become what you consume. Control your eyes."
    );

    // Hardcore Keywords
    private final List<String> hardcoreKeywords = Arrays.asList(
            "porn", "xxx", "sex", "nude", "nsfw", "sexy", "hentai", "rule34", "milf", 
            "blowjob", "tits", "boobs", "pussy", "dick", "cock", "escort", "bdsm", 
            "fetish", "erotica", "dildo", "webcam", "camgirls", "xvideos", "pornhub", 
            "xnxx", "xhamster", "brazzers", "onlyfans", "playboy", "chaturbate", 
            "stripchat", "eporner", "spankbang", "redtube", "youporn", "mia khalifa", 
            "sunny leone", "dani daniels", "johnny sins", "kendra lust",
            "anal", "gangbang", "creampie", "cum", "cumshot", "squirt", "orgasm", "masturbation", 
            "threesome", "orgy", "hardcore", "amateur porn", "uncensored", "bukkake", "deepthroat",
            "stepmom", "step-sister", "shemale", "tranny", "femboy", "gay porn", "lesbian porn",
            "bondage", "sadomasochism", "scat", "gloryhole", "handjob", "footjob", "titjob", "paizuri",
            "doujinshi", "jav", "cuckold", "pov porn", "vr porn", "webcam porn",
            "চটি", "পর্ণ", "সেক্স", "নগ্ন", "উলঙ্গ", "বেশ্যা", "মাগি", "খানকি", 
            "যৌন", "পর্ণগ্রাফি", "রেন্ডি", "চোদাচুতি", "গরম ভিডিও", "খারাপ ছবি",
            "যৌন মিলন", "যৌনাঙ্গ", "চুদো", "নগ্নতা",
            "চটি গল্প", "বাংলা চটি", "চুদাচুদি", "বৌদি", "ভাবি", "গুদ", "ধোন", "স্তন", "দুধ", 
            "মদন", "হস্তমৈথুন", "বীর্য", "ধর্ষণ", "সেক্স ভিডিও", "খারাপ সাইট", "নীল ছবি",
            "কচি মেয়ে", "দেশি মাগি", "মাল", "হট বৌদি",
            "bhabi", "chudai", "bangla choti", "panu", "desi bhabi", "mms", "magi", 
            "choda", "chodachudi", "khanki", "besha", "randi", "nengta", "nangta", 
            "baal", "vodai", "bokachoda", "kuttar bacha", "shuarer bacha", "kharap video",
            "gud", "dhon", "bara", "banchod", "maderchod", "chuida", "chudte", "mutte", 
            "hastamaithun", "birjo", "choti golpo", "desi sex", "boudi", "kachi", "khamcha",
            "khamchi", "hot boudi", "bangla mms", "gandi video", "nangta meye"
    );

    // Romantic & Suggestive Keywords
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

    // Adult Websites
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
        setupNotification(); // Anti-Freezer Shield On

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        // FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY যুক্ত করা হলো ব্রাউজারের ভেতর আরও ডিপ স্ক্যানের জন্য
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        this.setServiceInfo(info);
    }

    // Anti-Freezer Notification Logic
    private void setupNotification() {
        String CHANNEL_ID = "RasSilent_Guard_Ultra";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // IMPORTANCE_HIGH সিস্টেমকে বোঝায় যে এই সার্ভিসটি কিল করা যাবে না
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RasSilent Ultimate Blocker", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("RasSilent Guard is Active")
                .setContentText("Ultimate Protection ON. No Escape Route.")
                .setSmallIcon(android.R.drawable.ic_secure)
                .setOngoing(true) 
                .setPriority(NotificationCompat.PRIORITY_MAX) // হাই-প্রায়োরিটি, ফ্রিজার আটকাতে
                .build();
        startForeground(1005, notification);
    }

    // সার্ভিস যদি কোনো কারণে রিস্টার্ট হয়, তবে যেন আঠার মতো লেগে থাকে
    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        return START_STICKY; 
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        String packageName = event.getPackageName().toString().toLowerCase();

        backgroundExecutor.execute(() -> {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) return;

            boolean shouldBlock = false;

            // ===================================================================
            // ১. THE ULTIMATE SELF-DEFENSE (Settings, Uninstall, Accessibility, Play Store, Launchers)
            // ===================================================================
            boolean isSystemOrStore = packageName.equals("com.android.settings") || 
                                      packageName.equals("com.miui.securitycenter") || 
                                      packageName.contains("packageinstaller") ||
                                      packageName.equals("com.android.vending") || // Google Play Store
                                      packageName.contains("launcher") || // All Launchers (Nova, Default, etc.)
                                      packageName.contains("home") || // Some launchers use 'home' in package name
                                      packageName.contains("sec.android.app"); // Samsung specific apps/launchers

            if (isSystemOrStore) {
                String fullScreenText = extractAllText(rootNode).toLowerCase();
                
                // অ্যাপের নাম স্ক্রিনে আছে কি না?
                boolean isAppOnScreen = fullScreenText.contains("testapp") || 
                                        fullScreenText.contains("rasfocus") || 
                                        fullScreenText.contains("rassilent");
                
                // যদি ইউজার Settings, Play Store বা Launcher এ গিয়ে অ্যাপের নাম স্ক্রিনে আনে
                // তাহলে সরাসরি ব্লক! (দরজা ভেতর থেকে লক)
                if (isAppOnScreen) {
                    shouldBlock = true;
                }
            }

            // ===================================================================
            // ২. PRE-EMPTIVE CONTENT & SHORTS/REELS SCANNING
            // ===================================================================
            if (!shouldBlock) {
                shouldBlock = scanForAdultAndShorts(rootNode, packageName);
            }
            
            if (shouldBlock) {
                mainHandler.post(this::showSafetyOverlay);
            }
            rootNode.recycle();
        });
    }

    // পুরো স্ক্রিনের টেক্সট ম্যাজিকের মতো এক্সট্রাক্ট করা (Lightning Fast)
    private String extractAllText(AccessibilityNodeInfo node) {
        if (node == null) return "";
        StringBuilder sb = new StringBuilder();
        if (node.getText() != null) sb.append(node.getText().toString()).append(" ");
        if (node.getContentDescription() != null) sb.append(node.getContentDescription().toString()).append(" ");
        
        for (int i = 0; i < node.getChildCount(); i++) {
            sb.append(extractAllText(node.getChild(i)));
        }
        return sb.toString();
    }

    // অ্যাডাল্ট কন্টেন্ট এবং শর্টস চেক
    private boolean scanForAdultAndShorts(AccessibilityNodeInfo node, String pkg) {
        if (node == null) return false;
        
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();
        
        String content = (text != null ? text.toString() : "") + " " + (desc != null ? desc.toString() : "");
        content = content.toLowerCase();

        // টাইপ শেষ করার আগেই ধরবে (No Regex, Pure Contains)
        for (String k : hardcoreKeywords) {
            if (content.contains(k)) return true;
        }
        for (String k : romanticKeywords) {
            if (content.contains(k)) return true;
        }
        for (String k : adultWebsites) {
            if (content.contains(k)) return true;
        }

        // Shorts ও Reels ব্লক
        if (pkg.contains("youtube") && (content.contains("shorts") || (viewId != null && viewId.contains("shorts")))) return true;
        if (pkg.contains("facebook") && (content.contains("reels") || (viewId != null && viewId.contains("reel")))) return true;

        for (int i = 0; i < node.getChildCount(); i++) {
            if (scanForAdultAndShorts(node.getChild(i), pkg)) return true;
        }
        return false;
    }

    // ফ্লিকারিং-ফ্রি প্রফেশনাল হাদিস ওভারলে
    private void showSafetyOverlay() {
        if (isOverlayShowing) return;
        isOverlayShowing = true;

        mainHandler.post(() -> {
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
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
                
                // ওভারলে আসার সাথে সাথেই হোমে কিক (No flicker!)
                performGlobalAction(GLOBAL_ACTION_HOME); 
                
                // ৫ সেকেন্ড পর হাদিস রিমুভ হবে
                mainHandler.postDelayed(() -> {
                    if (overlayView != null) {
                        windowManager.removeView(overlayView);
                        overlayView = null;
                        isOverlayShowing = false;
                    }
                }, 5000); 
            } catch (Exception e) { isOverlayShowing = false; }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdown();
    }

    @Override public void onInterrupt() {}
}
