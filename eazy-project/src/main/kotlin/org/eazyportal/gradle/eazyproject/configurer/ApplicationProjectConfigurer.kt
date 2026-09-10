package org.eazyportal.gradle.eazyproject.configurer

import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_PROJECT_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.gradle.api.Project

/**
 * `application` — the (unpublished) top of the graph; depends on every library sibling (impl).
 * `default` is the receiver escape hatch, not part of the layered architecture, so it is intentionally excluded.
 */
internal class ApplicationProjectConfigurer(
    eazyProjectExtension: EazyProjectExtension,
    project: Project,
) : GradleProjectConfigurer(eazyProjectExtension, project) {

    override fun configurePlugins() {
        applyPlugin(KOTLIN_PROJECT_CONVENTION)
    }

    override fun configureDependencies() {
        applyEazyPortalCore(DependencyConfiguration.IMPLEMENTATION, ProjectType.APPLICATION)

        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.COMMON)
        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.API)
        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.PERSISTENCE)
        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.SERVICE)
        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.CLIENT)
        applySiblingProject(DependencyConfiguration.IMPLEMENTATION, ProjectType.WEB)
    }

}
