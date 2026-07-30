# Android proof record: 2026-07-29

## Source matrix

- Anki core embedded in the backend and proof APK:
  `fc433d2b13de69d8dcab9c1086f35c4d9b2c328a`
- Anki Android backend:
  `4a72226e8caca4677a09d3cc3aaed44f3c8b2992`
- AnkiDroid base:
  `4f4ffdf6043f6ee0ed6c1ea17c489f972de3ed14`
- AnkiDroid feature:
  `64799ed0181b9c3d24f889a8513755b5a010303b`

## Feasible host work

- Local-backend AnkiDroid build path is configured without committing
  machine-local properties.
- The Android collection layer delegates Brainlift evidence to the generated
  Rust backend API.
- The modern reviewer presents Memory, Performance, and Readiness separately
  and falls back to unavailable evidence without disabling review.
- A dedicated proof variant uses a unique application ID and embeds the Anki
  commit supplied at build time.
- The proof APK contains `lib/arm64-v8a/librsdroid.so`.

## Verification results

- Focused `testPlayDebugUnitTest`: passed, 6 tests.
- `assemblePlayDebug`: passed.
- `assemblePlayBrainliftProof` with the Anki commit above: passed.
- `ktlintCheck`: passed.
- `lintPlayDebug`: passed.
- Proof application ID: `com.ichi2.anki.brainlift`.
- Proof application label: `Brainlift Anki`.
- Proof version: `2.25.0alpha2-brainlift`.
- Proof ARM64 APK SHA-256:
  `55b97a82de3dd2645ab141f6768b82a4beb888e0f50ede21c934563e1aecc017`.
- The exact Anki commit above was found in the proof APK's DEX payload.
- The proof APK contains `lib/arm64-v8a/librsdroid.so`.
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
