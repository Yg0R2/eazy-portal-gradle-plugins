package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.extension.*
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.project.model.ProjectServiceParameters
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginContainer

class BehemothProjectConfigurer(
    projectParameters: ProjectServiceParameters,
    private val project: Project,
) : GradleProjectConfigurer(projectParameters, project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.integration-test-conventions")
        apply("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
        apply("org.eazyportal.plugin.gradle.repositories-conventions")
    }

    override fun DependencyHandler.configure() {
        runWhenApplyCoreDependenciesEnabled {
            addCoreDependency(ProjectTypes.COMMON)
            addCoreDependency(ProjectTypes.BEHEMOTH)
        }

        val apiProject = project.getSubProject(ProjectTypes.API)
        val commonProject = project.findSubProject(ProjectTypes.COMMON)
        val daoProject = project.findSubProject(ProjectTypes.DAO)
        val serviceProject = project.getSubProject(ProjectTypes.SERVICE)

        addAsImplementation(apiProject)
        addAsImplementation(commonProject)
        addAsImplementation(daoProject)
        addAsImplementation(serviceProject)

        addAsTestFixturesApi(testFixtures(apiProject))
        addAsTestFixturesApi(testFixtures(commonProject))
        addAsTestFixturesApi(testFixtures(daoProject))
        addAsTestFixturesApi(testFixtures(serviceProject))
    }

}
