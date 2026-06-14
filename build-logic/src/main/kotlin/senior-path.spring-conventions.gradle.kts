import org.gradle.api.artifacts.VersionCatalogsExtension

// Spring-flavoured convention: base Kotlin conventions + Spring/JPA Kotlin
// compiler plugins (all-open & no-arg) + the Spring Boot BOM + a common test
// stack (Spring test, Testcontainers, Awaitility, MockK). Exercise build files
// then only declare the starters/drivers specific to that exercise.

plugins {
    id("senior-path.kotlin-conventions")
    id("org.jetbrains.kotlin.plugin.spring") // all-open: makes @Component/@Transactional classes proxyable
    id("org.jetbrains.kotlin.plugin.jpa")    // no-arg: synthesises the constructor JPA needs for @Entity
}

private val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    val bom = libs.findLibrary("spring-boot-dependencies").get()
    add("implementation", platform(bom))
    add("testImplementation", platform(bom))

    add("implementation", libs.findLibrary("spring-boot-starter").get())

    add("testImplementation", libs.findLibrary("spring-boot-starter-test").get())
    add("testImplementation", libs.findLibrary("spring-boot-testcontainers").get())
    add("testImplementation", libs.findLibrary("testcontainers-junit").get())
    add("testImplementation", libs.findLibrary("awaitility").get())
    add("testImplementation", libs.findLibrary("mockk").get())
    add("testRuntimeOnly", libs.findLibrary("junit-platform-launcher").get())
}
