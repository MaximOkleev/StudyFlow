import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.JavaExec

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    jacoco
}

group = "dev.studyflow"
version = "1.1.5"

val nativeAccessJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Compose Desktop/Skiko loads native libraries. Newer JDKs print a restricted-native-access
// warning unless native access is explicitly enabled for unnamed modules.
tasks.withType<JavaExec>().configureEach {
    jvmArgs(nativeAccessJvmArgs)
}

compose.desktop {
    application {
        mainClass = "studyflow.MainKt"
        jvmArgs += nativeAccessJvmArgs

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "StudyFlow"
            packageVersion = "1.1.5"
            description = "Desktop study planner with tasks, notes, focus timer and statistics."
            copyright = "2026 StudyFlow"
            windows {
                iconFile.set(project.file("src/main/resources/icons/studyflow-icon.ico"))
            }
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


tasks.register<JavaExec>("runWeb") {
    group = "application"
    description = "Run local StudyFlow Web server at http://127.0.0.1:5173 using the same SQLite database as desktop."
    mainClass.set("studyflow.web.StudyFlowWebServerKt")
    classpath = sourceSets.main.get().runtimeClasspath
    workingDir = rootProject.projectDir
    args("--port=5173")
}
