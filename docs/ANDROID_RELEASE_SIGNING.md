# VELTRIX STUDY — Android release signing

The repository builds a debug APK automatically on pushes to `main`. A distributable release APK must be signed with a persistent private keystore so future updates can install over the existing app.

## One-time keystore setup

Generate the release keystore on a trusted device or computer. Keep the file and passwords private; never commit them to this repository.

```bash
keytool -genkeypair -v \
  -keystore veltrix-study-release.jks \
  -alias veltrix-study \
  -keyalg RSA -keysize 4096 -validity 10000
```

Base64-encode the keystore without line wrapping:

```bash
base64 -w 0 veltrix-study-release.jks
```

Add these four repository Actions secrets under Settings → Secrets and variables → Actions:

- `ANDROID_KEYSTORE_BASE64`: the base64 output above
- `ANDROID_KEYSTORE_PASSWORD`: keystore password
- `ANDROID_KEY_ALIAS`: `veltrix-study` (or the alias selected during generation)
- `ANDROID_KEY_PASSWORD`: key password

Keep a secure offline backup of the original keystore. Losing it prevents signing compatible updates under the same app identity.

## Build

After the four secrets are configured, open Actions → **Build signed VELTRIX STUDY release APK** → Run workflow. The workflow verifies the APK signature and uploads `veltrix-study-release-apk` as a 30-day artifact.

The workflow intentionally does not create a keystore or publish an unsigned release. Debug builds remain independent of release signing.
