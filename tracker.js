// tracker.js - Universal Traffic Tracking File
import { initializeApp, getApps, getApp } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-app.js";
import { getFirestore, doc, setDoc, increment } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js";
import { getAuth, signInAnonymously } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js";

// 🔥 Aapnar Firebase Config
const firebaseConfig = {
    apiKey: "AIzaSyDGd3KAo45UuqmeGFALziz_oKm3htEASHY",
    authDomain: "mywebtools-f8d53.firebaseapp.com",
    projectId: "mywebtools-f8d53",
    storageBucket: "mywebtools-f8d53.firebasestorage.app",
    messagingSenderId: "979594414301",
    appId: "1:979594414301:web:7048c995e56e331a85f334"
};

const SYNC_APP_ID = "rasel-mia-universal-sync";

// Firebase aage theke initialize kora thakle notun kore korbe na (Error thekanor jonno)
const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();
const db = getFirestore(app);
const auth = getAuth(app);

// Authentication & Tracking
signInAnonymously(auth).then(() => {
    try {
        const tzOffset = (new Date()).getTimezoneOffset() * 60000;
        const todayDate = (new Date(Date.now() - tzOffset)).toISOString().split('T')[0];
        const displayDateStr = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
        
        // Check if already visited today
        const lastVisit = localStorage.getItem('last_visit_log');
        
        if (lastVisit !== todayDate) {
            const trafficRef = doc(db, 'artifacts', SYNC_APP_ID, 'public', 'data', 'traffic_stats', todayDate);
            
            // Count 1 bariye din
            setDoc(trafficRef, {
                displayDate: displayDateStr,
                visits: increment(1)
            }, { merge: true }).then(() => {
                localStorage.setItem('last_visit_log', todayDate);
                console.log("Visit tracked successfully!");
            });
        }
    } catch(e) {
        console.error("Tracking Error:", e);
    }
}).catch(err => console.error("Auth Error:", err));