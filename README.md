# VELTRIX STUDY — Native Android

VELTRIX STUDY is being rebuilt as a native Android application. The Android UI is implemented with Android Activity and native Views; study records are stored in a private SQLite database. The Android app does not embed the website or use WebView.

## Current native modules
- Native dashboard and bottom navigation
- Nine-week timetable, Monday–Sunday, with the fixed start date Monday 16 November 2026
- Local topic completion tracking
- Local notes and practice-score history
- Native Android notification reminders with weekday selection and reboot rescheduling
- SQLite schema reserved for provenance-checked question-bank records, CBT attempts and answers

## Important CBT data integrity rule
The native CBT screen will not fabricate questions. A complete 180-question UTME paper requires 60 English, 40 Mathematics, 40 Economics and 40 Geography questions with verified answers, explanations and source provenance. The native database is ready for that import, but the verified question-bank sync and full exam-taking flow remain to be implemented.

## Build
GitHub Actions builds the debug APK on pushes to main and manual workflow dispatch. The app uses Java 17 and Android Gradle Plugin 8.7.3.

## Device validation
Install and test on a physical Android device before treating any build as release-ready. Verify navigation, timetable date lock, persistence after force-stop/reboot, notification scheduling and the eventual CBT import/exam flow.

## Native CBT and offline study data

The Android app now includes the existing 196-record verified question-bank export at `app/src/main/assets/questions.json`. On launch, the app validates the required fields and imports the records into its private SQLite database while retaining source labels and URLs. The current subject inventory is English 72, Mathematics 42, Economics 40 and Geography 42.

The native CBT implementation prepares a mixed-year paper with 60 English, 40 Mathematics, 40 Economics and 40 Geography questions. It persists each answer locally, starts its two-hour clock only after the explicit Begin Exam action, marks on submission or expiry, and stores reviewable corrections. A 2025-only or 2026-only full paper remains unavailable because those year-specific subject counts do not meet all four quotas.

The native focus timer records completed study sessions locally. Cloud synchronization is intentionally not enabled: the linked Hatchable project is personal/private, its existing study APIs are admin-gated, and this Android client has no secure native authentication session. Do not embed admin credentials in the APK or weaken project privacy to bypass that boundary.

## Release validation status

A GitHub Actions pull-request build path and an Android 11 device acceptance checklist are present. The APK is not considered validated until the native build succeeds and the app is installed and exercised on an Android 11 device.
