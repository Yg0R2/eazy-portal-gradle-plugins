package org.eazyportal.plugin.gradle.portal.project.configurer

import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer

class CommonProjectConfigurer(
    project: Project,
) : GradleProjectConfigurer(project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.integration-test-conventions")
        apply("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
        apply("org.eazyportal.plugin.gradle.repositories-conventions")
    }

}
