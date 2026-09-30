# FocusLock 2.0

Personal Android app lock with a Korean interface, navy/mint styling, focus/apps/settings screens, a session progress ring, searchable app selection, persisted selections, 1/25/50/90-minute presets, custom 1–1440-minute sessions and light/dark themes. Session actions stay visible on small screens.

Normal sessions can be ended early after confirmation. Strict sessions reject early stop and reconfiguration until expiry. Accessibility detects package names and shows a timed accessibility overlay. No internet permission, analytics SDK, ads or screen-content retrieval.

## Build and verification

JDK 17, Gradle 8.7, AGP 8.6.1, Android SDK 35. Run `gradle assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug`.

GitHub Actions builds the APK, runs nine unit tests and Android lint, then installs it on an API 35 emulator. The smoke script runs 11 instrumentation checks and verifies that an accessibility overlay appears for a separate test app and disappears after expiry. It captures actual screens and diagnostics. See [latest validation run](https://github.com/mgp185/focusing/actions/runs/36738796170). Physical Galaxy-device testing remains necessary.

APK and reports are available as Actions artifacts. The `smoke` module and instrumentation APK are test tools; install only the main app APK for personal use.

## Install

The debug signing key differs from the previous 1.1 build: uninstall the old app, then install the new APK. Settings are cleared on uninstall. CI debug keys are ephemeral, so future debug builds may also require reinstalling.

Grant FocusLock Accessibility access. If restricted, open Settings > Apps > FocusLock app info > More > Allow restricted settings, then enable the installed accessibility service. Menu labels vary by OS/OEM. Start with one selected app and a one-minute ordinary session, then a one-minute strict session.

## Limits

Strict mode restricts in-app changes. It does not prevent system permission revocation, uninstall, force-stop, safe mode or reboot followed by clock changes. Multi-window, PiP, notification paths and OEM power management require device checks. Background playback is not stopped. Daily usage quotas, recurring schedules and focus statistics are not implemented.

This is a personal test build, not a Play Store release.
