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