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

    test {
        val subprojectTestsTasks = rootProject.subprojects
            .flatMap {
                listOf(
                    ":${it.name}:test",
                    ":${it.name}:integrationTest",
                )
            }.toTypedArray()

        dependsOn(subprojectTestsTasks)
    }
}
