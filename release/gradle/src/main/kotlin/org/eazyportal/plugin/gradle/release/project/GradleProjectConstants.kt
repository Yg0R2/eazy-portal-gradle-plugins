package org.eazyportal.plugin.gradle.release.project

object GradleProjectConstants {

    const val GRADLE_PROPERTIES_FILE_NAME = "gradle.properties"

    val GRADLE_PROJECT_FILES = setOf(
        "build.gradle",
        "build.gradle.kts",
        "settings.gradle",
        "settings.gradle.kts"
    )

}
