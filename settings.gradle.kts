rootProject.name = "senior-path"

pluginManagement {
    // build-logic supplies the precompiled `senior-path.*-conventions` plugins.
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
    }
    // `libs` catalog is auto-created from gradle/libs.versions.toml.
}

// --- Exercises ---------------------------------------------------------------
// Each exercise is a flat-named project whose directory lives under exercises/.
// The _TEMPLATE folder is intentionally NOT included (it is a copy source only).
fun exercise(name: String) {
    include(":$name")
    project(":$name").projectDir = file("exercises/$name")
}

exercise("p1-01-tx-self-invocation")
exercise("p1-02-lost-update")
