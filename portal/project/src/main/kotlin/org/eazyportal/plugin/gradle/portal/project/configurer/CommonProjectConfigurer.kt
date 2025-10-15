package org.eazyportal.plugin.gradle.portal.project.configurer

import org.eazyportal.plugin.gradle.portal.common.model.EazyPortalServiceParameters
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer

class CommonProjectConfigurer(
    parameters: EazyPortalServiceParameters,
    project: Project,
) : GradleProjectConfigurer(parameters, project) {

    override fun PluginContainer.configure() {
        apply("org.eazyportal.plugin.gradle.integration-test-conventions")
        apply("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
        apply("org.eazyportal.plugin.gradle.repositories-conventions")
    }

}
