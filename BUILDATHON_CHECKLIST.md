# Frontier Buildathon 2026 checklist

Source of truth: [official OmniRush Buildathon rules](https://omnirush.ai/buildathon), reviewed 18 October 2026 (IST).

This is an evidence record, not a declaration of legal or contest compliance.
`VERIFIED` means the evidence is present in this workspace or the named local
check passed. `PARTIALLY VERIFIED` means local evidence exists but an external
fact still needs confirmation. `NOT VERIFIED` means the check was not run or
cannot be established from this repository.

This publication restores the documentation from `9c44d16` onto the verified
base `75f2908`, then corrects the license and environment-example requirement.
Historical test or audit claims are not treated as fresh verification. The
publication report records the actual local test result separately.

## Mandatory rules and submission requirements

| Requirement | Evidence | Status | Remaining action |
|---|---|---|---|
| **Solo participation** | The repository cannot establish how many people are entering. | **NOT VERIFIED** | Confirm that this is one person's entry in the OmniRush dashboard and submission. |
| **OmniRush is the only generative AI** | Source files and documentation are present, but session history and use of other AI tools are not recorded in the repository. | **NOT VERIFIED** | Use only OmniRush for project code, text and images; disable other AI autocomplete/tools; submit the relevant OmniRush sessions. |
| **Start fresh during the event** | The official FAQ requires a fresh project during the event; repository history alone cannot establish the complete provenance. | **NOT VERIFIED** | Confirm the project was started during the event and that the submitted OmniRush sessions match the code. |
| **Connected GitHub account and public repository** | `origin` is `https://github.com/Pinjari/Recall.git`; the documentation branch starts from the live `main` SHA verified at publication preparation. Public visibility, connected-account identity and GitHub account age are external facts. | **PARTIALLY VERIFIED** | In the buildathon dashboard, confirm the connected GitHub account is the owner, the repository is public, and the account is at least three days old. |
| **Open-source project** | `LICENSE` is the standard Apache License 2.0 text. | **PARTIALLY VERIFIED** | Verify the public repository exposes this file and that the submitted repository is the connected account's project. |
| **README** | `README.md` documents scope, setup, run, test, build, Docker and platform limitations. | **VERIFIED** | Keep the README synchronized with the final submitted behavior. |
| **Recorded OmniRush sessions** | No session recording or transcript is stored in the repository. | **NOT VERIFIED** | Select the sessions in the OmniRush submission dashboard that produced this project. |
| **Join before registration closes (prize eligibility)** | The official joining deadline is 19 October 2026 at 11:59 PM IST; no registration receipt is available locally. | **NOT VERIFIED** | Confirm timely joining and registration in the dashboard/Unstop entry. |
| **Submit on time** | The official deadline is 20 October 2026 at 11:59 PM IST. No submission receipt is available locally. | **NOT VERIFIED** | Submit or update the dashboard entry before the deadline. |
| **Dashboard submission** | No dashboard submission data is present in the repository. | **NOT VERIFIED** | Add the track, public repository, OmniRush sessions, and any available demo/live links in the OmniRush buildathon dashboard. |
| **Submission ID entered in Unstop** | No Submission ID is present in the repository. | **NOT VERIFIED** | Copy the dashboard Submission ID into the matching Unstop entry and retain the confirmation. |

## Rebuildability and quality checks

These are the official rebuild checklist and judging-oriented reproducibility
checks. They are separate from the entry rules above.

| Check | Evidence | Status | Remaining action |
|---|---|---|---|
| Use git; commit after working steps | The verified base is `75f2908`; this documentation branch preserves it and excludes the unrelated game commits. | **PARTIALLY VERIFIED** | Commit the validated three-file change; do not treat this as proof of the complete development history. |
| Dockerfile | `Dockerfile` has a dependency-preparation stage and an offline-test stage. | **PRESENT; EXECUTION NOT VERIFIED** | On a clean Linux machine, run `docker build --tag recallflow-milestone-a .` and `docker run --rm recallflow-milestone-a`. |
| Clean Linux rebuild | The Docker base images are Linux JDK 17 images, but no Docker build was run in this review. | **NOT VERIFIED** | Verify the Docker build and test command on clean Linux; record the result outside the source tree or in the submission evidence. |
| Offline tests | `Dockerfile` invokes Gradle with `--offline` in its final stage; `make test` is the canonical local test command. | **NOT VERIFIED** | Complete the Docker run with network access disabled and confirm the offline test stage succeeds. |
| One test command | `Makefile` defines `make test`; this row verifies command availability, not a test execution. | **VERIFIED (command defined only)** | Run `make test` before publication and record the result in the publication report; separately verify the offline Linux/Docker run. |
| Dependency lockfiles | `settings-gradle.lockfile` and module lockfiles under `shared/`, `composeApp/`, `androidApp/` and `desktopApp/` are committed. | **VERIFIED** | Regenerate lockfiles only when dependencies intentionally change. |
| Pinned toolchain | `.java-version` pins JDK `17.0.14`; `gradle/wrapper/gradle-wrapper.properties` pins the Gradle distribution; `gradle/libs.versions.toml` pins project dependency versions. | **VERIFIED** | Keep these pins and the wrapper in the public repository. |
| **Add `.env.example` (explicit rebuild requirement)** | `.env.example` is present and contains comments stating that the local app currently requires no environment variables or secrets; it invents no configuration keys. | **VERIFIED (file/content only)** | Keep this file even while no secret configuration is required; if configuration is added, document names with fake values only. |
| Secret handling | `.gitignore` excludes `local.properties`; the publication change set contains only documentation and a comments-only environment example. No complete repository or dependency secret audit is claimed. | **PARTIALLY VERIFIED** | Scan the publication change set before committing; never add real credentials or private configuration. |
| No machine-specific absolute paths | The publication change set is limited to the license, checklist and environment example; the publication scan checks these files, not every historical revision. | **NOT VERIFIED in this checklist** | Run the targeted path scan before committing; keep generated output, SDK paths and local configuration ignored. |
| No generated build output in Git | `.gitignore` excludes `**/build/`; no generated output is part of the intended three-file publication change. | **VERIFIED (change-set scope only)** | Do not force-add generated APKs, databases, reports or caches. |

## Platform verification

The repository documents several targets, but a platform configuration is not
evidence that that platform was built. Per the requested review, every build
not executed and observed here remains **NOT VERIFIED**.

| Target | Evidence | Status | Remaining action |
|---|---|---|---|
| Android debug APK and connected-device test | Android module and device-test configuration exist; this review did not run an Android SDK/device build. | **NOT VERIFIED** | Run `make android-debug` and the connected Android device test on an Android-capable Linux environment. |
| Linux desktop build/package | Linux DEB configuration exists; no Linux build/package was executed here. | **NOT VERIFIED** | Build and, if required, package the desktop target on Linux. |
| Windows desktop build/package | Windows MSI configuration exists; no Windows build/package was executed here. | **NOT VERIFIED** | Build/package on Windows if claiming Windows support. |
| macOS desktop build/package | The README records a prior macOS smoke path, but it was not re-run as part of this documentation change. | **NOT VERIFIED for this review** | Re-run the intended smoke/package command if macOS evidence is needed in the submission. |

## Recommendations

| Recommendation | Evidence | Status | Remaining action |
|---|---|---|---|
| **Demo video** (recommended, not a mandatory entry rule) | No video URL is recorded in the repository. | **NOT VERIFIED** | Record a 2–3 minute walkthrough and add the link in the dashboard. |
| **Live link** (optional; useful when the project can be hosted) | RecallFlow is a native Android/desktop project; no live URL is recorded. | **NOT VERIFIED / OPTIONAL** | Add a live link only if a useful hosted build exists. |

## License and asset review

This is not legal advice and is not a complete license or dependency audit.

- `LICENSE` is copied without wording changes from the
  [canonical Apache-2.0 text](https://www.apache.org/licenses/LICENSE-2.0.txt).
  A byte-for-byte comparison is required before committing; the publication
  report records its actual result. The canonical appendix placeholders are
  retained; no copyright holder or year is invented.
- The declared dependencies are listed in `gradle/libs.versions.toml`; the
  resolved graph is represented by the committed Gradle lockfiles. Their full
  license compatibility and packaged attribution obligations are **NOT VERIFIED**
  by this documentation-only publication.
- This change introduces no application code, dependency, image, font, audio,
  video or model asset, and does not integrate EmbeddingGemma.
- No `NOTICE` file is added. Whether distributed dependencies or bundled native
  artifacts require preserving upstream NOTICE or other attribution files is
  **NOT VERIFIED** and must be reviewed before distributing packaged binaries.

### Unresolved human review

Before distributing packaged binaries, review the complete resolved dependency
graph and the license/NOTICE files inside native or bundled artifacts (especially
Skiko and SQLite components), and verify the attribution obligations for any
future assets. This repository review does not establish legal certainty.
