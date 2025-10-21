plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

tasks {
    register("publish") {
        val includedBuildPublishTasks = gradle.includedBuilds
            .filter { it.name != "build-logic" }
            .map { it.task(":publish") }
            .toTypedArray()
        val subprojectPublishTasks = rootProject.subprojects
            .mapNotNull { it.tasks.findByName("publish") }
            .toTypedArray()

        dependsOn(*includedBuildPublishTasks, *subprojectPublishTasks)
    }
}
