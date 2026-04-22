# 📱 AlertiFy – Your Personal Safety Assistant

AlertiFy is an Android-based safety application designed to provide quick emergency assistance using intelligent triggers like **shake detection**. The app helps users send SOS alerts instantly during critical situations, improving personal safety and response time.

---

## 🚀 Features

### 🆘 Emergency SOS
- Hold-to-trigger SOS button with 3-second countdown
- Sends SMS with live location to emergency contacts
- Automatically calls emergency services
- Starts audio recording in the background

### 📳 Shake Detection
- Shake phone 3 times to trigger SOS
- Runs as a foreground service (works even when app is closed)
- Survives device reboots using `BootReceiver`
- Battery optimization exemption for continuous monitoring

### 🔊 Volume Button SOS
- Press volume button 3 times within 1.5 seconds
- Works on lock screen
- Runs alongside shake detection service

### 📞 Fake Call (De-escalation Feature)
- Simulates incoming call with ringtone & vibration
- Animated UI for realistic experience
- Accept → shows live call screen with timer
- Respects silent mode
- Accessible from SOS screen

### 🗺️ Nearby Hospitals
- Fetches hospitals within 5km using OpenStreetMap + Overpass API
- No API key required
- Interactive map with markers
- Tap hospital → zoom on map
- Displays Open/Closed status (if available)

### 👥 Emergency Contacts
- Add/manage emergency contacts
- Contacts receive SOS alerts with live location

### 🚑 Quick Emergency Dial
- One-tap call for:
   - Ambulance (108)
   - Police (112)
   - Fire (101)

### 🔐 Authentication
- Firebase Email/Password login & signup
- Session persistence using DataStore
- Logout with confirmation dialog

---

## 🛠️ Tech Stack

### 📱 Mobile Development
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Navigation:** Navigation Compose
- **Architecture:** MVVM

### 🔐 Authentication & Storage
- **Authentication:** Firebase Authentication
- **Database:** Firebase Firestore
- **Local Storage:** DataStore Preferences

### 📍 Location & Maps
- **Location:** Google Play Services
- **Maps:** OSMDroid (OpenStreetMap)
- **Hospital Data:** Overpass API

### ⚙️ System & Background
- **Foreground Services:** Android Services
- **Sensors:** SensorManager (Accelerometer)

---

## ⚙️ How It Works

1. The app continuously monitors motion using sensors
2. When a shake gesture is detected:
   - SOS is triggered automatically
   - Location is fetched
   - Alerts are sent to emergency contacts
3. Works even when the app is minimized

---

## 📂 Project Structure
com.example.alertify
│
├── data/
│ ├── model/
│ │ ├── DeviceContact.kt
│ │ ├── EmergencyContact.kt
│ │ └── User.kt
│ │
│ └── repository/
│ ├── AuthRepository.kt
│ └── ContactRepository.kt
│
├── receiver/
│ └── BootReceiver.kt
│
├── service/
│ ├── RecordingService.kt
│ ├── ShakeService.kt
│ └── VolumeButtonService.kt
│
├── ui/
│ ├── components/
│ │ └── SlidingCancelButton.kt
│ │
│ ├── navigation/
│ │ ├── NavGraph.kt
│ │ └── Routes.kt
│ │
│ ├── screens/
│ │ ├── contacts/
│ │ ├── fakecall/
│ │ ├── hospitals/
│ │ ├── login/
│ │ ├── main/
│ │ ├── signup/
│ │ └── sos/
│ │
│ └── theme/
│
├── utils/
│ ├── AudioRecorder.kt
│ ├── LocationHelper.kt
│ ├── SessionManager.kt
│ ├── ShakeDetector.kt
│ ├── SosManager.kt
│ └── UiState.kt