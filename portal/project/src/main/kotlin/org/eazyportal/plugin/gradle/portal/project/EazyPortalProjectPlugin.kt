package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.gradle.portal.project.configurer.ProjectConfigurerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project


class EazyPortalProjectPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        ProjectConfigurerFactory.createProjectConfigurer(target)
            .configure()
    }

}
