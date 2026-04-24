/// <reference types="@capacitor/local-notifications" />
/// <reference types="@capacitor/push-notifications" />
/// <reference types="@capacitor/splash-screen" />

import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.capacitorjs.app.testapp', 
  appName: 'RasFocus',
  webDir: 'dist',
  plugins: {
    CapacitorCookies: {
      enabled: true,
    },
    CapacitorHttp: {
      enabled: true,
    },
    SplashScreen: {
      launchAutoHide: true,       // এটি এখন অটোমেটিক সরে যাবে
      launchShowDuration: 3000,   // ৩ সেকেন্ড পর্যন্ত স্প্ল্যাশ দেখাবে
      backgroundColor: "#020617", // আপনার ডার্ক থিমের সাথে মেলানো কালার
      showSpinner: true,
      androidSpinnerStyle: "large",
      spinnerColor: "#ec4899"
    },
    LocalNotifications: {
      smallIcon: 'ic_stat_icon_config_sample',
      iconColor: '#CE0B7C',
    },
    PushNotifications: {
      presentationOptions: ["alert", "sound"]
    }
  },
};

export default config;
