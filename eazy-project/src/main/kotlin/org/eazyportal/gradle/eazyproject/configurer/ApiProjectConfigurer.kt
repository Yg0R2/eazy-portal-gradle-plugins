package org.eazyportal.gradle.eazyproject.configurer

import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_LIBRARY_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.gradle.api.Project

/** `api` — exposes `common` + eazy-portal-core-api to consumers via `api`. */
internal class ApiProjectConfigurer(
    eazyProjectExtension: EazyProjectExtension,
    project: Project,
) : GradleProjectConfigurer(eazyProjectExtension, project) {

    override fun configurePlugins() {
        applyPlugin(KOTLIN_LIBRARY_CONVENTION)
    }

    override fun configureDependencies() {
        applyEazyPortalCore(DependencyConfiguration.API, ProjectType.API)

        applySiblingProject(DependencyConfiguration.API, ProjectType.COMMON)
    }

}
