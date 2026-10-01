# FocusLock 3.0 validation

Final successful run: https://github.com/mgp185/focusing/actions/runs/36811074127
Code commit: 8613cd47651e86199b46e524a70edafb9236f2bb

- APK build and Android lint passed.
- 22 JUnit checks passed with zero failures or ignored tests, including weekday/weekend, overnight start-day semantics, overlapping/adjacent windows, DST, coin rates and session policies.
- 26 Android persisted-state/UI checks passed: ordinary/strict immediate sessions, running-strict schedule protection, multiple schedules, completion/abandonment rewards, reboot cancellation and page anchors.
- 19 live API-35 integration checks passed: actual foreground service connection, all three AudioTrack BGM soundscapes, track changes and on/off, real focus overlays, both quit choices and zero rewards on abandonment, real one-minute completion, scheduled overlays, connected time accrual, running-strict deletion rejection and saving a selected installed app through the native editor.
- The original timed app-lock overlay appeared over a separate test app and disappeared after expiry.
- Native screenshots were inspected for the focus screen, blocking and quit screens, schedule editor, saved schedule and shop.

Delivered APK SHA256: 9e3d8c4debaacb98156942dfda95be3a1bc0bd43f4b675c4735b92d9421bb064 (51919 bytes).

No physical Galaxy was connected. Actual sound quality, incoming/emergency calls, OEM restricted settings and power management, PiP and multi-window remain physical-device checks. The API-35 emulator validation is not a complete Android-16/Galaxy certification. Coins are local virtual data, not tamper-proof assets; full system locking/uninstall prevention is not implemented.
