package org.eazyportal.plugin.gradle.release.project

import org.eazyportal.plugin.gradle.release.project.GradleProjectConstants.GRADLE_PROJECT_FILES
import org.eazyportal.plugin.gradle.release.project.GradleProjectConstants.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.exception.InvalidProjectLocationException
import org.eazyportal.plugin.release.core.project.exception.MissingProjectVersionPropertyException
import org.eazyportal.plugin.release.core.project.exception.MultipleProjectVersionPropertyException
import org.eazyportal.plugin.release.core.project.exception.ProjectVersionPropertyException
import org.eazyportal.plugin.release.core.version.model.Version

class GradleProjectActions<T : Any>(
    private val projectFile: ProjectFile<T>
) : ProjectActions<T> {

    private val gradlePropertiesFile: ProjectFile<T>

    init {
        if (!projectFile.exists() || projectFile.isFile()) {
            throw InvalidProjectLocationException("Invalid Gradle project location: $projectFile")
        }

        gradlePropertiesFile = projectFile.resolve(GRADLE_PROPERTIES_FILE_NAME).also {
            if (!it.exists()) {
                throw InvalidProjectLocationException("'$GRADLE_PROPERTIES_FILE_NAME' file is missing in: $projectFile")
            }
        }
    }

    override fun getVersion(): Version {
        val versions = gradlePropertiesFile.readLines()
            .asSequence()
            .filter { it.isVersionLine() }
            .map { it.getVersionFromLine() }
            .map { Version.of(it) }
            .toList()

        return when (versions.size) {
            0 -> throw MissingProjectVersionPropertyException("The project does not have version property.")
            1 -> versions[0]
            else -> throw MultipleProjectVersionPropertyException("The project has multiple versions: $versions")
        }
    }

    override fun scmFilesToCommit(): Array<String> = arrayOf(".")

    override fun setVersion(version: Version) {
        val versionLines = gradlePropertiesFile.readLines()
            .filter { it.isVersionLine() }

        when (versionLines.size) {
            0 -> throw ProjectVersionPropertyException("The project does not have version property.")
            1 -> {
                gradlePropertiesFile.readText()
                    .replace(versionLines[0], getNewVersionLine(version))
                    .run { gradlePropertiesFile.writeText(this) }
            }

            else -> throw MultipleProjectVersionPropertyException("The project has multiple versions: $versionLines")
        }
    }

    companion object {
        fun isGradleProject(projectFile: ProjectFile<*>): Boolean =
            GRADLE_PROJECT_FILES.any { projectFile.resolve(it).exists() }

        private fun getNewVersionLine(version: Version): String =
            "version = $version"

        private fun String.getVersionFromLine(): String =
            substring(indexOf("=") + 1).trim()

        private fun String.isVersionLine(): Boolean =
            trim().let { it.startsWith("version=") || it.startsWith("version =") }

    }

}