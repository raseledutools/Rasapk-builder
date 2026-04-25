package com.capacitorjs.app.testapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.HashSet;
import java.util.Set;

public class ControlActivity extends Activity {

    private SharedPreferences prefs;

    // Sidebar Menus
    private TextView menuBlocks, menuAdultBlock;
    // Main Views
    private ScrollView viewBlocks, viewAdultBlock, viewStrictProtocols;
    // Sub Tabs inside Adult Block
    private TextView tabSafeBrowsing1, tabStrictProtocols1;
    private TextView tabSafeBrowsing2, tabStrictProtocols2;
    
    // Drawer Layout
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ================= আসল ফুল-স্ক্রিন মোড (Immersive) =================
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
                      | View.SYSTEM_UI_FLAG_FULLSCREEN 
                      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        // =================================================================

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#0DA4A6")); 

        setContentView(R.layout.activity_control);
        prefs = getSharedPreferences("RasFocusPrefs", MODE_PRIVATE);

        // ================= ড্রয়ার লজিক (Hamburger Menu) =================
        drawerLayout = findViewById(R.id.drawerLayout);
        Button btnMenu = findViewById(R.id.btnMenu);
        
        btnMenu.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // Init Sidebar and Views
        menuBlocks = findViewById(R.id.menuBlocks);
        menuAdultBlock = findViewById(R.id.menuAdultBlock);
        viewBlocks = findViewById(R.id.viewBlocks);
        viewAdultBlock = findViewById(R.id.viewAdultBlock);
        viewStrictProtocols = findViewById(R.id.viewStrictProtocols);

        // Init Sub Tabs
        tabSafeBrowsing1 = findViewById(R.id.tabSafeBrowsing1);
        tabStrictProtocols1 = findViewById(R.id.tabStrictProtocols1);
        tabSafeBrowsing2 = findViewById(R.id.tabSafeBrowsing2);
        tabStrictProtocols2 = findViewById(R.id.tabStrictProtocols2);

