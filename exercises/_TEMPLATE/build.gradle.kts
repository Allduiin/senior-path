// Copy this folder to exercises/p{PHASE}-{NN}-{slug}/ and register it in settings.gradle.kts.
// Pick the convention that fits:
//   - id("senior-path.spring-conventions")  → Spring + JPA + Testcontainers test stack
//   - id("senior-path.kotlin-conventions")  → plain Kotlin/JVM only
plugins {
    id("senior-path.spring-conventions")
}

dependencies {
    // Declare only what THIS exercise needs; the common stack comes from the convention.
    // Examples:
    // implementation(libs.spring.boot.starter.data.jpa)
    // implementation(libs.spring.boot.starter.amqp)
    // runtimeOnly(libs.postgresql)
    // testImplementation(libs.testcontainers.postgresql)
    // testImplementation(libs.testcontainers.rabbitmq)
}
