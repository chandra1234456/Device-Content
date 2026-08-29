# Changelog

All notable changes to Device Content are documented here. Format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versions correspond to the
`versionName` in `app/build.gradle.kts`.

## [Unreleased] — targeting 1.1.0

### Added
- Full **Material 3 / Jetpack Compose** rewrite — Fragments, XML layouts, and Navigation
  Component are gone, replaced with a single-Activity Compose app (MVVM, `StateFlow`, DataStore).
- Dashboard home screen: quick-stat cards, category chips, search/filter, empty/loading/error
  states per category.
- Dynamic color (Android 12+) with a hand-tuned Material 3 fallback theme, plus light/dark/
  system theme switching from Settings.
- Adaptive layout — a `NavigationRail` two-pane layout on tablets/foldables, chips + single
  column on phones.
- New app icon (adaptive icon + Android 13+ monochrome variant) and an animated splash screen
  (entrance pop-in on Android 12+, a scale/fade exit animation on every supported version).
- In-app update check via the Play Store (flexible flow with a restart banner once downloaded).
- Native Privacy Policy and Terms & Services screens (Settings → Legal, and from the first-run
  screen), kept in sync with the versions published online.
- Runtime permission flow for the Camera tab, with an in-context rationale instead of relying
  on a permission that was previously declared but never actually requested.
- Static launcher shortcuts (long-press the app icon) for Battery, Storage, and Network.
- **PDF export** — the FAB now generates a real PDF (app name, developer, and contact email on
  every page) and saves it straight to the device's Downloads folder, no share sheet involved.
  Confirms via a dialog before writing anything.
- **Device Health Score** on the dashboard — a 0-100 score (Battery/Storage/Memory/Network
  subscores, an animated ring, and short recommendations when something's actually off) computed
  entirely from data the app already collects. No new permissions.
- **Battery Health & Analytics screen** — live temperature, voltage, current, and power draw, a
  "Good/Charging" status pill, and a **Battery Test** that samples charge level over time to
  estimate charge/discharge rate and time-to-full, with a simple line chart. No new permissions.
- **Diagnostics ("Test My Phone")** — Display (full-screen color/dead-pixel sweep), Touch (drag-
  to-cover grid), Vibration, Flash, and live-reading tests for Proximity, Accelerometer,
  Gyroscope, and Compass, with a hub screen and a "Run All Automatic Checks" action for the four
  sensor tests. No new dangerous permissions — Vibration uses the normal, auto-granted `VIBRATE`
  permission, and Flash reuses the existing Camera permission flow. Speaker/Microphone, a live
  Camera preview, Bluetooth, GPS, and Wi-Fi testing are intentionally deferred — each needs its
  own contextual permission rationale rather than being requested speculatively.
- **Live Monitor** — a real-time dashboard (RAM, storage, battery, and network throughput, all
  polled ~every 1.5s while the screen is open) plus the device's thermal-throttling status
  (Normal/Light/Moderate/Severe/Critical) as an honest stand-in for "CPU load": Android has
  blocked regular apps from reading `/proc/stat` since Android 8 (Oreo), so a fabricated live
  CPU percentage was deliberately left out rather than faked. No new permissions — thermal status
  and network totals are both public, permission-free APIs.
- A **Quick Actions** grid on the dashboard (Battery, Diagnose, Monitor) — the entry point for
  feature screens going forward, replacing the Battery quick-stat card's one-off click handler.

### Changed
- Release builds now also shrink resources (`shrinkResources = true`), not just code.
- `CameraManager`/`PackageManager` calls and `/proc/cpuinfo` reads moved off the main thread,
  with per-category caching so switching tabs is instant after the first load.
- Simplified the top bar to a single direct Settings icon — removed the overflow menu (Copy
  all / Share as text), the manual Refresh action, and the Retry button on error states, since
  none of them were pulling their weight.
- Per-row Copy is now shown only on values worth pasting elsewhere (Android ID, Fingerprint,
  Package Name, Local IP, WiFi BSSID) instead of on every single row.
- Corrected the support email used in-app (`chandradev3660@gmail.com`) — the externally
  published Privacy Policy/Terms pages have a typo (`@gmai.com`) that's outside this repo.

### Fixed
- The dashboard's Health Score card, Quick Actions grid, and quick-stats row were pinned above a
  separately-scrolling inner list, so on shorter screens they could squeeze the category list
  down to almost nothing. The whole screen is now one scrolling list, so everything moves
  together.
- Removed the Favorites tab/star toggle — it added a second, disconnected way to browse the same
  items the category list already covers, without pulling its weight.
- Switching to the dashboard's default category on first launch never actually triggered a
  load — a same-category guard in the ViewModel treated the initial state as already loaded, so
  the Overview tab would show its loading shimmer forever until you switched away and back.
- The first-run Terms/Privacy screen showed on *every* launch instead of only the first —
  now gated behind a persisted "has seen intro" flag.
- The "Agree & Start" button on that screen previously had no click handler at all.
- Search was wired to the wrong toolbar action and never actually filtered anything.
- `Context.getDisplay()` was called unconditionally in the Display info screen despite
  requiring API 30+, while `minSdk` is 27 — a real crash on Android 9/10/10.1 devices.
- Removed a dead `<activity android:name=".WebViewActivity">` manifest entry pointing at a
  class that didn't exist anywhere in the source.

## [1.0.2]
Reconstructed from git history (no changelog was kept before this file existed):
- Latest version released to the Play Store prior to the Compose rewrite.
- Terms & Privacy Policy intro/WebView flow introduced.
- Night theme and additional device-info fields added.
- CI workflow added for testing/deployment.
- Export-to-file option added.

## [1.0.0]
- Initial release: core device info screen (Build/OS, battery, memory, CPU, sensors, camera,
  network, app info, locale).
