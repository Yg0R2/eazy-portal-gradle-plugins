package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.configurer.ProjectConfigurerFactory
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Receiver-module entry point (design §5.5):
 * - creates [EazyProjectExtension],
 * - resolves the module's [ProjectType] from its name,
 * - runs the matching [org.eazyportal.gradle.eazyproject.configurer.ProjectConfigurer],
 * - and registers the `eazyDiagnostics` task.
 *
 * Applied directly by a receiver module,
 * or — the normal path — auto-applied by `eazy-settings` to every subproject (never the root),
 * which also feeds [EazyProjectExtension.eazyPortalCoreVersion] (design §6).
 */
class EazyProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project == project.rootProject) {
            throw GradleException("$EAZY_PROJECT_PLUGIN_ID plugin must not be applied to the root project — it is only for subprojects.",)
        }

        val extension = project.extensions.create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java).apply {
            eazyPortalCoreVersion.convention(DefaultVersions.CORE_DEFAULT_VERSION)
            framework.convention(Framework.SPRING_BOOT)
            applyEazyPortalCoreDependencies.convention(true)
        }

        ProjectConfigurerFactory.forType(extension, project)
            .configure()

        registerDiagnostics(project, extension)
    }

    /** Reports the cascade a receiver module's empty `build.gradle.kts` can't otherwise show (design §5.7). */
    private fun registerDiagnostics(
        project: Project,
        eazyProjectExtension: EazyProjectExtension,
    ) {
        project.tasks.register(EAZY_PROJECT_DIAGNOSTICS_TASK_NAME) { task: Task ->
            task.group = "help"
            task.description = "Reports how eazy-project configured this module."

            // Captured as plain data here — CC-safe, no Project (or lazy notation) escapes the doLast (design §5.7).
            val message = Message(
                projectPath = project.path,
                projectType = ProjectType.fromProjectName(project.name),
                wiringSummary = project.wiringSummary(),
                eazyProjectExtension = eazyProjectExtension,
            )

            task.doLast {
                task.logger.lifecycle(message.toString())
            }
        }
    }

    companion object {
        const val EAZY_PROJECT_EXTENSION_NAME = "eazyProject"
        const val EAZY_PROJECT_DIAGNOSTICS_TASK_NAME = "eazyDiagnostics"
        const val EAZY_PROJECT_PLUGIN_ID = "org.eazyportal.gradle.eazy-project"
    }

    private data class Message(
        private val projectPath: String,
        private val projectType: ProjectType,
        private val wiringSummary: WiringSummary,
        private val eazyProjectExtension: EazyProjectExtension,
    ) {

        override fun toString(): String =
            buildString {
                appendLine(
                    """
                    eazy-project diagnostics — $projectPath
                      projectType      : $projectType
                      conventions      : ${wiringSummary.pluginIds}
                      siblings         : ${wiringSummary.siblingDependencies()}
                    """.trimIndent()
                )
                append(createEazyPortalCoreSection().prependIndent("  "))
            }

        private fun createEazyPortalCoreSection(): String =
            if (eazyProjectExtension.applyEazyPortalCoreDependencies.get()) {
                val version = eazyProjectExtension.eazyPortalCoreVersion.get()

                val source = if (version == DefaultVersions.CORE_DEFAULT_VERSION) {
                    "default (DefaultVersions)"
                } else {
                    "DSL override"
                }

                """
                eazyportal-core :
                  version      : $version   (source: $source)
                  framework    : ${eazyProjectExtension.framework.get()}
                  dependencies : ${wiringSummary.eazyPortalCoreDependencies()}
                """.trimIndent()
            } else {
                "eazyportal-core : N/A   (disabled via DSL)"
            }

    }

}
