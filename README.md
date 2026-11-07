# JARVIS for Android

A beginner-friendly Android personal assistant project with a futuristic blue interface.

## Current milestone

- Native Android app foundation using Kotlin and Jetpack Compose
- Initial JARVIS home screen
- App identity and planned "Hey Jarvis" wake phrase

## Planned milestones

1. Add microphone permission and foreground voice interaction with a visible notification.
2. Add speech-to-text and text-to-speech.
3. Add Claude integration through a secure backend (never commit API keys to this repository).
4. Add user-controlled memory and app-launching shortcuts.
5. Investigate a wake phrase flow that respects Android background microphone and battery restrictions.

## Build

Open this repository in Android Studio, allow Gradle sync, then run the `app` configuration on an Android device or emulator.

Requirements: Android Studio with Android SDK 35 installed. The app currently has a minimum SDK of 26.

## Important

The "Hey Jarvis" wake phrase is a planned feature, not implemented yet. Android restricts background microphone use; any listening mode must be explicitly enabled and visibly disclosed to the user.
