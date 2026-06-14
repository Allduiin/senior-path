import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Base convention shared by every module: Kotlin/JVM on a Java 21 toolchain,
// JUnit 5 as the test platform. Keeps each exercise build file minimal.

plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        // Treat Kotlin warnings as informational here; exercises are learning artifacts.
        allWarningsAsErrors.set(false)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
