plugins {
    id("senior-path.spring-conventions")
}

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.rabbitmq)
    testImplementation(libs.awaitility)
}
