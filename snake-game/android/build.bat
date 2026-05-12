@echo off
chcp 65001 >nul
setlocal

set JAVA_HOME=C:\Users\michael\.qclaw\workspace\jdk-17.0.10+7
set ANDROID_HOME=C:\Users\michael\.qclaw\workspace\android-sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%

cd /d "%~dp0"

echo Building Snake Game APK...
echo.

call gradlew.bat assembleRelease

if exist app\build\outputs\apk\release\app-release.apk (
    echo.
    echo APK built successfully!
    copy /Y app\build\outputs\apk\release\app-release.apk SnakeGame.apk
    echo Output: SnakeGame.apk
) else (
    echo Build failed!
)

pause
