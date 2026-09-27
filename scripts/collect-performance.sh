#!/usr/bin/env bash
set -euo pipefail

package_name="${CS26_PERF_PACKAGE:-com.knotkt.cs26.android}"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
output_dir="${1:-docs/evidence/runs/performance-${timestamp}}"

if ! command -v adb >/dev/null 2>&1; then
  echo "adb is required; install Android SDK platform-tools" >&2
  exit 1
fi

if [[ "$(adb get-state 2>/dev/null || true)" != "device" ]]; then
  echo "no authorized Android device or emulator is available" >&2
  adb devices >&2 || true
  exit 1
fi

mkdir -p "$output_dir"
{
  printf 'timestamp_utc=%s\n' "$timestamp"
  printf 'package=%s\n' "$package_name"
  printf 'host=%s\n' "$(uname -a)"
  printf 'device_model=%s\n' "$(adb shell getprop ro.product.model | tr -d '\r')"
  printf 'android_version=%s\n' "$(adb shell getprop ro.build.version.release | tr -d '\r')"
  printf 'build_fingerprint=%s\n' "$(adb shell getprop ro.build.fingerprint | tr -d '\r')"
} >"$output_dir/environment.txt"

adb shell am force-stop "$package_name"
adb shell am start -W -n "$package_name/.MainActivity" >"$output_dir/startup.txt"
adb shell dumpsys meminfo "$package_name" >"$output_dir/meminfo.txt"
adb shell dumpsys gfxinfo "$package_name" framestats >"$output_dir/gfxinfo-framestats.txt"

echo "performance evidence written to $output_dir"
