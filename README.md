# Grayzone

Grayzone is a digital-wellbeing Android app that helps people reclaim their attention
without relying on an all-or-nothing blocker. It gives protected apps a shared daily
allowance and introduces progressively stronger friction as that allowance runs down.

## What it does

- Select social apps to protect, including Instagram, TikTok, X, YouTube, Reddit, and Facebook.
- Set a daily attention allowance for protected apps.
- Track remaining quota during an active app session.
- Move through four states: normal, mild friction, grayscale, and intervention.
- Refill a limited amount of time through intentional actions such as taking a short walk,
  completing a to-do, or reading in a calm app.
- Review usage and intervention metrics in Insights.
- Adjust protected apps, allowance, thresholds, refill sources, and protection state in Settings.

## Project status

Grayzone is currently a working prototype. State is held in a shared activity-scoped
`GrayzoneViewModel`; persistence, production usage enforcement, and a backend are not yet
implemented.

## Tech stack

- Kotlin 2.4
- Kotlin Multiplatform shared module
- Jetpack Compose and Material 3
- Android SDK 36, minimum SDK 24
- AndroidX Navigation Compose and Lifecycle
- Gradle Kotlin DSL

## Requirements

- Android Studio with Android SDK 36 installed
- JDK 11 or newer
- An Android device or emulator running API 24 or newer

## Build and run

From the project root:

```bash
./gradlew :androidApp:assembleDebug
```

Install the debug APK on a connected device with:

```bash
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

You can also open the project in Android Studio and run the `androidApp` configuration.

## First launch

1. Start Grayzone and choose the apps you want to protect.
2. Select a daily allowance.
3. When prompted, enable Grayzone's usage access permission in Android system settings.
4. Return to the app to open the Home screen.

The permission is required to identify foreground app usage. Android may show a warning for
the `PACKAGE_USAGE_STATS` permission because it is granted manually through system settings.

## Tests

Run the shared module's Android host tests with:

```bash
./gradlew :shared:testAndroidHostTest
```

## Repository layout

```text
androidApp/   Android application, Compose screens, navigation, and usage tracking
shared/       Kotlin Multiplatform shared Compose module
```