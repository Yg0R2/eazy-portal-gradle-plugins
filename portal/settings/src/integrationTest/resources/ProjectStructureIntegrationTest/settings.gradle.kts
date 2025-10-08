rootProject.name = "dummy-project"

plugins {
    id("org.eazyportal.plugin.gradle.portal.settings")
}

include(
    "dummy-api",
    "dummy-behemoth",
    "dummy-client",
    "dummy-common",
    "dummy-dao",
    "dummy-service",
    "dummy-web",
)
