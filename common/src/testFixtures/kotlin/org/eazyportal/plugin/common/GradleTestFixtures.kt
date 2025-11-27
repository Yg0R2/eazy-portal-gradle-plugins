package org.eazyportal.plugin.common

object GradleTestFixtures {

    const val BUILD_GRADLE_KTS_FILE_NAME = "build.gradle.kts"
    const val GRADLE_PROPERTIES_FILE_NAME = "gradle.properties"
    const val SETTINGS_GRADLE_KTS_FILE_NAME = "settings.gradle.kts"

    const val PROJECT_NAME = "dummy-project"
    val SUBPROJECT_NAMES = setOf(
        "dummy-api",
        "dummy-application",
        "dummy-behemoth",
        "dummy-client",
        "dummy-common",
        "dummy-dao",
        "dummy-service",
        "dummy-web",
    )

}
