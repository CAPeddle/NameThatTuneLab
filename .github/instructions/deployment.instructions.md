````instructions
---
applyTo: "**/*"
---

# Android Device Deployment Instruction

Use this instruction when asked to deploy/install the app APK to a physical Android device.

## Standard Deployment Flow

1. Verify ADB can see the device:

```bash
adb devices
```

2. Resolve device state before install:
- `device` → continue
- `unauthorized` → unlock phone and accept **Allow USB debugging?** prompt
- `offline` or not listed → replug cable, switch USB mode to File Transfer (MTP), restart ADB

3. Install debug build from project root:

```bash
./gradlew :app:installDebug
```

Windows PowerShell/CMD equivalent:

```bash
.\\gradlew.bat :app:installDebug
```

4. If Gradle install is unavailable, use direct APK install:

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## ADB Recovery Sequence

If authorization prompt does not appear or the device remains unauthorized:

```bash
adb kill-server
adb start-server
adb devices
```

Then on device:
1. Developer options → **Revoke USB debugging authorizations**
2. Reconnect USB cable
3. Accept new RSA authorization prompt

## Multiple Devices

Target one device explicitly:

```bash
adb -s <deviceId> install -r app/build/outputs/apk/debug/app-debug.apk
```

## Common Install Errors

- `INSTALL_FAILED_VERSION_DOWNGRADE` → uninstall existing app first
- `INSTALL_FAILED_UPDATE_INCOMPATIBLE` → uninstall app signed with different key, then reinstall
- `adb` not found → add Android `platform-tools` to `PATH`
- Device not detected on Windows → install/update OEM USB driver in Device Manager

## Project Fast Path

```bash
adb devices
.\\gradlew.bat :app:installDebug
```
````
