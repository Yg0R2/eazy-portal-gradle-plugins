package org.eazyportal.plugin.common.gradle

import org.eazyportal.plugin.common.CommonTestFixtures.BUILD_GRADLE_KTS_FILE_NAME
import org.eazyportal.plugin.common.CommonTestFixtures.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.common.CommonTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.CommonTestFixtures.SETTINGS_GRADLE_KTS_FILE_NAME
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import java.io.File
import java.nio.file.Files
import kotlin.io.path.writeLines
import kotlin.io.resolve

class GradleProjectBuilder(
    private val projectDir: File,
    private val projectName: String = PROJECT_NAME,
    private val projectVersion: String = "0.0.1-SNAPSHOT",
    private val settingsPluginIds: Set<String> = emptySet(),
    private val subProjectNames: Set<String> = emptySet(),
    private val projectPluginIds: Set<String> = emptySet(),
) {

    private val extraProjectContent = mutableListOf<String>()
    private val extraSettingsContent = mutableListOf<String>()

    fun build() {
        createGradleRunner(projectDir, "init", "--dsl", "kotlin")
            .build()

        subProjectNames.forEach {
            Files.createDirectories(projectDir.resolve(it).toPath())
        }

        projectDir.resolve(BUILD_GRADLE_KTS_FILE_NAME).toPath()
            .writeLines(createBuildFileContent(projectPluginIds))

        projectDir.resolve(GRADLE_PROPERTIES_FILE_NAME).toPath()
            .writeLines(createPropertiesFileContent(projectVersion))

        projectDir.resolve(SETTINGS_GRADLE_KTS_FILE_NAME).toPath()
            .writeLines(createSettingsFileContent(projectName, settingsPluginIds, subProjectNames))
    }

    fun withExtraProjectConfig(config: String): GradleProjectBuilder =
        apply { extraProjectContent.add(config) }

    fun withExtraSettingsConfig(config: String): GradleProjectBuilder =
        apply { extraSettingsContent.add(config) }

    fun withListPluginsTask(): GradleProjectBuilder =
        apply {
            extraProjectContent.add(
                """
                tasks {
                    register("listPlugins") {
                        val plugins = project.plugins
                
                        doLast {
                            plugins.forEach { println(it::class.java.name) }
                        }
                    }
                }
                """.trimIndent()
            )
        }

    private fun createPropertiesFileContent(projectVersion: String): List<String> =
        listOf("version = $projectVersion")

    private fun createBuildFileContent(
        plugins: Set<String>,
    ): List<String> =
        with(mutableListOf<String>()) {
            generatePluginsBlock(plugins)

            addAll(extraProjectContent)

            this
        }

    private fun createSettingsFileContent(
        projectName: String,
        plugins: Set<String>,
        subProjectNames: Set<String>,
    ): List<String> =
        with(mutableListOf("""rootProject.name = "$projectName"""")) {
            generatePluginsBlock(plugins)

            if (subProjectNames.isNotEmpty()) {
                add("include(")
                addAll(subProjectNames.map { "\"$it\"," })
                add(")")
            }

            addAll(extraSettingsContent)

            this
        }

    private fun MutableList<String>.generatePluginsBlock(plugins: Set<String>) {
        if (plugins.isNotEmpty()) {
            add("plugins {")
            addAll(plugins.map { "id(\"$it\")" })
            add("}")
        }
    }

}
