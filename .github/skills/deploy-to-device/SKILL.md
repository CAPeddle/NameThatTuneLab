````skill
---
name: deploy-to-device
description: "Deploy the Android debug APK to a connected physical device using ADB and Gradle. Use this skill when asked to install or run the app on a phone, troubleshoot unauthorized/offline devices, or verify an APK install."
argument-hint: "[module] [variant] — e.g., ':app debug'"
---

# Deploy APK to Connected Android Device

This skill installs the app on a USB-connected Android device and verifies launch readiness.

## Prerequisites

- Android SDK platform tools installed (`adb` available on `PATH`)
- Developer options enabled on device
- USB debugging enabled on device
- Device unlocked and connected by USB (data cable)

## Step 1 — Verify Device Connectivity

```bash
adb devices
```

Expected states:
- `device` → ready to install
- `unauthorized` → approve the USB debugging prompt on phone
- `offline` → reconnect cable and restart ADB
- no devices listed → check cable/USB mode/drivers

## Step 2 — Resolve Common ADB Authorization Issues

If state is `unauthorized`:

```bash
adb kill-server
adb start-server
adb devices
```

Then on phone:
1. Keep screen unlocked
2. Accept **Allow USB debugging?** prompt
3. (Optional) enable **Always allow from this computer**

If prompt does not appear:
1. Developer options → Revoke USB debugging authorizations
2. Replug cable
3. Set USB mode to File Transfer (MTP)
4. Run `adb devices` again

## Step 3 — Build and Install Debug APK

```bash
# Preferred: build + install from Gradle
./gradlew :app:installDebug

# Windows PowerShell / CMD equivalent
.\gradlew.bat :app:installDebug
```

## Step 4 — Alternate Direct APK Install

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If multiple devices are connected:

```bash
adb -s <deviceId> install -r app/build/outputs/apk/debug/app-debug.apk
```

## Step 5 — Verify Deployment

```bash
adb devices
adb shell pm list packages | grep [PLACEHOLDER:applicationId]
```

If package lookup tooling differs on Windows shell, verify by launching from app drawer.

## Troubleshooting

| Problem | Fix |
|---|---|
| `device unauthorized` | Accept RSA prompt, or revoke USB authorizations and reconnect |
| `INSTALL_FAILED_VERSION_DOWNGRADE` | Uninstall old app first: `adb uninstall [PLACEHOLDER:applicationId]` |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | App signed with different key; uninstall existing app and reinstall |
| Device not detected on Windows | Install/update OEM USB drivers in Device Manager |
| `adb` not found | Add `platform-tools` to `PATH` or run with full path |

## Project-Specific Fast Path

From repository root:

```bash
adb devices
.\gradlew.bat :app:installDebug
```

If successful, launch the app from the phone app list.
````
