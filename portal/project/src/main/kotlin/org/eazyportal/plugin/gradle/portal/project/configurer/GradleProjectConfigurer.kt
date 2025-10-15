package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.model.EazyPortalServiceParameters
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskContainer

open class GradleProjectConfigurer(
    parameters: EazyPortalServiceParameters,
    private val project: Project,
) : ProjectConfigurer {

    final override fun configure() {
        project.plugins.configure()

        project.tasks.configure()

        project.dependencies.configure()
    }

    protected open fun DependencyHandler.configure() {
    }

    protected open fun PluginContainer.configure() {
    }

    protected open fun TaskContainer.configure() {
    }

}
