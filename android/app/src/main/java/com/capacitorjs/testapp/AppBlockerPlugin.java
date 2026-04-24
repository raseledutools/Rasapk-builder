package com.capacitorjs.app.testapp; // আপনার প্যাকেজ নাম যদি অন্য কিছু হয়, তবে সেটি দেবেন

import android.content.Intent;
import android.provider.Settings;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AppBlocker")
public class AppBlockerPlugin extends Plugin {

    @PluginMethod
    public void updateSettings(PluginCall call) {
        // ফ্রন্টএন্ড থেকে পাঠানো ডেটা রিসিভ করা
        boolean blockKeywords = call.getBoolean("blockKeywords", false);
        boolean adultContent = call.getBoolean("adultContent", false);
        boolean blockReelsShorts = call.getBoolean("blockReelsShorts", false);

        // ডেটাগুলো অ্যান্ড্রয়েডের SharedPreferences (লোকাল মেমোরি)-এ সেভ করা
        // যাতে আমাদের Accessibility Service যেকোনো সময় এগুলো পড়তে পারে
        getContext().getSharedPreferences("FocusSettings", 0)
            .edit()
            .putBoolean("blockKeywords", blockKeywords)
            .putBoolean("adultContent", adultContent)
            .putBoolean("blockReelsShorts", blockReelsShorts)
            .apply();

        // ফ্রন্টএন্ডকে সাকসেস মেসেজ পাঠানো
        JSObject ret = new JSObject();
        ret.put("status", "Settings Received by Native Android!");
        call.resolve(ret);
    }

    @PluginMethod
    public void requestAccessibility(PluginCall call) {
        // ইউজারের ফোনের Accessibility Settings ওপেন করা
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        call.resolve();
    }

    @PluginMethod
    public void requestBatteryOptimization(PluginCall call) {
        // ইউজারের ফোনের Battery Optimization Settings ওপেন করা
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        call.resolve();
    }
}
