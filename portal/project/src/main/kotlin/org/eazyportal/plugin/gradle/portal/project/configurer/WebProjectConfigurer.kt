package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.extension.findSubProject
import org.eazyportal.plugin.gradle.portal.common.extension.getSubProject
import org.eazyportal.plugin.gradle.portal.common.model.EazyPortalServiceParameters
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginContainer

class WebProjectConfigurer(
    parameters: EazyPortalServiceParameters,
    private val project: Project,
) : GradleProjectConfigurer(parameters, project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.integration-test-conventions")
        apply("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
        apply("org.eazyportal.plugin.gradle.repositories-conventions")
    }

    override fun DependencyHandler.configure() {
        runWhenApplyCoreDependenciesEnabled {
            addCoreDependency(ProjectTypes.COMMON)
            addCoreDependency(ProjectTypes.WEB)
        }

        addProjectDependency(project.getSubProject(ProjectTypes.API))
        addProjectDependency(project.findSubProject(ProjectTypes.COMMON))
        addProjectDependency(project.findSubProject(ProjectTypes.DAO))
        addProjectDependency(project.getSubProject(ProjectTypes.SERVICE))
    }

}
