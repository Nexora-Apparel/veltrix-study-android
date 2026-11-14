# Native Android 11 validation

## Automated build
The pull-request workflow must complete `gradle --no-daemon assembleDebug` and upload `app/build/outputs/apk/debug/app-debug.apk`.

## Real-device acceptance checklist
Target: Realme 5 Pro, Android 11, 6 GB RAM.

- Install the APK and launch VELTRIX STUDY without a WebView.
- Confirm all 196 question records are imported into private SQLite storage and subject counts are English 72, Mathematics 42, Economics 40, Geography 42.
- Prepare a mixed-year 180-question paper: 60 English, 40 Mathematics, 40 Economics, 40 Geography.
- Confirm the timer remains stopped on the prepared-paper screen and begins only after explicit Begin Exam.
- Select answers, navigate between questions, force-stop/reopen the app, and verify answers persist and remaining time is derived from the original start timestamp.
- Submit early and allow a separate test attempt to expire; verify marking occurs only after submission/expiry.
- Reopen historical corrections and verify selected answer, correct answer, option reasoning, worked explanation, topic and source URL.
- Run, stop and background a focus session; verify the session is recorded once.
- Verify notes, timetable completion, reminders and progress remain intact.

## Cloud synchronization gate
The existing Hatchable project is personal/private and the Android client has no authenticated native session. Do not expose its API or weaken project visibility to make sync appear functional. Cloud sync remains disabled until a secure native sign-in/token flow is approved and implemented.
