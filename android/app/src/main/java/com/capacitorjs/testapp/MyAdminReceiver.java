package com.capacitorjs.app.testapp; // আপনার প্যাকেজের নাম যদি ভিন্ন হয়, তবে এখানে ঠিক করে দেবেন

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, "RasFocus Device Admin Enabled. Uninstall Protection Active.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        // কেউ অ্যাডমিন ডিজেবল করতে চাইলে স্ক্রিনে এই ওয়ার্নিং মেসেজটি দেখাবে
        return "আপনি কি নিশ্চিত? এটি বন্ধ করলে RasFocus এর আনইনস্টল প্রটেকশন রিমুভ হয়ে যাবে এবং আপনি ফোকাস হারাবেন।";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Toast.makeText(context, "RasFocus Device Admin Disabled.", Toast.LENGTH_SHORT).show();
    }
}
