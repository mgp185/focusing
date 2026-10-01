# FocusLock 3.0
Personal Android app lock. Korean navy/mint UI, ordinary/strict immediate app lock, multiple weekly schedules with per-schedule targets and ordinary/strict modes, overnight windows, focus mode, virtual coins and an empty shop. Original offline synthesized piano, ambient and rain loops include selection, toggle and volume controls.

## Semantics
- Day selections identify the START day. 23:00–07:00 continues into the next day. Device local timezone applies. Equal start/end is rejected.
- Running strict schedules cannot be edited, disabled or deleted. Outside their active windows they are editable.
- Connected accessibility-service time inside the union of enabled scheduled windows yields 1 coin/hour, maximum regardless of overlapping targets/schedules. No retroactive offline rewards or immediate-lock rewards. Pauses caused by service death or large clock changes are not credited.
- Focus mode blocks other apps and the launcher via accessibility overlays. FocusLock, SystemUI, the default dialer and telecom packages remain available; emergency/ringing calls are intentionally allowed. Full system lock and uninstall prevention are not possible with ordinary permissions.
- Focus completion earns 10 coins/hour in whole-coin units, with fractional completed time carried forward. Abandonment earns zero for that session. Schedules and focus rewards add independently (11/hour together).
- The quit dialog rotates encouragement text and offers explicit continue/quit options. Permission/service interruptions or reboot cancel the focus reward.
- Coins are local virtual balances with no monetary value, cloud sync or purchase items yet. Uninstall/data clearing loses them.

## Build
JDK17, Gradle8.7, AGP8.6.1, SDK35, minSdk26, targetSdk35. `gradle assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug`.
Only the main APK is for installation. Smoke and instrumentation APKs are test tools.

## Permissions and limits
Accessibility reads foreground package names, not screen contents. No INTERNET permission, ads, accounts or analytics. Foreground media playback and optional notification permission support audible BGM; a playback notification is shown as Android allows.
Grant accessibility; if Android restricts it, use Settings > Apps > FocusLock app info > More > Allow restricted settings. OEM labels vary.
Strict mode controls in-app edits, not system revocation, uninstall, force-stop, safe mode or clock changes. PiP, multi-window, notification shade and manufacturer battery policies need physical-device checks. Background media from other apps is not blocked. Existing debug keys differ; reinstall may be necessary and clears data. No Play Store release.

## Validation
See VALIDATION.md for current verified evidence. Physical Galaxy S23 testing is still necessary.
