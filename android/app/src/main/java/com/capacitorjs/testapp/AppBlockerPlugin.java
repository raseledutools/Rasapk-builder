package com.capacitorjs.app.testapp;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.List;

@CapacitorPlugin(name = "AppBlocker")
public class AppBlockerPlugin extends Plugin {

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        Context context = getContext();
        boolean hasAccessibility = false;
        boolean hasUsage = false;

        if (context != null) {
            // ১. Accessibility Permission চেক করা
            AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            for (AccessibilityServiceInfo service : enabledServices) {
                if (service.getId().contains(context.getPackageName())) {
                    hasAccessibility = true;
                    break;
                }
            }

            // ২. Usage Access Permission চেক করা
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
            hasUsage = (mode == AppOpsManager.MODE_ALLOWED);
        }

        // ড্যাশবোর্ডে স্ট্যাটাস পাঠানো
        JSObject ret = new JSObject();
        ret.put("accessibility", hasAccessibility);
        ret.put("usage", hasUsage);
        call.resolve(ret);
    }

    @PluginMethod
    public void updateSettings(PluginCall call) {
        // ফ্রন্টএন্ড (HTML) থেকে পাঠানো ডেটা রিসিভ করা
        boolean blockKeywords = call.getBoolean("blockKeywords", false);
        boolean adultContent = call.getBoolean("adultContent", false);
        boolean blockReelsShorts = call.getBoolean("blockReelsShorts", false);
        
        // কাস্টম কিওয়ার্ড লিস্ট রিসিভ করা (ডিফল্ট: খালি লিস্ট "[]")
        String customKeywordsList = call.getString("customKeywordsList", "[]");

        Context context = getContext();
        if (context != null) {
            // ডেটাগুলো অ্যান্ড্রয়েডের SharedPreferences-এ "FocusSettings" ফাইলে সেভ করা
            // যাতে Accessibility Service রিয়েল-টাইমে এগুলো পড়তে পারে
            context.getSharedPreferences("FocusSettings", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("blockKeywords", blockKeywords)
                .putBoolean("adultContent", adultContent)
                .putBoolean("blockReelsShorts", blockReelsShorts)
                .putString("customKeywordsList", customKeywordsList) // কাস্টম শব্দগুলো সেভ করা
                .apply();
        }

        JSObject ret = new JSObject();
        ret.put("status", "Settings Received by Native Android!");
        call.resolve(ret);
    }

    @PluginMethod
    public void requestAccessibility(PluginCall call) {
        // ইউজারের ফোনের Accessibility Settings ওপেন করা
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        call.resolve();
    }

    @PluginMethod
    public void requestUsagePermission(PluginCall call) {
        // ইউজারের ফোনের Usage Access Settings ওপেন করা
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        call.resolve();
    }
}
