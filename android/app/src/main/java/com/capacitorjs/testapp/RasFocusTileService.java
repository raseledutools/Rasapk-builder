package com.capacitorjs.app.testapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class RasFocusTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel("RasFocus UI");
            tile.updateTile();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        
        // ControlActivity ওপেন করার জন্য ইন্টেন্ট তৈরি
        Intent intent = new Intent(this, ControlActivity.class);
        // সার্ভিস থেকে অ্যাক্টিভিটি ওপেন করার জন্য NEW_TASK ফ্ল্যাগ দেওয়া বাধ্যতামূলক
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // UI ওপেন করার লজিক (রানেবল)
        Runnable openUI = () -> {
            if (Build.VERSION.SDK_INT >= 34) { // Android 14 বা তার উপরের ভার্সনের জন্য
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this, 
                        0, 
                        intent, 
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                );
                startActivityAndCollapse(pendingIntent);
            } else { // Android 13 বা তার নিচের ভার্সনের জন্য
                startActivityAndCollapse(intent);
            }
        };

        // যদি ইউজারের ফোন লক করা থাকে, তবে আগে পাসওয়ার্ড/ফিঙ্গারপ্রিন্ট চাইবে, তারপর অ্যাপ ওপেন করবে
        if (isLocked()) {
            unlockAndRun(openUI);
        } else {
            openUI.run();
        }
    }
}
