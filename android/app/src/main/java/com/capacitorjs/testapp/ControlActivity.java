package com.capacitorjs.app.testapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class ControlActivity extends Activity {

    private SharedPreferences prefs;
    private EditText keywordInput, breakTimeInput;
    private LinearLayout keywordListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ডায়ালগ স্টাইল এবং ডার্ক থিম নিশ্চিত করা
        setContentView(R.layout.activity_control);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);

        prefs = getSharedPreferences("RasFocusPrefs", MODE_PRIVATE);

        keywordInput = findViewById(R.id.keywordInput);
        breakTimeInput = findViewById(R.id.breakTimeInput);
        keywordListContainer = findViewById(R.id.keywordListContainer);

        // ১. Focus / Take a Break Button
        Button btnStartFocus = findViewById(R.id.btnStartFocus);
        btnStartFocus.setOnClickListener(v -> {
            String timeStr = breakTimeInput.getText().toString().trim();
            if (!timeStr.isEmpty()) {
                long minutes = Long.parseLong(timeStr);
                long breakEndTime = System.currentTimeMillis() + (minutes * 60 * 1000);
                
                prefs.edit().putLong("break_until", breakEndTime).apply();
                Toast.makeText(this, "Focus Mode Started for " + minutes + " minutes!", Toast.LENGTH_LONG).show();
                finish(); // প্যানেল ক্লোজ করে দিবে
            } else {
                Toast.makeText(this, "Please enter break minutes", Toast.LENGTH_SHORT).show();
            }
        });

        // ২. Add Custom Keyword Button
        Button btnAddKeyword = findViewById(R.id.btnAddKeyword);
        btnAddKeyword.setOnClickListener(v -> {
            String word = keywordInput.getText().toString().trim().toLowerCase();
            if (!word.isEmpty()) {
                Set<String> words = new HashSet<>(prefs.getStringSet("blocked_words", new HashSet<>()));
                words.add(word);
                prefs.edit().putStringSet("blocked_words", words).apply();
                
                keywordInput.setText("");
                updateKeywordUI();
                Toast.makeText(this, "Keyword Added!", Toast.LENGTH_SHORT).show();
            }
        });

        // ৩. Clear All Keywords Button
        Button btnClearKeywords = findViewById(R.id.btnClearKeywords);
        btnClearKeywords.setOnClickListener(v -> {
            prefs.edit().remove("blocked_words").apply();
            updateKeywordUI();
            Toast.makeText(this, "All custom keywords cleared!", Toast.LENGTH_SHORT).show();
        });

        // ৪. Close Button
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // UI তে বর্তমান শব্দগুলো দেখাও
        updateKeywordUI();
    }

    private void updateKeywordUI() {
        keywordListContainer.removeAllViews();
        Set<String> words = prefs.getStringSet("blocked_words", new HashSet<>());
        
        if(words.isEmpty()){
            TextView tv = new TextView(this);
            tv.setText("No custom keywords added.");
            tv.setTextColor(Color.LTGRAY);
            keywordListContainer.addView(tv);
            return;
        }

        for (String w : words) {
            TextView tv = new TextView(this);
            tv.setText("🚫 " + w);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(16);
            tv.setPadding(0, 8, 0, 8);
            keywordListContainer.addView(tv);
        }
    }
}
