plugins {
    id("senior-path.spring-conventions")
}

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.testcontainers.postgresql)
}
