# 📚 Vex – Study & Revision Reminder App

Vex is an Android app that keeps students on top of their work by reminding them of
upcoming assignments and topics to revise.  
Built with **Kotlin**, **Jetpack Compose**, and **Firebase**, Vex helps you stay organized and never miss a deadline.

---

## ✨ Features
- **Assignment Tracking** – Add and manage assignments with due dates and notes.
- **Daily Notifications** – Smart reminders for:
  - Assignments due within the next 7 days
  - Evening revision sessions
- **Topics & Revision** – Keep track of topics that need to be revised and mark them as revised.
- **Firebase Integration** – Real-time database storage and syncing across devices.

---

## 🛠️ Tech Stack
| Area | Technology |
|------|-----------|
| Language | **Kotlin** |
| UI | **Jetpack Compose** |
| Backend | **Firebase Realtime Database** |
| Notifications | **WorkManager**, **AlarmManager** (for scheduled reminders) |
| Dependency Injection (optional) | Hilt / Dagger |
| Build Tool | Gradle |

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (latest stable)
- A Firebase project with a Realtime Database configured

### Firebase Setup
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a project and enable **Realtime Database**.
3. Download the `google-services.json` file and place it in:
4. Set appropriate Firebase security rules:
```json
{
  "rules": {
    "assignments": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "topics": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
