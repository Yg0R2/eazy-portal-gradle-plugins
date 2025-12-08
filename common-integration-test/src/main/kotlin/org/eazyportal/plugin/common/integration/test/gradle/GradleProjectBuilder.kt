package org.eazyportal.plugin.common.integration.test.gradle

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures
import java.io.File
import java.nio.file.Files
import kotlin.io.path.writeLines

class GradleProjectBuilder(
    private val projectDir: File,
    private val projectName: String = GradleTestFixtures.PROJECT_NAME,
    private val projectVersion: String = "0.0.1-SNAPSHOT",
    @Deprecated("")
    private val settingsPluginIds: Set<String> = emptySet(),
    @Deprecated("")
    private val subProjectNames: Set<String> = emptySet(),
    @Deprecated("")
    private val projectPluginIds: Set<String> = emptySet(), // TODO: add withRelease, withEazyPortal
) {

    private val _projectPluginIds = mutableSetOf<String>()
    private val extraProjectContent = mutableListOf<String>()

    private val _settingsPluginIds = mutableSetOf<String>()
    private val extraSettingsContent = mutableListOf<String>()

    private val _subprojectName = mutableSetOf<String>()

    fun build() {
        GradleUtils.createGradleRunner(projectDir, "--no-configuration-cache", "init", "--dsl", "kotlin")
            .build()

        (subProjectNames + _subprojectName).forEach {
            Files.createDirectories(projectDir.resolve(it).toPath())
        }

        projectDir.resolve(GradleTestFixtures.BUILD_GRADLE_KTS_FILE_NAME).toPath()
            .writeLines(createBuildFileContent(projectPluginIds + _projectPluginIds).also { println(it.joinToString(System.lineSeparator())) })

        projectDir.resolve(GradleTestFixtures.GRADLE_PROPERTIES_FILE_NAME).toPath()
            .writeLines(createPropertiesFileContent(projectVersion))

        projectDir.resolve(GradleTestFixtures.SETTINGS_GRADLE_KTS_FILE_NAME).toPath()
            .writeLines(createSettingsFileContent(projectName, settingsPluginIds + _settingsPluginIds, subProjectNames + _subprojectName).also { println(it.joinToString(System.lineSeparator())) })
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

    fun withProjectPlugins(vararg pluginIds: String): GradleProjectBuilder =
        apply { _projectPluginIds.addAll(pluginIds) }

    fun withEazyPortalProjectPlugin(): GradleProjectBuilder =
        apply { withProjectPlugins("org.eazyportal.plugin.gradle.portal-project") }

    fun withSettingPlugins(vararg pluginIds: String): GradleProjectBuilder =
        apply { _settingsPluginIds.addAll(pluginIds) }

    fun withEazyPortalSettingsPlugin(): GradleProjectBuilder =
        apply { withSettingPlugins("org.eazyportal.plugin.gradle.portal-settings") }

    fun withSubprojectNames(vararg subprojectNames: String): GradleProjectBuilder =
        apply { _subprojectName.addAll(subprojectNames) }

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