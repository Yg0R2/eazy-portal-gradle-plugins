plugins {
    base
}

tasks.register("publish") {
    group = "publishing"
    description = "Placeholder task to be abel to call `publish` on the root level without the `mavan-publish` plugin."
}

// Subprojects are swept in automatically by Gradle's built-in same-name task matching.
// The included builds are a separate Gradle build, so they need to be wired in explicitly.
listOf("clean", "build", "check", "publish").forEach { taskName ->
    tasks.named(taskName) {
        dependsOn(gradle.includedBuilds.map { it.task(":$taskName") })
    }
}
