.PHONY: test build run-desktop android-debug clean

GRADLE_TEST_ARGS ?=

test:
	./gradlew :shared:jvmTest :desktopApp:test $(GRADLE_TEST_ARGS) --no-daemon --console=plain

build:
	./gradlew :androidApp:assembleDebug :desktopApp:jar --no-daemon --console=plain

run-desktop:
	./gradlew :desktopApp:run --no-daemon --console=plain

android-debug:
	./gradlew :androidApp:assembleDebug --no-daemon --console=plain

clean:
	./gradlew clean --no-daemon --console=plain
