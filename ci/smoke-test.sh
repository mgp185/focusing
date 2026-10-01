#!/usr/bin/env bash
set -euo pipefail
mkdir -p smoke-output
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb install -r smoke/build/outputs/apk/debug/smoke-debug.apk
adb shell am instrument -w com.example.focuslock.test/com.example.focuslock.FlowTests | tee smoke-output/instrumentation.txt
python3 -c "from pathlib import Path; assert 'PASS 26 checks' in Path('smoke-output/instrumentation.txt').read_text()"
adb shell am start -n com.example.focuslock/.MainActivity
sleep 3
adb exec-out screencap -p > smoke-output/home.png
adb shell am instrument -w -e mode block com.example.focuslock.test/com.example.focuslock.FlowTests | tee smoke-output/block-setup.txt
adb shell am start -n com.example.focuslock/.MainActivity
sleep 2
adb shell settings put secure enabled_accessibility_services com.example.focuslock/com.example.focuslock.AppWatchService
adb shell settings put secure accessibility_enabled 1
sleep 5
adb shell dumpsys accessibility > smoke-output/accessibility-before.txt
adb shell run-as com.example.focuslock cat shared_prefs/focus_lock.xml > smoke-output/test-prefs.xml
adb shell am start -n com.example.focusfixture/.FixtureActivity
sleep 3
adb shell dumpsys accessibility > smoke-output/accessibility-blocked.txt
adb logcat -d -s AndroidRuntime AppWatchService > smoke-output/service-log.txt
adb shell dumpsys window windows > smoke-output/blocked-windows.txt
adb exec-out screencap -p > smoke-output/blocked.png
python3 -c "from pathlib import Path; assert 'ACCESSIBILITY_OVERLAY' in Path('smoke-output/blocked-windows.txt').read_text()"
sleep 35
adb shell dumpsys window windows > smoke-output/expired-windows.txt
adb exec-out screencap -p > smoke-output/expired.png
python3 -c "from pathlib import Path; assert 'ACCESSIBILITY_OVERLAY' not in Path('smoke-output/expired-windows.txt').read_text()"
echo 'PASS: real accessibility overlay appeared and expired' | tee smoke-output/overlay-result.txt
