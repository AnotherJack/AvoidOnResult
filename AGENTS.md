# AGENTS.md

## Cursor Cloud specific instructions

This is an **Android library project** (AvoidOnResult) with a demo app. It is a pure client-side Android project — no backend, databases, or Docker.

### Project structure

- `avoidonresult/` — The reusable Android library (Java). Produces an AAR.
- `app/` — Demo app showcasing the library (Kotlin). Produces an APK.

### Prerequisites (installed via VM snapshot)

- **JDK 8** at `/usr/lib/jvm/java-8-openjdk-amd64`
- **Android SDK** at `/opt/android-sdk` with `platforms;android-26` and `build-tools;26.0.2`
- Environment variables `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT` are set in `~/.bashrc`
- `local.properties` is generated with `sdk.dir=/opt/android-sdk`

### Key commands

| Task | Command |
|------|---------|
| Build all | `./gradlew build` |
| Lint | `./gradlew lint` |
| Unit tests | `./gradlew test` |
| Clean | `./gradlew clean` |
| Assemble debug APK | `./gradlew :app:assembleDebug` |

### Gotchas

- The original project used AGP 2.3.2 and Gradle 3.3, but those artifacts are no longer available from JCenter/Google Maven. The `build.gradle` and `gradle-wrapper.properties` have been updated to **AGP 3.0.1** and **Gradle 4.1** to resolve dependencies.
- The `google()` repository shortcut is NOT supported in Gradle 4.1 buildscript blocks for this configuration. The explicit Maven URL `https://dl.google.com/dl/android/maven2/` is used instead.
- Deprecation warnings about `compile`/`provided`/`testCompile`/`androidTestCompile` configurations are expected; these still function in AGP 3.0.x.
- The `sdkmanager` CLI tool (latest version) requires JDK 17+. If you need to install additional SDK packages, temporarily set `JAVA_HOME` to JDK 21 (`/usr/lib/jvm/java-21-openjdk-amd64`) for the `sdkmanager` command only.
- This is an Android app — it cannot be "run" in the cloud VM since there is no Android emulator. Testing is limited to `./gradlew build`, `./gradlew test`, and `./gradlew lint`.
