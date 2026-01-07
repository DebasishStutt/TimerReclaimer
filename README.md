# TimeReclaim - Phone-Free Time Tracker

<div align="center">

**Reclaim your time. Track your phone-free hours.**

A modern Android productivity app that tracks how long your phone stays locked throughout the day.

</div>

## 📱 Features

- **Automatic Time Tracking**: Starts counting the moment you lock your phone
- **Persistent Notification**: Real-time counter visible in your notification shade
- **Daily Email Reports**: Receive a summary of your phone-free time at 23:59 every day
- **Background Tracking**: Continues tracking even when the app is closed
- **Modern UI**: Sleek Material Design 3 interface with gradient themes
- **Privacy-Focused**: All data stored locally on your device

## 🚀 What It Does

TimeReclaim monitors your phone lock/unlock cycles and calculates the total time your phone screen is locked during a 24-hour period (00:00 to 23:59). It helps you understand how much time you're truly disconnected from your device.

### How It Works

1. **Screen Lock Detected** → Counter starts running
2. **Screen Unlocked** → Counter pauses
3. **Screen Locked Again** → Counter resumes from where it stopped
4. **23:59 Daily** → Email report sent with your total phone-free time
5. **Midnight Reset** → Counter resets to 00:00:00 for the new day

## 📋 Requirements

- Android 11 (API 30) or higher
- Android Studio Arctic Fox or newer
- JDK 17

## 🛠️ Installation & Building

### Using Android Studio

1. **Clone or Open the Project**
   ```bash
   cd d:\All_Learnings\Android_Apps\TimeReclaim
   ```

2. **Open in Android Studio**
   - File → Open → Select the TimeReclaim folder
   - Wait for Gradle sync to complete

3. **Build the APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or use: `Build → Make Project` (Ctrl+F9)

4. **Run on Device/Emulator**
   - Connect your Android device via USB with USB debugging enabled
   - Or start an Android emulator (API 30+)
   - Click Run (▶️) or press Shift+F10

### Using Command Line

```bash
# Windows
gradlew.bat assembleDebug

# Unix/Linux/Mac
./gradlew assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## 📦 Installation on Device

### Install via ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Install via File Transfer

1. Copy the APK to your phone
2. Open the file and tap Install
3. Enable "Install from Unknown Sources" if prompted

## 🎯 First-Time Setup

1. **Launch the App**
   - You'll see the onboarding screen

2. **Enter Your Email**
   - Provide the email address where you want to receive daily reports
   - Email validation ensures correct format

3. **Grant Permissions**
   - **Notifications**: Required to show the persistent counter
   - **Battery Optimization**: Disable to ensure reliable background tracking

4. **Start Tracking**
   - The app automatically starts tracking
   - Lock your phone to begin counting

## 💡 Usage

### Viewing Your Stats

- Open the app to see:
  - **Current accumulated time** for today
  - **Lock/Unlock status**
  - **Number of lock sessions**
  - **Your email address** for reports

### Notification Shade

- Swipe down to see your notification shade
- The TimeReclaim notification shows:
  - Current phone-free time (HH:MM:SS)
  - Lock status
- Tap to open the full app

### Daily Email Report

- At 23:59 each day, your email app will open automatically
- Pre-filled email with:
  - Total phone-free time
  - Number of lock sessions
  - Date
- Review and send the email to yourself

## 🏗️ Architecture

### Components

- **OnboardingActivity**: First-run setup with email and permissions
- **MainActivity**: Dashboard showing real-time statistics
- **TrackingService**: Foreground service for background time tracking
- **ScreenStateReceiver**: Detects screen lock/unlock events
- **DailyResetReceiver**: Handles 23:59 alarm for email and reset
- **BootReceiver**: Restarts service after device reboot
- **PreferencesManager**: Centralized data persistence
- **NotificationHelper**: Manages persistent notification

### Technologies

- **Language**: Kotlin
- **UI Framework**: Material Design 3
- **Async**: Kotlin Coroutines
- **Architecture**: Service-based with Broadcast Receivers
- **Storage**: SharedPreferences
- **Scheduling**: AlarmManager

## 🎨 UI/UX Highlights

- **Vibrant Gradient Backgrounds**: Eye-catching color schemes
- **Material Design 3**: Latest design guidelines
- **Dark Mode Support**: Automatic theme switching
- **Smooth Animations**: Polished user experience
- **Readable Typography**: Clear time display

## ⚙️ Permissions Explained

| Permission                             | Purpose                                        |
| -------------------------------------- | ---------------------------------------------- |
| `POST_NOTIFICATIONS`                   | Show persistent notification with time counter |
| `FOREGROUND_SERVICE`                   | Run tracking service in background             |
| `FOREGROUND_SERVICE_SPECIAL_USE`       | Special use foreground service for tracking    |
| `SCHEDULE_EXACT_ALARM`                 | Trigger daily email at exactly 23:59           |
| `RECEIVE_BOOT_COMPLETED`               | Restart tracking after device reboot           |
| `WAKE_LOCK`                            | Keep tracking active during sleep              |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent aggressive battery optimization        |

## 🔧 Troubleshooting

### Service Not Running

- Open the app to restart the service
- Check battery optimization settings
- Ensure app is not force-stopped

### Email Not Sending

- Ensure you have an email app installed
- Check that your email address is correct
- The email intent opens your email app; you must manually send

### Notification Not Showing

- Grant notification permission in app settings
- Check that notifications aren't blocked for TimeReclaim

### Time Not Updating

- Ensure the app isn't killed by battery optimization
- Check that screen lock detection is working (lock and unlock to test)

## 📝 Notes

- **Email Sending**: Uses email intent (opens your email app pre-filled)
- **Background Limitations**: Android 12+ has strict background execution limits
- **Battery Optimization**: Some OEMs may still kill the service despite exemption
- **Time Accuracy**: Depends on proper screen lock detection

## 🤝 Development

### Project Structure

```
TimeReclaim/
├── app/
│   ├── src/main/
│   │   ├── java/com/timereclaim/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── OnboardingActivity.kt
│   │   │   ├── TrackingService.kt
│   │   │   ├── ScreenStateReceiver.kt
│   │   │   ├── DailyResetReceiver.kt
│   │   │   ├── BootReceiver.kt
│   │   │   ├── NotificationHelper.kt
│   │   │   ├── PreferencesManager.kt
│   │   │   └── TimeReclaimApplication.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── values/
│   │   │   ├── drawable/
│   │   │   └── mipmap/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 📄 License

This project is created for educational and personal productivity purposes.

---

**Built with ❤️ using Kotlin and Material Design 3**
