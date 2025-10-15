package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.extension.addAsCompileOnly
import org.eazyportal.plugin.gradle.portal.common.extension.addAsTestFixturesImplementation
import org.eazyportal.plugin.gradle.portal.common.extension.addAsTestImplementation
import org.eazyportal.plugin.gradle.portal.common.extension.findSubProject
import org.eazyportal.plugin.gradle.portal.common.extension.testFixtures
import org.eazyportal.plugin.gradle.portal.common.model.EazyPortalServiceParameters
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginContainer

class ServiceProjectConfigurer(
    parameters: EazyPortalServiceParameters,
    private val project: Project,
) : GradleProjectConfigurer(parameters, project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.integration-test-conventions")
        apply("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
        apply("org.eazyportal.plugin.gradle.repositories-conventions")
    }

    override fun DependencyHandler.configure() {
        val apiProject = project.findSubProject(ProjectTypes.API)
        val daoProject = project.findSubProject(ProjectTypes.DAO)
        val commonProject = project.findSubProject(ProjectTypes.COMMON)

        addAsCompileOnly(apiProject)
        addAsCompileOnly(daoProject)
        addAsCompileOnly(commonProject)

        addAsTestFixturesImplementation(testFixtures(apiProject))
        addAsTestFixturesImplementation(testFixtures(daoProject))
        addAsTestFixturesImplementation(testFixtures(commonProject))

        addAsTestImplementation(apiProject)
        addAsTestImplementation(daoProject)
        addAsTestImplementation(commonProject)
    }

}
