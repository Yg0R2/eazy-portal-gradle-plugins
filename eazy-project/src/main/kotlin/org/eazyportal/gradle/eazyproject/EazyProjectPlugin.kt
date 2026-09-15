package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.configurer.ProjectConfigurerFactory
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
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
 * - and registers the `dummyDiagnostics` task.
 *
 * Applied directly by a receiver module,
 * or — the normal path — auto-applied by `«dummy-settings»` to every subproject (never the root),
 * which also feeds [EazyProjectExtension.eazyPortalCoreVersion] (design §6).
 */
class EazyProjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project == project.rootProject) {
            throw GradleException("org.eazyportal.gradle.eazy-project plugin must not be applied to the root project — it is only for subprojects.",)
        }

        val extension = project.extensions.create("eazyProject", EazyProjectExtension::class.java).apply {
            eazyPortalCoreVersion.convention(DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION)
        }

        ProjectConfigurerFactory.forType(extension, project)
            .configure()

        registerDiagnostics(project, extension)
    }

    /** Reports the cascade a receiver module's empty `build.gradle.kts` can't otherwise show (design §5.7). */
    private fun registerDiagnostics(
        project: Project,
        extension: EazyProjectExtension,
    ) {
        project.tasks.register("dummyDiagnostics") { task: Task ->
            task.group = "help"
            task.description = "Reports how eazy-project configured this module."

            // Captured as plain data here — CC-safe, no Project escapes the doLast (design §5.7).
            val summary = project.wiringSummary()
            val eazyPortalCoreVersion = extension.eazyPortalCoreVersion.get()

            task.doLast {
                task.logger.lifecycle(
                    """
                    eazy-project diagnostics — ${project.path}
                      archetype           : ${ProjectType.fromProjectName(project.name)}
                      convention          : ${summary.pluginIds}
                      siblings            : ${summary.siblings}
                      eazy-portal-core        : ${summary.eazyPortalCore}   (versions via the eazy-portal-core BOM)
                      eazyPortalCoreVersion  : $eazyPortalCoreVersion   (source: ${getExampleCoreVersionSource(eazyPortalCoreVersion)})
                    """.trimIndent(),
                )
            }
        }
    }

    private fun getExampleCoreVersionSource(eazyPortalCoreVersion: String): String =
        if (eazyPortalCoreVersion == DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION) {
            "default (DefaultVersions)"
        } else {
            "«dummy-settings» DSL override"
        }

}
