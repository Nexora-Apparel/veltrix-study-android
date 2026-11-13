# VELTRIX STUDY

A native Android home for the VELTRIX STUDY timetable, with a simple study-reminder feature.

- App name: VELTRIX STUDY
- Package: `com.veltrix.study`
- Minimum Android: 6.0 (API 23)
- Website: https://veltrix-study.hatchable.site

## What is included

- Smooth, hardware-accelerated WebView browsing.
- Existing study account and saved progress remain on the website.
- Full nine-week timetable available in a built-in offline study space.
- Tick off topics, save notes and record practice scores without internet; offline entries stay on this phone and are not automatically synced to the cloud yet.
- Choose a reminder time and the days it repeats; phone notifications return after a restart.
- Phone notifications, with reminders restored after a restart.
- Back navigation and external links handled outside the app.
- If the connection drops, the app opens the offline study space; reconnect with the button when ready.

The website remains private. The app does not contain login credentials or database secrets.

## Build

The GitHub Actions workflow builds a debug APK on every push to `main`. Download the artifact named **veltrix-study-debug-apk** from the workflow run.

This first APK is for installation and device testing. A signed release build can be prepared separately once a private signing key is configured.
