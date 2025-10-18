package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.gradle.portal.common.EazyPortalSharedService
import org.eazyportal.plugin.gradle.portal.common.EazyPortalSharedService.Companion.EAZY_PORTAL_SHARED_SERVICE_NAME
import org.eazyportal.plugin.gradle.portal.common.extension.getType
import org.eazyportal.plugin.gradle.portal.project.configurer.ProjectConfigurerFactory
import org.eazyportal.plugin.gradle.portal.project.model.EazyPortalExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType


class EazyPortalProjectPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val eazyPortalSharedService = target.gradle.sharedServices
            .registrations[EAZY_PORTAL_SHARED_SERVICE_NAME]
            .service
            .get() as EazyPortalSharedService

        val extension = if (target == target.rootProject) {
            target.extensions.create("eazyPortal", EazyPortalExtension::class.java).apply {
                projectTypeMap.set(
                    target.allprojects.associateWith { it.getType() }
                )
            }
        } else {
            target.rootProject.extensions.getByType<EazyPortalExtension>()
        }

        ProjectConfigurerFactory.createProjectConfigurer(eazyPortalSharedService.parameters, extension, target)
            .configure()
    }

}