        // Sidebar Click Listeners (সাথে ড্রয়ার ক্লোজ করার লজিক)
        menuBlocks.setOnClickListener(v -> {
            switchTab("blocks");
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        menuAdultBlock.setOnClickListener(v -> {
            switchTab("adult");
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Sub Tab Click Listeners
        tabStrictProtocols1.setOnClickListener(v -> switchSubTab("strict"));
        tabSafeBrowsing2.setOnClickListener(v -> switchSubTab("safe"));

        // Close Button
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // Setup Functions
        setupBlocksTab();
        setupAdultBlockTab();
        setupStrictProtocolsTab();

        // Default Tab
        switchTab("blocks");
    }

    private void switchTab(String tab) {
        if (tab.equals("blocks")) {
            menuBlocks.setBackgroundColor(Color.WHITE);
            menuBlocks.setTextColor(Color.parseColor("#0DA4A6"));
            menuBlocks.setTypeface(null, android.graphics.Typeface.BOLD);

            menuAdultBlock.setBackgroundColor(Color.parseColor("#0DA4A6"));
            menuAdultBlock.setTextColor(Color.WHITE);
            menuAdultBlock.setTypeface(null, android.graphics.Typeface.NORMAL);

            viewBlocks.setVisibility(View.VISIBLE);
            viewAdultBlock.setVisibility(View.GONE);
            viewStrictProtocols.setVisibility(View.GONE);

        } else if (tab.equals("adult")) {
            menuAdultBlock.setBackgroundColor(Color.WHITE);
            menuAdultBlock.setTextColor(Color.parseColor("#0DA4A6"));
            menuAdultBlock.setTypeface(null, android.graphics.Typeface.BOLD);

            menuBlocks.setBackgroundColor(Color.parseColor("#0DA4A6"));
            menuBlocks.setTextColor(Color.WHITE);
            menuBlocks.setTypeface(null, android.graphics.Typeface.NORMAL);

            // Default to Safe Browsing sub-tab when entering Adult Block
            switchSubTab("safe");
        }
    }

    private void switchSubTab(String subTab) {
        if (subTab.equals("safe")) {
            viewBlocks.setVisibility(View.GONE);
            viewAdultBlock.setVisibility(View.VISIBLE);
            viewStrictProtocols.setVisibility(View.GONE);
        } else if (subTab.equals("strict")) {
            viewBlocks.setVisibility(View.GONE);
            viewAdultBlock.setVisibility(View.GONE);
            viewStrictProtocols.setVisibility(View.VISIBLE);
        }
    }

    private void setupBlocksTab() {
        EditText breakTimeInput = findViewById(R.id.breakTimeInputBlocks);
        findViewById(R.id.btnStartFocusBlocks).setOnClickListener(v -> {
            String timeStr = breakTimeInput.getText().toString().trim();
            if (!timeStr.isEmpty()) {
                long minutes = Long.parseLong(timeStr);
                prefs.edit().putLong("break_until", System.currentTimeMillis() + (minutes * 60 * 1000)).apply();
                Toast.makeText(this, "Focus Mode Started!", Toast.LENGTH_SHORT).show();
                finish(); 
            }
        });

        // Setup generic list logic
        EditText websiteInput = findViewById(R.id.websiteInput);
        LinearLayout websiteListContainer = findViewById(R.id.websiteListContainer);
        findViewById(R.id.btnAddWebsite).setOnClickListener(v -> {
            addStringToList("blocked_words", websiteInput.getText().toString());
            websiteInput.setText("");
            refreshListUI("blocked_words", websiteListContainer);
        });
        refreshListUI("blocked_words", websiteListContainer);
        
        EditText appInput = findViewById(R.id.appInput);
        LinearLayout appListContainer = findViewById(R.id.appListContainer);
        findViewById(R.id.btnAddApp).setOnClickListener(v -> {
            addStringToList("blocked_apps", appInput.getText().toString());
            appInput.setText("");
            refreshListUI("blocked_apps", appListContainer);
        });
        refreshListUI("blocked_apps", appListContainer);
    }

    private void setupAdultBlockTab() {
        findViewById(R.id.btnStartFocusAdult).setOnClickListener(v -> {
            Toast.makeText(this, "Adult Focus Active!", Toast.LENGTH_SHORT).show();
        });

        EditText keywordInputAdult = findViewById(R.id.keywordInputAdult);
        LinearLayout keywordListContainerAdult = findViewById(R.id.keywordListContainerAdult);
        findViewById(R.id.btnAddKeywordAdult).setOnClickListener(v -> {
            addStringToList("blocked_words", keywordInputAdult.getText().toString());
            keywordInputAdult.setText("");
            refreshListUI("blocked_words", keywordListContainerAdult);
            refreshListUI("blocked_words", findViewById(R.id.websiteListContainer));
        });
        refreshListUI("blocked_words", keywordListContainerAdult);

        // Checkboxes
        CheckBox chkAdult = findViewById(R.id.chkAdult);
        CheckBox chkHardcore = findViewById(R.id.chkHardcore);
        CheckBox chkRomantic = findViewById(R.id.chkRomantic);
        CheckBox chkReels = findViewById(R.id.chkReels);
        CheckBox chkShorts = findViewById(R.id.chkShorts);

        chkAdult.setChecked(prefs.getBoolean("chkAdult", true));
        chkHardcore.setChecked(prefs.getBoolean("chkHardcore", true));
        chkRomantic.setChecked(prefs.getBoolean("chkRomantic", true));
        chkReels.setChecked(prefs.getBoolean("chkReels", true));
        chkShorts.setChecked(prefs.getBoolean("chkShorts", true));

        View.OnClickListener chkListener = v -> {
            prefs.edit()
                .putBoolean("chkAdult", chkAdult.isChecked())
                .putBoolean("chkHardcore", chkHardcore.isChecked())
                .putBoolean("chkRomantic", chkRomantic.isChecked())
                .putBoolean("chkReels", chkReels.isChecked())
                .putBoolean("chkShorts", chkShorts.isChecked())
                .apply();
        };

        chkAdult.setOnClickListener(chkListener);
        chkHardcore.setOnClickListener(chkListener);
        chkRomantic.setOnClickListener(chkListener);
        chkReels.setOnClickListener(chkListener);
        chkShorts.setOnClickListener(chkListener);
    }

    private void setupStrictProtocolsTab() {
        findViewById(R.id.btnStartFocusStrict).setOnClickListener(v -> {
            Toast.makeText(this, "Strict Focus Active!", Toast.LENGTH_SHORT).show();
        });

        // 15 Min Panic Lockdown
        findViewById(R.id.btnPanicLockdown).setOnClickListener(v -> {
            long breakEndTime = System.currentTimeMillis() + (15 * 60 * 1000);
            prefs.edit().putLong("break_until", breakEndTime).apply();
            Toast.makeText(this, "PANIC LOCKDOWN INITIATED FOR 15 MINS!", Toast.LENGTH_LONG).show();
            finish(); 
        });

        // Checkboxes
        CheckBox chkUrlTracking = findViewById(R.id.chkUrlTracking);
        CheckBox chkSafeSearch = findViewById(R.id.chkSafeSearch);
        CheckBox chkStrictMode = findViewById(R.id.chkStrictMode);
        CheckBox chkDnsBlock = findViewById(R.id.chkDnsBlock);
        CheckBox chkIncognito = findViewById(R.id.chkIncognito);

        chkUrlTracking.setChecked(prefs.getBoolean("chkUrlTracking", true));
        chkSafeSearch.setChecked(prefs.getBoolean("chkSafeSearch", true));
        chkStrictMode.setChecked(prefs.getBoolean("chkStrictMode", false));
        chkDnsBlock.setChecked(prefs.getBoolean("chkDnsBlock", false));
        chkIncognito.setChecked(prefs.getBoolean("chkIncognito", false));

        View.OnClickListener chkListener = v -> {
            prefs.edit()
                .putBoolean("chkUrlTracking", chkUrlTracking.isChecked())
                .putBoolean("chkSafeSearch", chkSafeSearch.isChecked())
                .putBoolean("chkStrictMode", chkStrictMode.isChecked())
                .putBoolean("chkDnsBlock", chkDnsBlock.isChecked())
                .putBoolean("chkIncognito", chkIncognito.isChecked())
                .apply();
        };

        chkUrlTracking.setOnClickListener(chkListener);
        chkSafeSearch.setOnClickListener(chkListener);
        chkStrictMode.setOnClickListener(chkListener);
        chkDnsBlock.setOnClickListener(chkListener);
        chkIncognito.setOnClickListener(chkListener);
    }

    // Helpers
    private void addStringToList(String key, String value) {
        if (value.trim().isEmpty()) return;
        Set<String> set = new HashSet<>(prefs.getStringSet(key, new HashSet<>()));
        set.add(value.trim().toLowerCase());
        prefs.edit().putStringSet(key, set).apply();
    }

    private void refreshListUI(String key, LinearLayout container) {
        container.removeAllViews();
        Set<String> items = prefs.getStringSet(key, new HashSet<>());
        for (String item : items) {
            TextView tv = new TextView(this);
            tv.setText("• " + item);
            tv.setTextColor(Color.parseColor("#333333"));
            tv.setTextSize(14);
            tv.setPadding(0, 5, 0, 5);
            container.addView(tv);
        }
    }
}
