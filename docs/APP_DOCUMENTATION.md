# Device Content: Application Documentation

Package `com.chandra.practice.deviceinfo` · minSdk 27 · targetSdk/compileSdk 36

## 1. What the app is

A local-only device information and diagnostics tool. It reads data the Android OS already
exposes, shows it in a Material 3 UI, and lets the user copy, share or export it. It has no
backend, no login and no analytics SDK.

## 2. What data is shown

### System info (Home → System tab)
| Category | Data |
|---|---|
| Build & OS | Model, manufacturer, brand, device, product, board, hardware, Android version, API level, build ID/time, security patch, fingerprint, bootloader, radio version |
| Battery | Level, status, health, temperature, voltage, technology, thermal status |
| Display | Resolution, density, refresh rate, screen size, orientation |
| Memory & Storage | Total/available RAM, internal storage total/used/free |
| CPU & Hardware | ABIs, core count, hardware/board details |
| Sensors | List of available sensors |
| Camera | Number of cameras and the direction each faces (needs `CAMERA`) |
| Network | Connection type, Wi-Fi and IP details |
| App Info | This app's package, version, install info |
| Locale | Language, region, time zone |

### Tools
- **Health score**: derived from battery, storage and memory ([DeviceHealthCalculator.kt](../app/src/main/java/com/chandra/practice/deviceinfo/data/health/DeviceHealthCalculator.kt)).
- **Monitor**: live CPU/memory snapshots.
- **Network analyzer**, **Sensor explorer** (live values), **Benchmark** (CPU/memory scores).
- **Storage analyzer**: used/free plus a breakdown by Photos & Images, Videos, Audio & Music,
  Downloads, Apps & System Data, Free. Media sizes come from MediaStore.
- **Diagnostics / Phone checkup**: touch, display, speaker, microphone, camera, flash,
  vibration, GPS, and per-sensor tests. Results are kept **in memory only** for the session.

### Where data can leave the device
Only when the user taps **Copy**, **Share** or **Download PDF**. Nothing is uploaded.

## 3. Permissions

Declared in [AndroidManifest.xml](../app/src/main/AndroidManifest.xml).

| Permission | Type | Feature | Requested when |
|---|---|---|---|
| `CAMERA` | Runtime | Camera info, camera test, flash test | Opening those screens |
| `RECORD_AUDIO` | Runtime | Microphone test (input level meter, not saved) | Opening the mic test |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Runtime | GPS test | Opening the GPS test |
| `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO` | Runtime (API 33+) | Storage analyzer sizes | Tapping "Allow access" in Storage |
| `READ_EXTERNAL_STORAGE` (`maxSdkVersion=32`) | Runtime (API ≤ 32) | Same as above | Same |
| `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` | Normal | Network info | Install time |
| `BLUETOOTH`, `BLUETOOTH_CONNECT` | Normal / runtime on 31+ | Bluetooth status | Install time |
| `VIBRATE` | Normal | Vibration test | Install time |
| `INTERNET` | Normal | Play in-app updates, online legal pages (WebView) | Install time |

`uses-feature` for camera, microphone, GPS, location, Wi-Fi, Bluetooth, touchscreen, autofocus
and flash are all `required="false"`, so the app is installable on tablets, Chromebooks and
devices without that hardware. Hardware tests must handle missing hardware gracefully.

### Play Console notes
- **Photo and video permissions** (`READ_MEDIA_IMAGES/VIDEO`) are policy-restricted. You must
  justify them in the permissions declaration. If rejected, remove the Photos/Videos rows and
  those two permissions; the rest of the app is unaffected.
- **Downloads** on Android 11+ only reports files created by this app (scoped storage), so that
  row can be low or zero. This is a platform limit.
- Update the **Data safety form** and the hosted **Privacy Policy** to mention camera,
  microphone, location and media access, all processed on-device only.

## 4. Architecture

- Single `MainActivity` hosting Compose; edge-to-edge via `enableEdgeToEdge()`.
- `configChanges` is declared on the activity, so rotation, fold and font changes don't
  recreate it. Compose reads the new configuration instead.
- `AppNavHost` owns the nav graph, transitions, and a max content width of 840dp on wide
  screens (display/touch tests stay full screen).
- MVVM: each screen has a `ViewModel` exposing `StateFlow`, created with a small `Factory`
  that pulls repositories from `AppContainer`.
- `DeviceInfoRepository` computes each category off the main thread and caches it. Call
  `invalidate(category)` after something that changes the data (e.g. a permission grant).
- Preferences (theme mode, dynamic color, intro seen) live in DataStore.

### Adding a new info category
1. Add an entry to `InfoCategory`.
2. Add a branch in `DeviceInfoRepository.collect()` and a function returning `List<DeviceInfoItem>`.
3. It appears in chips/rail, search and PDF export automatically.

### Adding a new tool screen
1. Add a route in `Routes.kt` and a `composable` in `AppNavHost.kt`.
2. Create `Screen` + `ViewModel` under `ui/screens/<name>/`.
3. Add a `QuickAction` in `HomeScreen.kt` (`QuickActionsGrid`).

## 5. Developer checklist

**Build**
- JDK 17 is required. `gradle.properties` currently has a machine-specific
  `org.gradle.java.home` and the configuration cache turned off. Don't commit those local changes.
- Versions are old: Kotlin 1.9.24, Compose compiler 1.5.14, Compose BOM 2024.06, AGP 8.6.1.
  Upgrading unlocks Compose strong skipping and newer APIs.
- `compileOptions` use Java 8; consider moving to 17.

**Security**
- `app/build.gradle.kts` contains the keystore path and passwords in plain text. Move them to
  `local.properties`/CI secrets, rotate the passwords, and keep `keystore/` out of git.
- Don't log device identifiers (Android ID, fingerprint) in release builds.

**Release**
1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Add a `CHANGELOG.md` entry.
3. Build a signed bundle: `./gradlew bundleRelease` (R8 minify + resource shrinking are on).
4. Smoke-test the release build on a phone, a tablet/foldable emulator, Android 8.1 and 14+.
5. Check the Play Console permission declarations and Data safety form.

**Testing**
- Unit tests: `./gradlew testDebugUnitTest`. Only template tests exist today, so repository
  logic and `DeviceHealthCalculator` are good first candidates.
- Manually verify each permission flow: grant, deny, "don't ask again", and revoke in Settings.
- Verify hardware tests on devices without a flash, with few sensors, or without a camera.

**Known limitations**
- Diagnostic results aren't persisted between launches.
- The Bluetooth diagnostic result should be checked against real adapter state before being
  trusted (see `DiagnosticsViewModel`).
- Values like IMEI or serial number are not available to normal apps on modern Android and are
  not shown.
