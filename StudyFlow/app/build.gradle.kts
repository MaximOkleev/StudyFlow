import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    jacoco
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

jacoco {
    toolVersion = "0.8.10"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    classDirectories.setFrom(files(sourceSets.main.get().output.asFileTree.matching {
        include("studyflow/data/**")
        include("studyflow/util/**")
        include("studyflow/domain/**")
        exclude(
            "dev.studyflow.app.generated.resources/**",
            "**/generated/**"
        )
    }))
}

tasks.register("coverage") {
    dependsOn(tasks.jacocoTestReport)
    doLast {
        println("Coverage report generated at: build/reports/jacoco/test/html/index.html")
    }
}
