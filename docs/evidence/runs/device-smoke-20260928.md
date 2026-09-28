# Device Smoke Run / 真机冒烟记录

Date / 日期: 2026-09-28
Device / 设备: connected Android device over ADB Wi-Fi
Build / 构建: `androidApp-debug.apk` with `-Pcs26BaseUrl=http://192.168.31.9:8080`

## Findings / 发现

- `Connection refused` was reproducible when the local Ktor process was stopped. The APK endpoint was correct for the physical device; the service process was absent.
- `10.0.2.2` is reserved for the Android emulator. A physical device must use the developer machine LAN address and the server must bind to `0.0.0.0:8080`.
- The login screen accepted short phone input before the request reached the server. The client now requires at least 8 digits and maps connection refusal/timeouts to an actionable Chinese message.

## Automated Checks / 自动化检查

| Check | Result |
| --- | --- |
| `GET /health` over `192.168.31.9:8080` | pass |
| Development code request and verification | pass |
| Profile read | pass |
| Typed post creation and feed listing | pass |
| Group creation, invite, acceptance, member message | pass |
| Blocked-author feed filtering | pass |
| APK install and launch through ADB | pass |

The device was locked behind the system notification/keyguard surface during scripted UI taps, so full screen-level interaction still needs one unlocked-device run. This is an environment prerequisite, not an application failure.