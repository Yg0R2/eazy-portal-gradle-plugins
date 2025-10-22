package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.extension.findSubProject
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.project.model.ProjectServiceParameters
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginContainer

class DaoProjectConfigurer(
    projectParameters: ProjectServiceParameters,
    private val project: Project,
) : GradleProjectConfigurer(projectParameters, project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.conventions.integration-test-conventions")
        apply("org.eazyportal.plugin.gradle.conventions.kotlin-lib-conventions")
        apply("org.eazyportal.plugin.gradle.conventions.repositories-conventions")
    }

    override fun DependencyHandler.configure() {
        runWhenApplyCoreDependenciesEnabled {
            addCoreDependency(ProjectTypes.COMMON)
            addCoreDependency(ProjectTypes.DAO)
        }

        addProjectDependency(project.findSubProject(ProjectTypes.COMMON))
    }

}
