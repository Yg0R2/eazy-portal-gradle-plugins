plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

tasks {
    register("publish") {
        val includedBuildPublishTasks = gradle.includedBuilds
            .mapNotNull { it.task(":publish") }
            .toTypedArray()
        val subprojectPublishTasks = rootProject.subprojects
            .mapNotNull { it.tasks.findByName("publish") }
            .toTypedArray()

        dependsOn(*includedBuildPublishTasks, *subprojectPublishTasks)
    }
}
