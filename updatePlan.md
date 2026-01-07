Fix App Crash on Relaunch
Problem Analysis
The app crashes when the user taps the app icon while the tracking service is already running in the background.

Root Cause
The current flow is:

OnboardingActivity is the LAUNCHER activity (receives app icon clicks)
When tapped, OnboardingActivity checks isOnboardingComplete
If true, it calls 
navigateToMain()
 which starts MainActivity and calls finish()
Problem: If MainActivity is already running in the background, creating a new instance causes a conflict/crash
Why This Happens
Every time the user taps the app icon, Android launches OnboardingActivity (the LAUNCHER activity)
OnboardingActivity then tries to start a new MainActivity instance
This conflicts with any existing MainActivity instance, causing the crash
The notification also disappears because the service likely crashes with the activity
Proposed Solution
Add android:launchMode="singleTask" to MainActivity in AndroidManifest.xml. This ensures:

Only ONE instance of MainActivity can exist
When OnboardingActivity tries to start MainActivity, it will bring the existing instance to the foreground instead of creating a new one
No conflicts, no crashes
Alternative Approaches Considered
Make MainActivity the LAUNCHER - Would require more complex logic
Use FLAG_ACTIVITY_SINGLE_TOP - Less reliable than launchMode
Add taskAffinity - Overly complex for this use case
The singleTask launch mode is the simplest and most reliable solution.

Proposed Changes
AndroidManifest.xml
Add android:launchMode="singleTask" to the MainActivity declaration:

<activity
    android:name=".MainActivity"
    android:exported="false"
    android:launchMode="singleTask"
    android:screenOrientation="portrait"
    android:theme="@style/Theme.TimeReclaimer.NoActionBar" />
This ensures that:

Only one instance of MainActivity exists
Attempting to start it again brings the existing instance to foreground
The app won't crash when relaunched while running
Verification Plan
Manual Testing
Test 1: Fresh Install Flow

Uninstall the app if already installed
Install and launch the app
Complete onboarding (enter email, grant permissions)
Verify app goes to MainActivity and tracking service starts
Verify notification appears in notification shade
Expected: App should work normally
Test2: Relaunch While Running

With the app running from Test 1, press Home button
App goes to background, notification remains visible
Tap the app icon from launcher
Expected: App should reopen to MainActivity showing current timer
Expected: No crash should occur
Expected: Notification should remain visible
Test 3: Multiple Relaunches

Repeat Test 2 multiple times (Home → Tap icon)
Expected: Each time should bring MainActivity to foreground without crashes
Test 4: Notification Tap

With app in background, tap the notification
Expected: Should open MainActivity
Verify timer is updating
Expected: No crash
Additional Notes
The singleTask launch mode is appropriate here because:

We only want ONE main activity instance
We want it to be brought to foreground when relaunched
This is standard behavior for apps with a single main screen
No changes needed to Kotlin code - this is purely a manifest configuration fix