package org.eazyportal.plugin.common.integration.test

object ProjectTestFixtures {

    const val BUILD_GRADLE_KTS_FILE_NAME = "build.gradle.kts"
    const val GRADLE_PROPERTIES_FILE_NAME = "gradle.properties"
    const val SETTINGS_GRADLE_KTS_FILE_NAME = "settings.gradle.kts"

    const val PROJECT_NAME = "dummy-project"
    @Deprecated("")
    const val SUBMODULE_NAME = "dummy-ui" // TODO: have more then one submodule
    val SUBMODULE_NAMES = arrayOf("dummy-ui")
    val SUBPROJECT_NAMES = sortedSetOf(
        "dummy-api",
        "dummy-application",
        "dummy-behemoth",
        "dummy-client",
        "dummy-common",
        "dummy-dao",
        "dummy-service",
        "dummy-web",
        *SUBMODULE_NAMES,
    ).toTypedArray()

}