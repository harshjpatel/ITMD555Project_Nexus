# ITMD 555 Project Nexus 🚀

## 📌 Overview
Nexus is a community platform, like Reddit, that bridges fragmented social discussions to deliver high-quality information and meaningful discourse.

Users can join or start community pages, create posts on any community, and view posts from others.

Communities include topics like:
Business, Entertainment, Health, Science, Sports, Technology, Gaming, Traveling, MacBook, or any random interest-based groups.

---

## 🧠 Groups & Personalization
Users begin by customizing their post feed through filtration (e.g., selecting/deselecting categories like Creative Design, Retro, etc.).  
This ensures the **Nexus Dashboard** is relevant from the very first login.

---

## 🚀 Current Features
- Like / Dislike posts and comments  
- Thread-based commenting system  
- Share posts with other users  
- Bookmark posts using **“Recaps”** for quick access  
- Search content directly from main feed  
- Community-based feed with filtering options  
- Create and engage across multiple communities  
- Firebase-powered backend (NoSQL database)
- Can see recent post in App widget.
- Cloud Messaging (FCM): Used for push notifications (via MyFirebaseMessagingService).
- AppWidget API: Powers the home screen widget (AppWidgetProvider, RemoteViews).
- WorkManager API: Handles reliable background tasks like the WidgetUpdateWorker.
- AlarmManager API: Specifically used in your PostWidget to bypass the standard 30-minute widget update limit for high-frequency (60s) refreshes.


---

## 🔥 Backend & Firebase Services
- Firebase Authentication (user login & identity management)  
- Cloud Firestore (database for users, posts, likes, comments, etc.)  

---

## 🔌 APIs Used
- Firebase Firestore API
- Firebase Auth API
- Firebase Cloud Messaging (FCM) API
- Firebase Analytics API
- Android AppWidget API
- Android WorkManager API
- Android AlarmManager API
- Glide API
- Google Material Design Components API
- Android Jetpack (AppCompat/ConstraintLayout) APIs
- Custom Share Preview API (Local Python Server)


---

## 📊 Presentation and files
👉 [View Project Presentation](https://github.com/harshjpatel/ITMD555Project_Nexus/blob/main/ITMD_555_HPatel_Project_Nexus_presentation.pdf)
👉 [View Project snapshots file](https://github.com/harshjpatel/ITMD555Project_Nexus/blob/main/H_Patel_FinalProject_snapshots.pdf)
👉 [View Project Code file](https://github.com/harshjpatel/ITMD555Project_Nexus/blob/main/H_Patel_FinalProject_project_code.pdf)

---