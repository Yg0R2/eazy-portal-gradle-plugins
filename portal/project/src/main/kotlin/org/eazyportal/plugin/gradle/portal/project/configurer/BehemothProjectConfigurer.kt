package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.extension.addAsImplementation
import org.eazyportal.plugin.gradle.portal.common.extension.addAsTestFixturesImplementation
import org.eazyportal.plugin.gradle.portal.common.extension.findSubProject
import org.eazyportal.plugin.gradle.portal.common.extension.getSubProject
import org.eazyportal.plugin.gradle.portal.common.extension.testFixtures
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginContainer

class BehemothProjectConfigurer(
    private val project: Project,
) : GradleProjectConfigurer(project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.integration-test-conventions")
        apply("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
        apply("org.eazyportal.plugin.gradle.repositories-conventions")
    }

    override fun DependencyHandler.configure() {
        val apiProject = project.getSubProject(ProjectTypes.API)
        val daoProject = project.findSubProject(ProjectTypes.DAO)
        val commonProject = project.findSubProject(ProjectTypes.COMMON)
        val serviceProject = project.getSubProject(ProjectTypes.SERVICE)

        addAsImplementation(apiProject)
        addAsImplementation(daoProject)
        addAsImplementation(commonProject)
        addAsImplementation(serviceProject)

        addAsTestFixturesImplementation(testFixtures(apiProject))
        addAsTestFixturesImplementation(testFixtures(daoProject))
        addAsTestFixturesImplementation(testFixtures(commonProject))
        addAsTestFixturesImplementation(testFixtures(serviceProject))
    }

}
