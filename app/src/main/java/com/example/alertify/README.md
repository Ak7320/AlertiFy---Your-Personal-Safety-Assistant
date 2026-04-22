# 📱 AlertiFy – Your Personal Safety Assistant

AlertiFy is an Android-based safety application designed to provide quick emergency assistance using intelligent triggers like **shake detection**. The app helps users send SOS alerts instantly during critical situations, improving personal safety and response time.

---

## 🚀 Features

Features
🆘 Emergency SOS

Hold-to-trigger SOS button with 3-second countdown
Sends SMS with live location to emergency contacts
Automatically calls emergency services
Starts audio recording in the background

📳 Shake Detection

Shake phone 3 times to trigger SOS
Runs as a foreground service — works when app is closed
Survives device reboots via BootReceiver
Battery optimization exemption for always-on monitoring

🔊 Volume Button SOS

Press volume button 3 times within 1.5 seconds to trigger SOS
Runs as a background foreground service alongside shake detection
Works on lock screen

📞 Fake Call De-escalation

Simulates an incoming call with real ringtone and vibration
Pulsing avatar animation while ringing
Accept → shows active call screen with live call timer
Silent mode aware — respects device ringer settings
Accessible directly from the SOS screen

🗺️ Nearby Hospitals

Finds hospitals within 5km using OpenStreetMap + Overpass API
No API key required — completely free
Interactive map with red markers for hospitals
Tap any hospital in the list to zoom the map to it
Shows Open/Closed status where available

👥 Emergency Contacts

Add and manage personal emergency contacts
Contacts receive SMS with your location during SOS

🚑 Quick Emergency Dial

One-tap calling for Ambulance (108), Police (112), Fire (101)
Cards on main menu for instant access

🔐 Authentication

Firebase email/password login and signup
Session persistence with DataStore — no login required after first sign-in
Logout with confirmation dialog
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
- **Location Services:** Google Play Services Location
- **Maps:** OSMDroid (OpenStreetMap)
- **Hospital Data:** Overpass API

### ⚙️ Background & System Services
- **Background Services:** Android Foreground Services
- **Sensors:** SensorManager (Accelerometer for Shake Detection)

---

## ⚙️ How It Works

1. The app runs a background service to monitor motion.
2. When a strong shake is detected using accelerometer data:
    - The app triggers an SOS event
    - Opens emergency screen / sends alerts
3. Ensures quick response without manual interaction.

---

## 📂 Project Structure
com.example.alertify
├── MainActivity.kt
├── data/
│   ├── model/
│   │   ├── DeviceContact.kt         # Device contact data model
│   │   ├── EmergencyContact.kt      # Emergency contact data model
│   │   └── User.kt                  # User data model
│   └── repository/
│       ├── AuthRepository.kt        # Firebase auth operations
│       └── ContactRepository.kt     # Contact CRUD operations
├── receiver/
│   └── BootReceiver.kt              # Restarts services on device reboot
├── service/
│   ├── RecordingService.kt          # Audio recording foreground service
│   ├── ShakeService.kt              # Shake detection foreground service
│   └── VolumeButtonService.kt       # Volume button SOS foreground service
├── ui/
│   ├── components/
│   │   └── SlidingCancelButton.kt   # Slide-to-cancel SOS component
│   ├── navigation/
│   │   ├── NavGraph.kt              # Navigation host and routes
│   │   └── Routes.kt                # Route constants
│   ├── screens/
│   │   ├── contacts/
│   │   │   ├── ContactManagementScreen.kt
│   │   │   └── ContactViewModel.kt
│   │   ├── fakecall/
│   │   │   └── FakeCallScreen.kt    # Fake call de-escalation UI
│   │   ├── hospitals/
│   │   │   └── NearbyHospitalsScreen.kt  # OSM map + hospital list
│   │   ├── login/
│   │   │   ├── LoginScreen.kt
│   │   │   ├── LoginViewModel.kt
│   │   │   └── LoginViewModelFactory.kt
│   │   ├── main/
│   │   │   └── MainMenuScreen.kt    # Home screen with emergency cards
│   │   ├── signup/
│   │   │   ├── SignupScreen.kt
│   │   │   ├── SignupViewModel.kt
│   │   │   └── SignupViewModelFactory.kt
│   │   └── sos/
│   │       ├── SosScreen.kt         # Hold-to-trigger SOS screen
│   │       └── SosViewModel.kt
│   └── theme/
│       ├── AlertifyTheme.kt
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── utils/
├── AudioRecorder.kt             # Audio recording helper
├── LocationHelper.kt            # Location fetching helper
├── SessionManager.kt            # DataStore login persistence
├── ShakeDetector.kt             # Shake gesture algorithm
├── SosManager.kt                # SOS trigger coordinator
└── UiState.kt                   # Shared UI state sealed class

