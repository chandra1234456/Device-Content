# Device Content

A Material 3 / Jetpack Compose Android app that reads and displays device, hardware, and
software information already available on your phone — entirely locally, with nothing
collected or transmitted anywhere.

## Available at

<a href='https://play.google.com/store/apps/details?id=com.chandra.practice.deviceinfo'>
<img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height="80"/></a>

See [CHANGELOG.md](CHANGELOG.md) for what changed in each release.

## Features

- **Dashboard** — quick-stat cards (RAM, storage, battery, CPU, network) plus a scrollable
  category list: Build & OS, Battery, Display, Memory & Storage, CPU & Hardware, Sensors,
  Camera, Network, App Info, and Locale.
- **Material 3** throughout — dynamic color on Android 12+, a hand-tuned fallback palette on
  older versions, light/dark/system theme switch, adaptive layout (phone vs. tablet/foldable).
- **Search, PDF export** — filter the current category, and download a PDF report (with app,
  developer, and contact details on every page) straight to your device's Downloads folder.
  Copy is shown only on values worth pasting elsewhere (Android ID, Fingerprint, Package Name,
  Local IP, WiFi BSSID).
- **In-app updates** — checks the Play Store for a newer version and offers a one-tap restart
  once it's downloaded (no-op on non-Play installs, e.g. local debug builds).
- **Native Privacy Policy & Terms screens** — reachable from the first-run screen and Settings,
  kept in sync with the versions published at the links below.
- **Accessibility** — content descriptions on icon actions, system font-scale support, proper
  touch targets, empty/loading/error states for every category.

## Tech stack

- Kotlin, single-Activity **Jetpack Compose** UI (no Fragments/XML layouts)
- **Material 3** (`androidx.compose.material3`), incl. `material3-window-size-class` for the
  phone/tablet layout split
- **MVVM**: `ViewModel` + `StateFlow` per screen, no shared/global mutable state
- **DataStore Preferences** for theme mode and first-run state — no database, no network calls
- **Navigation Compose** for in-app screens
- **Play In-App Update API** (`com.google.android.play:app-update-ktx`)
- `androidx.core.splashscreen` for the launch screen

## Project structure

```
app/src/main/java/com/chandra/practice/deviceinfo/
├── MainActivity.kt              Compose host, splash screen, in-app update wiring
├── AppContainer.kt              hand-rolled service locator (repositories)
├── data/
│   ├── model/                   DeviceInfoItem, QuickStat, InfoCategory
│   └── repository/              DeviceInfoRepository, UserPreferencesRepository
├── update/                       InAppUpdateManager (Play Core wrapper)
└── ui/
    ├── theme/                    Color.kt, Theme.kt, Type.kt, Shape.kt
    ├── components/                reusable composables (cards, chips, search, empty/error/loading states)
    ├── navigation/                Routes.kt, AppNavHost.kt
    └── screens/
        ├── intro/                 first-run screen
        ├── home/                  dashboard (screen + ViewModel + UI state)
        ├── settings/               theme, dynamic color, rate/share, legal links
        ├── legal/                  native Privacy Policy / Terms & Services screens
        └── webview/                fallback WebView for external links
```

## Permissions

| Permission | Why it's needed |
|---|---|
| `INTERNET` | Only used indirectly, via the Play Store app itself for in-app updates and rate/share intents — this app has no server and makes no network calls of its own. |
| `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` | Read the current connection type (Wi-Fi/cellular/none) shown on the Network tab. |
| `CAMERA` | Requested **only** when the Camera tab is opened, and only to read how many cameras exist and which way they face. The app never opens a camera session or captures photos/video. Declared with `<uses-feature android:required="false">` so it still installs on devices without a camera. |

No other permissions are requested. See the in-app Privacy Policy (Settings → Legal) for the
full data-handling explanation.

## Getting started

### Prerequisites

- Android Studio (a recent stable release)
- JDK 17
- Android SDK: `compileSdk 36`, `minSdk 27`

### Build & run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run the `app` configuration on a device/emulator
running Android 8.1 (API 27) or later.

### Versioning

`versionCode`/`versionName` live in `app/build.gradle.kts`. Bump both together and add a new
entry to [CHANGELOG.md](CHANGELOG.md) for every release you publish.

## Privacy

This app does not collect, transmit, sell, or share device or personal data. Everything it
reads is displayed locally and only leaves the device when you explicitly tap Copy, Share, or
Export. Full details:

- Privacy Policy: in-app (Settings → Legal) or https://sites.google.com/view/device-content/home
- Terms & Services: in-app (Settings → Legal) or https://sites.google.com/view/devicecontent/home

## Links

- 📲 [Get it on Google Play](https://play.google.com/store/apps/details?id=com.chandra.practice.deviceinfo)
- 🛠️ [Developer on GitHub](https://github.com/chandra1234456)
