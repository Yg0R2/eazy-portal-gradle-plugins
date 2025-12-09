package org.eazyportal.plugin.common.integration.test.gradle

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures
import java.io.File
import kotlin.io.path.writeLines

class GradleProjectBuilder(
    private val projectDir: File,
    private val projectName: String = GradleTestFixtures.PROJECT_NAME,
    private val projectVersion: String = "0.0.1-SNAPSHOT",
    @Deprecated("")
    private val projectPluginIds: Set<String> = emptySet(), // TODO: add withRelease, withEazyPortal
) {

    private val _projectPluginIds = mutableSetOf<String>()
    private val extraProjectContent = mutableListOf<String>()

    private val settingsPluginIds = mutableSetOf<String>()
    private val extraSettingsContent = mutableListOf<String>()

    private val subprojects = mutableMapOf<String, GradleProjectBuilder>()

    fun build() {
        GradleUtils.createGradleRunner(projectDir, "--no-configuration-cache", "init", "--dsl", "kotlin")
            .build()

        projectDir.resolve(GradleTestFixtures.BUILD_GRADLE_KTS_FILE_NAME).toPath()
            .writeLines(createBuildFileContent(projectPluginIds + _projectPluginIds, extraProjectContent))

        subprojects.forEach { (subprojectName, subprojectBuilder) ->
            projectDir.resolve(subprojectName)
                .also { it.mkdirs() }
                .resolve(GradleTestFixtures.BUILD_GRADLE_KTS_FILE_NAME).toPath()
                .writeLines(
                    createBuildFileContent(
                        subprojectBuilder.projectPluginIds + subprojectBuilder._projectPluginIds,
                        subprojectBuilder.extraProjectContent,
                    )
                )
        }

        projectDir.resolve(GradleTestFixtures.GRADLE_PROPERTIES_FILE_NAME).toPath()
            .writeLines(createPropertiesFileContent(projectVersion))

        projectDir.resolve(GradleTestFixtures.SETTINGS_GRADLE_KTS_FILE_NAME).toPath()
            .writeLines(createSettingsFileContent(projectName, settingsPluginIds, subprojects.keys))
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

    fun withEazyPortalReleasePlugin(): GradleProjectBuilder =
        apply { withProjectPlugins("org.eazyportal.plugin.gradle.release-gradle") }

    fun withSettingPlugins(vararg pluginIds: String): GradleProjectBuilder =
        apply { settingsPluginIds.addAll(pluginIds) }

    fun withEazyPortalSettingsPlugin(): GradleProjectBuilder =
        apply { withSettingPlugins("org.eazyportal.plugin.gradle.portal-settings") }

    fun withSubproject(
        subprojectName: String,
        subprojectInitBlock: GradleProjectBuilder.() -> Unit,
    ): GradleProjectBuilder =
        apply {
            subprojects[subprojectName] = GradleProjectBuilder(
                projectDir.resolve(subprojectName),
                subprojectName,
            ).apply { subprojectInitBlock(this) }
        }

    fun withSubprojectNames(vararg subprojectNames: String): GradleProjectBuilder =
        apply {
            subprojectNames.forEach {
                subprojects[it] = GradleProjectBuilder(projectDir.resolve(it), it)
            }
        }

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
                addAll(subProjectNames.map { "\"$it\"," })
                add(")")
            }

            addAll(extraSettingsContent)

            this
        }.also { println(it.joinToString(System.lineSeparator())) }

    private fun MutableList<String>.generatePluginsBlock(plugins: Set<String>) {
        if (plugins.isNotEmpty()) {
            add("plugins {")
            addAll(plugins.map { "id(\"$it\")" })
            add("}")
        }
    }

}