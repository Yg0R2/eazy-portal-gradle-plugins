package org.eazyportal.plugin.common.integration.test.gradle

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.BUILD_GRADLE_KTS_FILE_NAME
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.SETTINGS_GRADLE_KTS_FILE_NAME
import java.io.File
import kotlin.io.path.writeLines

class GradleProjectBuilder(
    private val projectDir: File,
) {

    var projectName: String = PROJECT_NAME
    var projectVersion: String = "0.0.1-SNAPSHOT"

    private val projectPluginIds = mutableSetOf<String>()
    private val extraProjectContent = mutableListOf<String>()

    private val settingsPluginIds = mutableSetOf<String>()
    private val extraSettingsContent = mutableListOf<String>()

    private val subprojects = mutableMapOf<String, GradleProjectBuilder>()

    fun build() {
        projectDir.mkdirs()

        GradleUtils.createGradleRunner(projectDir, "--no-configuration-cache", "init", "--dsl", "kotlin")
            .build()

        projectDir.resolve(BUILD_GRADLE_KTS_FILE_NAME)
            .toPath()
            .writeLines(createBuildFileContent(projectPluginIds, extraProjectContent))

        subprojects.forEach { (subprojectName, subprojectBuilder) ->
            projectDir.resolve(subprojectName)
                .also { it.mkdirs() }
                .resolve(BUILD_GRADLE_KTS_FILE_NAME).toPath()
                .writeLines(
                    createBuildFileContent(
                        subprojectBuilder.projectPluginIds,
                        subprojectBuilder.extraProjectContent,
                    )
                )
        }

        projectDir.resolve(GRADLE_PROPERTIES_FILE_NAME)
            .toPath()
            .writeLines(createPropertiesFileContent(projectVersion))

        projectDir.resolve(SETTINGS_GRADLE_KTS_FILE_NAME)
            .toPath()
            .writeLines(createSettingsFileContent(projectName, settingsPluginIds, subprojects.keys))
    }

    //------------------------------------------------------
    // build.gradle.kts Configuration
    //------------------------------------------------------

    fun withExtraProjectConfig(config: String): GradleProjectBuilder =
        apply {
            extraProjectContent.add(config)
        }

    fun withEazyPortalProjectPlugin(): GradleProjectBuilder =
        apply {
            withProjectPlugins("org.eazyportal.plugin.gradle.portal-project")
        }

    fun withEazyPortalReleasePlugin(): GradleProjectBuilder =
        apply {
            withProjectPlugins("java", "org.eazyportal.plugin.gradle.release-gradle")
        }

    fun withProjectPlugins(vararg pluginIds: String): GradleProjectBuilder =
        apply {
            projectPluginIds.addAll(pluginIds)
        }

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

    //------------------------------------------------------
    // settings.gradle.kts Configuration
    //------------------------------------------------------

    fun withExtraSettingsConfig(config: String): GradleProjectBuilder =
        apply {
            extraSettingsContent.add(config)
        }

    fun withSettingPlugins(vararg pluginIds: String): GradleProjectBuilder =
        apply {
            settingsPluginIds.addAll(pluginIds)
        }

    fun withEazyPortalSettingsPlugin(): GradleProjectBuilder =
        apply {
            withSettingPlugins("org.eazyportal.plugin.gradle.portal-settings")
        }

    fun withSubproject(
        subprojectName: String,
        subprojectInitBlock: GradleProjectBuilder.() -> Unit = {},
    ): GradleProjectBuilder =
        apply {
            subprojects[subprojectName] = GradleProjectBuilder(
                projectDir.resolve(subprojectName),
            ).apply {
                projectName = subprojectName

                subprojectInitBlock(this)
            }
        }

    fun withSubprojects(vararg subprojectNames: String): GradleProjectBuilder =
        apply {
            subprojectNames.forEach {
                withSubproject(it)
            }
        }

    //------------------------------------------------------
    // Helpers
    //------------------------------------------------------

    private fun createPropertiesFileContent(projectVersion: String): List<String> =
        listOf("version = $projectVersion")

    private fun createBuildFileContent(
        plugins: Set<String>,
        extraProjectContent: List<String>,
    ): List<String> =
        with(mutableListOf<String>()) {
            generatePluginsBlock(plugins)

            addAll(extraProjectContent)

            this
        }.also { println(it.joinToString(System.lineSeparator())) }

    private fun createSettingsFileContent(
        projectName: String,
        plugins: Set<String>,
        subProjectNames: Set<String>,
    ): List<String> =
        with(mutableListOf("""rootProject.name = "$projectName"""")) {
            generatePluginsBlock(plugins)

            if (subProjectNames.isNotEmpty()) {
                add("include(")
                addAll(subProjectNames.map { "    \"$it\"," })
                add(")")
            }

            addAll(extraSettingsContent)

            this
        }.also { println(it.joinToString(System.lineSeparator())) }

    private fun MutableList<String>.generatePluginsBlock(plugins: Set<String>) {
        if (plugins.isNotEmpty()) {
            add("plugins {")
            addAll(plugins.map { "    id(\"$it\")" })
            add("}")
        }
    }

}