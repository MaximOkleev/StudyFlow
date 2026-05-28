import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

group = "dev.studyflow"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "studyflow.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "StudyFlow"
            packageVersion = "1.0.0"
            description = "Desktop study planner with tasks, notes, focus timer and statistics."
            copyright = "2026 StudyFlow"
        }
    }
}
