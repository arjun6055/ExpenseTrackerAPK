# My Expense Analyzer V2.5.2 - Android Studio Native Project

This project is deliberately configured around the Android Studio installation already present on the target Windows PC.

## Known target environment
- Android Studio: Quail 4 / 2026.1.4
- Gradle JDK selected in Android Studio: Java 25
- Android SDK: `%LOCALAPPDATA%\Android\Sdk`
- Installed compile platform: Android 17 / API 37
- Gradle: 9.7.1
- Android Gradle Plugin: 9.4.0
- compileSdk: 37
- targetSdk: 35
- minSdk: 28
- Application ID: `com.expenseanalyzer.app`

AGP 9 uses built-in Kotlin support; the legacy `org.jetbrains.kotlin.android` plugin is intentionally absent.

## No automatic JDK/SDK download
There is no Temurin download and no forced API 35 requirement. The build uses the existing Android Studio JBR and installed SDK 37.

## Build
1. Open this folder in Android Studio.
2. Let Android Studio sync the project.
3. If desired, run `02_BUILD_DEBUG_APK.bat`; it first runs `gradle help` as a configuration check and only then runs `:app:assembleDebug`.
4. The APK is copied to `MyExpenseAnalyzer-V2.5.2-debug.apk`.

## App
- Native fingerprint/face/device-credential gate via Android BiometricPrompt.
- Existing Apps Script server-side PIN remains unchanged.
- WebView opens the deployed Apps Script `/exec` URL saved on the device.
