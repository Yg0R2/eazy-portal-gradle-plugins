package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.model.EazyPortalServiceParameters
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test

class RootProjectConfigurer(
    parameters: EazyPortalServiceParameters,
    private val project: Project,
) : GradleProjectConfigurer(parameters, project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.kotlin-project-conventions")
        apply("org.eazyportal.plugin.gradle.repositories-conventions")
    }

    override fun TaskContainer.configure() {
        named("jar", Jar::class.java) {
            enabled = false
        }

        named("test", Test::class.java) {
            enabled = false
        }
    }
}
