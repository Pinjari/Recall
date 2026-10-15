import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
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
