# RecallFlow

RecallFlow is a local-first personal knowledge app. Milestones B/C provide a
shared Android/desktop notes library backed by local SQLDelight storage, with
create, edit, delete and deterministic case-insensitive keyword search. Search
is lexical rather than semantic: it does not require a network or an AI model.
EmbeddingGemma 2 inference is not included.

## Scope and support status

| Target | Configuration | Verified in this workspace |
|---|---|---|
| Android | Android application | Build and connected-device test require Android SDK/device |
| Linux desktop | JVM desktop distribution | Not verified on this macOS host |
| Windows desktop | JVM desktop distribution | Configuration only; not verified |
| macOS desktop | JVM desktop distribution | Desktop JVM smoke path verified on macOS |

The desktop module is configured for DMG, MSI, and DEB packaging. Packaging is
host-specific; a Linux artifact must be built on Linux, a Windows artifact on
Windows, and a macOS artifact on macOS.

## Toolchain

- JDK `17.0.14` (JVM bytecode target 17)
- Kotlin `2.4.21`
- Gradle `9.7.0` via the committed wrapper
- Android Gradle Plugin `9.3.1`
- Compose Multiplatform `1.12.1`
- SQLDelight `2.4.1`
- Android compile SDK `37`

The runtime pin is recorded in `.java-version`. Dependency versions are recorded
in `gradle/libs.versions.toml`; Gradle dependency lockfiles are committed under
the individual module `gradle/` directories.

## Project layout

```text
:shared       KMP domain/data contracts, SQLDelight schema, drivers and tests
:composeApp   Shared Compose UI
:androidApp   Android launcher
:desktopApp   JVM desktop launcher and native distribution configuration
```

## Setup

1. Install JDK `17.0.14` and verify it with `java -version`.
2. Install Android SDK Platform 37 and Build Tools compatible with AGP 9.3.1
   for Android builds. Set `ANDROID_HOME` or `ANDROID_SDK_ROOT` locally; do not
   commit either setting.
3. Ensure the repository is on a filesystem path writable by the current user.

No global Gradle installation is required. Use `./gradlew` for every Gradle
command.

## Test

The canonical test command is:

```bash
make test
```

It runs the JVM SQLDelight repository CRUD tests and desktop unit tests. The
tests use temporary databases and do not require a network connection.

To run the Android SQLDelight device test on a connected device or emulator:

```bash
./gradlew :shared:connectedAndroidDeviceTest --no-daemon --console=plain
```

Set `ANDROID_HOME` or `ANDROID_SDK_ROOT` to the local SDK path before running
the command. It is not part of `make test` because a device/emulator is not
available on every build host.

## Run and build

Run the desktop app:

```bash
make run-desktop
```

Build the Android debug APK and desktop JVM jar:

```bash
make build
```

Build the Android debug APK only:

```bash
make android-debug
```

Run the desktop rendering smoke test without leaving a window open:

```bash
RECALLFLOW_DATA_DIR="$PWD/build/smoke-data" ./gradlew :desktopApp:run --args=--smoke-test --no-daemon --console=plain
```

On Linux CI, run the graphical smoke test under a configured display or Xvfb.

Build a host-native desktop package:

```bash
./gradlew :desktopApp:packageDeb   # Linux
./gradlew :desktopApp:packageMsi   # Windows
./gradlew :desktopApp:packageDmg   # macOS
```

## Docker and offline tests

Docker is used to prepare the Gradle distribution/dependency cache and then run
the same test workload in offline mode:

```bash
docker build --tag recallflow-milestone-a .
docker run --rm recallflow-milestone-a
```

The Dockerfile's first stage resolves pinned dependencies. Its final stage runs
the tests with `--offline`; no network is required by that test invocation.
Docker cannot prove native Windows/macOS packaging or Android device behavior.

## Reproducibility rules

- Keep the Gradle wrapper, version catalog, lockfiles and `.java-version`
  committed.
- Do not commit `local.properties`, model files, credentials, SDK paths or
  generated build output.
- Run `make test` after each working change.
- Native desktop packages must be built on their target operating system.
