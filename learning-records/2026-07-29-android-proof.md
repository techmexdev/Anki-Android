# Android proof record: 2026-07-29

## Source matrix

- Anki core embedded in the backend and proof APK:
  `9bfbb7b710def7519577d9ac8879817bf9747333`
- Anki Android backend:
  `14d7b093315a7311470fbb65a3648389b3a223a8`
- AnkiDroid base:
  `4f4ffdf6043f6ee0ed6c1ea17c489f972de3ed14`
- AnkiDroid feature:
  `72b250ef0430497aa29b7a49ccaed8bb01abd450`

## Feasible host work

- The build requires the sibling backend artifacts and fails clearly when they
  are absent, so it cannot silently package a published backend without the
  Brainlift contract.
- The Android collection layer delegates Brainlift evidence to the generated
  Rust backend API.
- The modern reviewer presents Memory, Performance, and Readiness separately
  and falls back to unavailable evidence without disabling review.
- A dedicated proof variant uses a unique application ID and reads the Anki
  commit from the packaged backend's generated `BuildConfig`.
- The proof APK contains `lib/arm64-v8a/librsdroid.so`.

## Verification results

- Focused `testPlayDebugUnitTest`: passed, 6 tests.
- `assemblePlayDebug`: passed.
- `assemblePlayBrainliftProof`: passed.
- `ktlintCheck`: passed.
- `lintPlayDebug`: passed.
- Proof application ID: `com.ichi2.anki.brainlift`.
- Proof application label: `Brainlift Anki`.
- Proof version: `2.25.0alpha2-brainlift`.
- Proof ARM64 APK SHA-256:
  `e7f8ce334be01b09954a81e767e52db125bf85b75ad138f42b9d2a05df10c632`.
- The exact Anki commit above was found in the proof APK's DEX payload.
- The proof APK contains `lib/arm64-v8a/librsdroid.so`.
- Backend AAR SHA-256:
  `3f13fa155e5d407f3214f5d185e1752fb40a7b8d247412a9ed3f5f5b7e67250b`.
- Backend testing JAR SHA-256:
  `f886eb45aec2525103318e8fc1bb10ad1adf922b5621cafd8d2cbf525fbe5294`.
- Backend Android-test APK SHA-256:
  `04c92a65861ac6051703ec446c0de2f2d7e5ef56bb41a1cec6b64b9e38c7b916`.
- A combined parallel invocation of both APK package tasks hit an Android
  incremental-splitter failure. Each variant built successfully when run as its
  own authoritative packaging command.

## Blocked hardware gates

`adb devices -l` reported no attached Android device. The following gates are
therefore **blocked and not passed**:

- backend `connectedDebugAndroidTest`;
- clean physical-device install and launch;
- physical offline review and reconnect;
- 10-phone plus 10-desktop bidirectional sync proof;
- same-card physical conflict proof;
- desktop-visible phone review recording.

These require an attached ARM64 Android phone and a disposable sync account.
No physical-device or connected-sync result is claimed by this record.
