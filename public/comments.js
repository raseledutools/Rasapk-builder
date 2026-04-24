// comments.js - Universal Real-time Commenting System by Rasel Mia
import { initializeApp, getApps, getApp } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-app.js";
import { getFirestore, collection, addDoc, onSnapshot } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js";
import { getAuth, signInAnonymously } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js";

// 🔥 Firebase Config
const firebaseConfig = {
    apiKey: "AIzaSyDGd3KAo45UuqmeGFALziz_oKm3htEASHY",
    authDomain: "mywebtools-f8d53.firebaseapp.com",
    projectId: "mywebtools-f8d53",
    storageBucket: "mywebtools-f8d53.firebasestorage.app",
    messagingSenderId: "979594414301",
    appId: "1:979594414301:web:7048c995e56e331a85f334"
};

const SYNC_APP_ID = "rasel-mia-universal-sync";

// Firebase initialization
const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();
const db = getFirestore(app);
const auth = getAuth(app);

// Automatic Page Name Detect kora (jemon: "converter", "exam")
let pageName = window.location.pathname.split('/').pop().replace('.html', '');
if (!pageName || pageName === '') pageName = 'index'; // Default home page

const commentsCollectionName = 'comments_' + pageName;
const commentsRef = collection(db, 'artifacts', SYNC_APP_ID, 'public', 'data', commentsCollectionName);

// 🔥 Fix 1: Removed DOMContentLoaded wrapper because type="module" is already deferred
signInAnonymously(auth).then(() => {
    loadComments();
}).catch(err => console.error("Auth Error in Comments:", err));

const submitBtn = document.getElementById('submit-comment');
const commentText = document.getElementById('comment-text');

// Click kore pathanor jonno
if (submitBtn) {
    submitBtn.addEventListener('click', submitCommentHandler);
}

// 🔥 Fix 2: Keyboard er "Enter" chaple jate comment send hoy
if (commentText) {
    commentText.addEventListener('keypress', (e) => {
        // Jodi Enter chape ebong Shift na chape, taholei send hobe
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault(); // Enter chaple jeno notun line toiri na hoy
            submitCommentHandler();
        }
    });
}

// Comment pathanor ashol logic
async function submitCommentHandler() {
    const nameInput = document.getElementById('comment-name').value.trim();
    const textInput = document.getElementById('comment-text').value.trim();
    
    if (!textInput) {
        alert('Doyakore apnar motamot likhun!');
        return;
    }

    const userName = nameInput !== '' ? nameInput : 'Anonymous User';
    
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Pathano hocche...';

    try {
        // Formatting Date
        const displayDateStr = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
        
        await addDoc(commentsRef, {
            name: userName,
            text: textInput,
            timestamp: Date.now(),
            dateStr: displayDateStr
        });

        // Input field faka kora
        document.getElementById('comment-text').value = '';
        submitBtn.innerHTML = '<i class="fa-solid fa-check"></i> Comment Kora Hoyeche!';
        
        setTimeout(() => {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa-regular fa-paper-plane"></i> Pathan';
        }, 2000);

    } catch (error) {
        console.error("Error adding comment: ", error);
        alert("Comment pathate somoshsha hoyeche.");
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fa-regular fa-paper-plane"></i> Pathan';
    }
}

// Real-time Comment Loading
function loadComments() {
    const commentsListDiv = document.getElementById('comments-list');
    if(!commentsListDiv) return;

    onSnapshot(commentsRef, (snapshot) => {
        let comments = [];
        snapshot.forEach((docSnap) => {
            comments.push({ id: docSnap.id, ...docSnap.data() });
        });

        // Notun comment gulo upore dekhanor jonno sort kora
        comments.sort((a, b) => b.timestamp - a.timestamp);

        commentsListDiv.innerHTML = ''; // Clear loading text

        if (comments.length === 0) {
            commentsListDiv.innerHTML = '<p class="text-slate-400 italic text-sm text-center py-6">Ekhono kono comment nei. Apni prothom comment korun!</p>';
            return;
        }

        comments.forEach(comment => {
            // Getting user initials for Avatar
            const initials = comment.name.substring(0, 2).toUpperCase();
            
            commentsListDiv.innerHTML += `
                <div class="bg-slate-50 border border-slate-100 p-4 rounded-xl shadow-sm hover:shadow-md transition-shadow flex gap-4 animate-[pageLoad_0.3s_ease-out]">
                    <div class="w-10 h-10 shrink-0 bg-gradient-to-tr from-pink-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-sm shadow-inner">
                        ${initials}
                    </div>
                    <div class="flex-grow">
                        <div class="flex items-center justify-between mb-1">
                            <h4 class="font-bold text-slate-800 text-sm">${comment.name}</h4>
                            <span class="text-[10px] text-slate-400 bg-slate-200/50 px-2 py-0.5 rounded-full">${comment.dateStr}</span>
                        </div>
                        <p class="text-slate-600 text-sm whitespace-pre-wrap">${comment.text}</p>
                    </div>
                </div>
            `;
        });
    }, (error) => {
        console.error("Error fetching comments:", error);
        commentsListDiv.innerHTML = '<p class="text-red-500 italic text-sm text-center py-4">Database theke comment anate problem hoyeche.</p>';
    });
}
