#!/usr/bin/env bash
# Captures README screenshots from a connected device or emulator (USB debugging on).
# Usage: ./scripts/capture-screenshots.sh
# For each prompt, open the named screen on the device, then press Enter.
set -euo pipefail

ADB="${ADB:-adb}"
OUT="docs/screenshots"
mkdir -p "$OUT"

"$ADB" get-state >/dev/null 2>&1 || { echo "No device found. Connect one or start an emulator."; exit 1; }

# Clean status bar (Android 9+ demo mode) so screenshots look tidy. Ignored if unsupported.
"$ADB" shell settings put global sysui_demo_allowed 1 || true
"$ADB" shell am broadcast -a com.android.systemui.demo -e command enter || true
"$ADB" shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1200 || true
"$ADB" shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false || true
"$ADB" shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false || true

shots=(
  "01-home|Home tab"
  "02-system-info|System tab (any category)"
  "03-diagnostics|Diagnostics, after running Quick Scan"
  "04-battery|Battery screen"
  "05-storage|Storage Analyzer (grant access first)"
  "06-network|Network analyzer"
  "07-benchmark|Benchmark, after a completed run"
  "08-dark|Home tab with dark theme on"
)

for entry in "${shots[@]}"; do
  name="${entry%%|*}"; label="${entry#*|}"
  read -r -p "Open: $label, then press Enter... "
  "$ADB" exec-out screencap -p > "$OUT/$name.png"
  echo "Saved $OUT/$name.png"
done

"$ADB" shell am broadcast -a com.android.systemui.demo -e command exit || true
echo "Done. Uncomment the Screenshots table in README.md."
