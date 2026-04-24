package com.capacitorjs.app.testapp;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // আমাদের নতুন AppBlocker প্লাগিনটি এখানে কানেক্ট করে দিলাম
        registerPlugin(AppBlockerPlugin.class);
    }
}
