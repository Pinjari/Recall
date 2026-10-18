# The first stage warms the Gradle distribution and dependency caches online.
FROM eclipse-temurin:17.0.14_7-jdk-jammy AS dependency-preparation

WORKDIR /workspace

COPY . .

RUN chmod +x gradlew \
    && ./gradlew :shared:generateCommonMainRecallDatabaseInterface :shared:jvmTest :desktopApp:test --no-daemon --console=plain

# The final image runs the canonical test command without network access.
FROM eclipse-temurin:17.0.14_7-jdk-jammy AS offline-test

WORKDIR /workspace

RUN apt-get update \
    && apt-get install --no-install-recommends --yes make \
    && rm -rf /var/lib/apt/lists/*

COPY --from=dependency-preparation /root/.gradle /root/.gradle
COPY . .

ENV GRADLE_TEST_ARGS=--offline

RUN chmod +x gradlew \
    && make test

CMD ["make", "test"]
