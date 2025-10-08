package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.extension.addAsCompileOnly
import org.eazyportal.plugin.gradle.portal.common.extension.addAsTestFixturesImplementation
import org.eazyportal.plugin.gradle.portal.common.extension.addAsTestImplementation
import org.eazyportal.plugin.gradle.portal.common.extension.findSubProject
import org.eazyportal.plugin.gradle.portal.common.extension.testFixtures
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginContainer

class ApiProjectConfigurer(
    private val project: Project,
) : GradleProjectConfigurer(project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.integration-test-conventions")
        apply("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
        apply("org.eazyportal.plugin.gradle.repositories-conventions")
    }

    override fun DependencyHandler.configure() {
        val commonProject = project.findSubProject(ProjectTypes.COMMON)

        addAsCompileOnly(commonProject)

        addAsTestFixturesImplementation(testFixtures(commonProject))

        addAsTestImplementation(commonProject)
    }

}
