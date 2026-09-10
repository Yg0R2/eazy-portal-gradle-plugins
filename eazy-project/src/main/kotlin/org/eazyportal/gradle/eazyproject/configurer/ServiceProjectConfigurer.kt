package org.eazyportal.gradle.eazyproject.configurer

import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_LIBRARY_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.gradle.api.Project

/** `service` — depends on `common`, `api`, `persistence` (impl). */
internal class ServiceProjectConfigurer(
    eazyProjectExtension: EazyProjectExtension,
    project: Project,
) : GradleProjectConfigurer(eazyProjectExtension, project) {

    override fun configurePlugins() {
        applyPlugin(KOTLIN_LIBRARY_CONVENTION)
    }

    override fun configureDependencies() {
        applyEazyPortalCore(DependencyConfiguration.IMPLEMENTATION, ProjectType.SERVICE)

        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.COMMON)
        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.API)
        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.PERSISTENCE)
    }

}
