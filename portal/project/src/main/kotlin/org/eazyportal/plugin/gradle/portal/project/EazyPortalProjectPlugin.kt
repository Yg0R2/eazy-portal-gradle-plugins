package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.gradle.portal.common.EazyPortalSharedService
import org.eazyportal.plugin.gradle.portal.common.EazyPortalSharedService.Companion.EAZY_PORTAL_SHARED_SERVICE_NAME
import org.eazyportal.plugin.gradle.portal.project.configurer.ProjectConfigurerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get


class EazyPortalProjectPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val eazyPortalSharedService = target.gradle.sharedServices
            .registrations[EAZY_PORTAL_SHARED_SERVICE_NAME]
            .service
            .get() as EazyPortalSharedService

        ProjectConfigurerFactory.createProjectConfigurer(eazyPortalSharedService.parameters, target)
            .configure()
    }

}
