rootProject.name = "dummy-project"

plugins {
    id("org.eazyportal.plugin.gradle.portal.settings")
}

eazyPortal {
    applyCoreDependencies = false
}

listOf<String>(
    "application",
    "api",
    "behemoth",
    "client",
    "common",
    "dao",
    "service",
    "web",
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}
