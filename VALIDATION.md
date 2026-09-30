# Validation evidence

[Core verification run](https://github.com/mgp185/focusing/actions/runs/36737479777): APK build, Android lint and 9 JUnit tests passed. API 35 emulator installation succeeded; instrumentation returned `PASS 11 checks`; timed blocking returned `PASS: real accessibility overlay appeared and expired`. Home, blocked and expired screenshots were inspected.

[Final theme compatibility run](https://github.com/mgp185/focusing/actions/runs/36738796170): includes readable status/navigation icons and API-26-compatible theme resources. Consult the run result and uploaded artifacts for the final APK and screenshots.

Runtime checks cover persisted state, empty selection, ordinary early stop, strict early-stop rejection, active-session reconfiguration rejection, monotonic expiry, automatic expiry, UI anchor controls, overlay creation and removal. They do not exhaustively automate every dialog, search input or theme toggle. No physical Samsung device was connected. Actual-device permissions, battery restrictions, notifications, PiP and multi-window remain to be tested.
