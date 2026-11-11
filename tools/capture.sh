#!/bin/bash
# Screenshots of the -Pdemo -Pshots build (app.kitchenwitch.shots) in one language, opened straight on each screen by intent extras.
# Usage: tools/capture.sh en|ru   (ANDROID_SERIAL picks the phone; the Mi 9 by default)
set -e
export ANDROID_SERIAL=${ANDROID_SERIAL:-d7a925e6} JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
cd "$(dirname "$0")/.."
L=$1; P=app.kitchenwitch.shots; OUT=docs/screenshots/$L; mkdir -p $OUT
./gradlew assembleRelease -Pdemo -Pshots -Pdemolang=$L -q 2>&1 | grep -E "^e: |FAILED" && exit 1
adb install -r app/build/outputs/apk/release/app-release.apk >/dev/null
adb shell pm clear $P >/dev/null
adb shell am start -n $P/app.kitchenwitch.MainActivity >/dev/null; sleep 10
shot() { adb shell am start -S -n $P/app.kitchenwitch.MainActivity $2 >/dev/null; sleep ${3:-5}
  f=$(adb shell dumpsys window | grep -m1 mCurrentFocus); echo "$f" | grep -q "$P" || { echo "focus lost: $f"; exit 1; }
  adb exec-out screencap -p > $OUT/$1.png; }
shot 1-kitchen "--ei tab 0" 6
shot 2-moon "--ei tab 1"
shot 3-book "--ei tab 2"
shot 4-shelf "--ei tab 3"
shot 5-wheel "--ei tab 4"
shot 6-cook "--es cook musakhan"
adb shell am force-stop $P
echo done $L
