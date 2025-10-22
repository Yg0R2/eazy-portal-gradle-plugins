package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.project.model.ProjectServiceParameters
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test

class RootProjectConfigurer(
    projectParameters: ProjectServiceParameters,
    private val project: Project,
) : GradleProjectConfigurer(projectParameters, project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.conventions.kotlin-project-conventions")
        apply("org.eazyportal.plugin.gradle.conventions.repositories-conventions")
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
