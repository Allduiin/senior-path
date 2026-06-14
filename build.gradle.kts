// Root build: no production code lives here. Exercises are independent modules
// that each apply a `senior-path.*-conventions` plugin from build-logic/.
// This file exists mainly to host aggregate/util tasks if needed later.

tasks.register("labInfo") {
    group = "help"
    description = "Prints how the learning lab is wired."
    doLast {
        println(
            """
            senior-path — Senior Java/Kotlin learning lab
            ----------------------------------------------
            Exercises:   ./exercises/<pN-NN-slug>/   (modules)
            Conventions: ./build-logic/              (shared build setup)
            Docs:        ./docs/roadmap.md, ./docs/progress-log.md
            Run one exercise's tests:  ./gradlew :<pN-NN-slug>:test
            """.trimIndent()
        )
    }
}
