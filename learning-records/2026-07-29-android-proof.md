# Android proof record: 2026-07-29

## Source matrix

- Anki core embedded in the backend and proof APK:
  `2231760d2edb52f8fae224a051775b548eba2dcf`
- Anki Android backend:
  `33f7faeb4dc657d7ecf2ba0eb27a6d27224b06d1`
- AnkiDroid base:
  `4f4ffdf6043f6ee0ed6c1ea17c489f972de3ed14`
- AnkiDroid feature:
  `a1390bebac99daae6179adc90d3c09554f2456ac`

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
  `2b688722a972a05bd1245249cc38af70f63f07eaf16fa164ec17a87e9b33c193`.
- The exact Anki commit above was found in the proof APK's DEX payload.
- The proof APK contains `lib/arm64-v8a/librsdroid.so`.
- Backend AAR SHA-256:
  `138cb213b29abdec21b5e2940d07d3332e32350d80a65aa05d676c3686b5b728`.
- Backend testing JAR SHA-256:
  `898641f879475e54186566a7f464853bff9d4792db4548ae0a350cfc59108325`.
- Backend Android-test APK SHA-256:
  `57517441eb5def38925b4d96958b1e4cb1d8e5ec91ce22f4b0cd3ba6ccbbf9c8`.
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
