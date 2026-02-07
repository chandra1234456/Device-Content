# Device Information Android App

This Android app retrieves and displays various device information such as Android ID, device model, manufacturer, OS version, and more. It also supports showing charts based on collected device data.


## Available at

<a href='https://play.google.com/store/apps/details?id=com.chandra.practice.deviceinfo'>
<img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height="80"/></a>

## Features

- Fetches device unique ID (`ANDROID_ID`)
- Displays device manufacturer, model, brand, and OS version
- Collects CPU architecture and screen metrics
- Shows charts visualizing device distribution and app usage (optional)
- Handles permission-free device info collection safely

## Getting Started

### Prerequisites

- Android Studio 8.1 and above.
- Android SDK 27+
- A physical or virtual Android device for testing

### Building the App

1. Clone or download the project.
2. Open the project in Android Studio.
3. Build and run the app on a device or emulator.

### Usage

- Launch the app to see device information displayed on the screen.
- Navigate to the charts section to view graphical representations of device data.
- The app does not require any special permissions for basic device info.

## Code Snippets

Example: Getting Android ID

```kotlin
fun getAndroidId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_ANDROID_ID"
}
```

## ✈️ Download

- 📲 [Get it on Google Play](https://play.google.com/store/apps/details?id=com.chandra.practice.deviceinfo)
- 🛠️ [Get it on GitHub](https://github.com/chandra1234456)
