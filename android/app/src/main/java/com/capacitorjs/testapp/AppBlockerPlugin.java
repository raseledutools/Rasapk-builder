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
            AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            for (AccessibilityServiceInfo service : enabledServices) {
                if (service.getId().contains(context.getPackageName())) {
                    hasAccessibility = true;
                    break;
                }
            }

            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
            hasUsage = (mode == AppOpsManager.MODE_ALLOWED);
        }

        JSObject ret = new JSObject();
        ret.put("accessibility", hasAccessibility);
        ret.put("usage", hasUsage);
        call.resolve(ret);
    }

    @PluginMethod
    public void updateSettings(PluginCall call) {
        boolean blockKeywords = call.getBoolean("blockKeywords", false);
        boolean adultContent = call.getBoolean("adultContent", false);
        boolean blockReelsShorts = call.getBoolean("blockReelsShorts", false);
        String customKeywordsList = call.getString("customKeywordsList", "[]");

        Context context = getContext();
        if (context != null) {
            // getApplicationContext() নিশ্চিত করে যে এটি গ্লোবাল মেমোরি
            context.getApplicationContext().getSharedPreferences("FocusSettings", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("blockKeywords", blockKeywords)
                .putBoolean("adultContent", adultContent)
                .putBoolean("blockReelsShorts", blockReelsShorts)
                .putString("customKeywordsList", customKeywordsList)
                .apply();
        }

        JSObject ret = new JSObject();
        ret.put("status", "Settings Received by Native Android!");
        call.resolve(ret);
    }

    @PluginMethod
    public void requestAccessibility(PluginCall call) {
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
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        call.resolve();
    }
}
