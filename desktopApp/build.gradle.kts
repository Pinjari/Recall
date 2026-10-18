import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
}

val desktopLockPlatform = when {
    System.getProperty("os.name").contains("mac", ignoreCase = true) &&
        System.getProperty("os.arch").lowercase() in setOf("aarch64", "arm64") -> "macos-arm64"

    System.getProperty("os.name").contains("linux", ignoreCase = true) &&
        System.getProperty("os.arch").lowercase() in setOf("aarch64", "arm64") -> "linux-arm64"

    System.getProperty("os.name").contains("linux", ignoreCase = true) -> "linux-x64"

    else -> error(
        "No committed desktop dependency lock exists for " +
            "${System.getProperty("os.name")} / ${System.getProperty("os.arch")}.",
    )
}
require(desktopLockPlatform in setOf("macos-arm64", "linux-x64")) {
    "No committed desktop dependency lock exists for $desktopLockPlatform."
}
dependencyLocking {
    lockFile = file("gradle.lockfile.$desktopLockPlatform")
}

kotlin {
    jvmToolchain(17)
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(libs.sqldelight.runtime)
    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "com.recallflow.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RecallFlow"
            packageVersion = "0.1.0"
            description = "A local-first, text-first notebook foundation"
            vendor = "RecallFlow"
            modules("java.sql")
        }
    }
}
