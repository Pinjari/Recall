# The first stage warms the Gradle distribution and dependency caches online.
FROM eclipse-temurin:17.0.14_7-jdk-jammy AS dependency-preparation

WORKDIR /workspace

COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY shared/build.gradle.kts shared/build.gradle.kts
COPY composeApp/build.gradle.kts composeApp/build.gradle.kts
COPY androidApp/build.gradle.kts androidApp/build.gradle.kts
COPY desktopApp/build.gradle.kts desktopApp/build.gradle.kts

RUN chmod +x gradlew \
    && ./gradlew :shared:jvmTest :desktopApp:test --no-daemon --console=plain

# The final image runs the canonical test command without network access.
FROM eclipse-temurin:17.0.14_7-jdk-jammy AS offline-test

WORKDIR /workspace

COPY --from=dependency-preparation /root/.gradle /root/.gradle
COPY . .

RUN chmod +x gradlew \
    && ./gradlew :shared:jvmTest :desktopApp:test --offline --no-daemon --console=plain

CMD ["./gradlew", ":shared:jvmTest", ":desktopApp:test", "--offline", "--no-daemon", "--console=plain"]
