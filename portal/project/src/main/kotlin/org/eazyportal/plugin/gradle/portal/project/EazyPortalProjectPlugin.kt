package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.gradle.portal.common.EazyPortalSettingsSharedService.Companion.EAZY_PORTAL_SETTINGS_SHARED_SERVICE_NAME
import org.eazyportal.plugin.gradle.portal.common.extension.getType
import org.eazyportal.plugin.gradle.portal.common.model.SettingsServiceParameters
import org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectSharedService.Companion.EAZY_PORTAL_PROJECT_SHARED_SERVICE_NAME
import org.eazyportal.plugin.gradle.portal.project.configurer.ProjectConfigurerFactory
import org.eazyportal.plugin.gradle.portal.project.model.ProjectServiceParameters
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.get

class EazyPortalProjectPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (target == target.rootProject) {
            registerProjectSharedService(target)
        }

        val projectParameters = getServiceParameters<ProjectServiceParameters>(
            target,
            EAZY_PORTAL_PROJECT_SHARED_SERVICE_NAME
        )

        ProjectConfigurerFactory.createProjectConfigurer(projectParameters, target)
            .configure()
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        private fun <T : BuildServiceParameters> getServiceParameters(
            project: Project,
            serviceName: String,
        ): T =
            project.gradle.sharedServices
                .registrations[serviceName]
                .service
                .get()
                .parameters as T

        private fun registerProjectSharedService(project: Project) {
            val settingsServiceParameters = runCatching {
                getServiceParameters<SettingsServiceParameters>(
                    project,
                    EAZY_PORTAL_SETTINGS_SHARED_SERVICE_NAME
                )
            }.onFailure {
                project.logger.warn("Ignoring missing SharedService: $EAZY_PORTAL_SETTINGS_SHARED_SERVICE_NAME")
            }.getOrNull()

            project.gradle.sharedServices.registerIfAbsent(
                EAZY_PORTAL_PROJECT_SHARED_SERVICE_NAME,
                EazyPortalProjectSharedService::class.java
            ) {
                parameters {
                    settingsParameters.set(settingsServiceParameters)

                    projectTypeMap.set(
                        project.allprojects.associate { it.path to it.getType() }
                    )
                }
            }
        }
    }

}
