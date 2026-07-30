# Brainlift Android proof build

The Brainlift app consumes the locally built sibling
`Anki-Android-Backend` AAR. Scoring and scheduling remain in Anki's Rust
backend; Android only requests and presents the generated protobuf snapshot.

## Prerequisites

- `../Anki-Android-Backend/rsdroid/build/outputs/aar/rsdroid-release.aar`
- `../Anki-Android-Backend/rsdroid-testing/build/libs/rsdroid-testing.jar`
- Android SDK and the repository-supported JDK
- `local.properties` with `local_backend=true` and a valid `sdk.dir`

Do not commit `local.properties`.

## Build

Build the sibling backend first. The app always consumes those local artifacts,
and the proof UI reads its Anki commit directly from the packaged backend
`BuildConfig`:

```sh
../Anki-Android-Backend/build.sh
./gradlew :AnkiDroid:assemblePlayBrainliftProof
```

The proof variant:

- uses application ID `com.ichi2.anki.brainlift`;
- uses app name `Brainlift Anki`;
- opens the modern reviewer on a clean install; and
- displays the embedded Anki commit below the evidence panel.

The standard debug build remains available:

```sh
./gradlew :AnkiDroid:assemblePlayDebug
```

## Verify

Run focused tests and repository checks:

```sh
./gradlew :AnkiDroid:testPlayDebugUnitTest \
  --tests com.ichi2.anki.ui.windows.reviewer.BrainliftEvidenceTest \
  --tests com.ichi2.anki.ui.windows.reviewer.StudyScreenRepositoryCollectionTest
./gradlew ktlintCheck
./gradlew :AnkiDroid:lintPlayDebug
```

Inspect the proof APK's package and embedded commit before installation:

```sh
AAPT2="$ANDROID_HOME/build-tools/$(ls "$ANDROID_HOME/build-tools" | sort -V | tail -1)/aapt2"
APK="AnkiDroid/build/outputs/apk/play/brainliftProof/AnkiDroid-play-arm64-v8a-brainliftProof.apk"
"$AAPT2" dump badging "$APK" | grep -E "package:|application-label:|native-code:"
ANKI_COMMIT="$(git -C ../Anki-Android-Backend/anki rev-parse HEAD)"
BACKEND_COMMIT="$(git -C ../Anki-Android-Backend rev-parse HEAD)"
unzip -p "$APK" 'classes*.dex' | strings | grep -F "$ANKI_COMMIT"
unzip -p "$APK" 'classes*.dex' | strings | grep -F "$BACKEND_COMMIT"
shasum -a 256 "$APK"
```

The final physical-phone proof must use a disposable profile and sync account.
It must record a clean install, one normal review, offline review, reconnect,
bidirectional sync counts, and the embedded commit. A host build or emulator
does not satisfy that gate.
