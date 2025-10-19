package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.extension.addAsImplementation
import org.eazyportal.plugin.gradle.portal.common.extension.addAsTestFixturesApi
import org.eazyportal.plugin.gradle.portal.common.extension.addAsTestFixturesImplementation
import org.eazyportal.plugin.gradle.portal.common.extension.testFixtures
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.project.CoreDependencyNotationFactory.createCoreDependencyNotation
import org.eazyportal.plugin.gradle.portal.project.model.ProjectServiceParameters
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskContainer

open class GradleProjectConfigurer(
    private val projectParameters: ProjectServiceParameters,
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

    protected fun DependencyHandler.addCoreDependency(projectType: ProjectTypes) {
        val coreDependency = projectParameters.settingsParameters.get().coreVersion.let {
            createCoreDependencyNotation(projectType, it)
        }

        addAsImplementation(coreDependency)

        addAsTestFixturesImplementation(testFixtures(coreDependency))
    }

    protected fun DependencyHandler.addProjectDependency(target: Project?) {
        addAsImplementation(target)

        addAsTestFixturesApi(testFixtures(target))
    }

    protected fun runWhenApplyCoreDependenciesEnabled(block: () -> Unit) {
        if (projectParameters.settingsParameters.get().applyCoreDependencies.getOrElse(false)) {
            block()
        }
    }

}
