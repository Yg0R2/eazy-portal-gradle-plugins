package org.eazyportal.gradle.eazyproject.configurer

import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_PROJECT_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.gradle.api.Project

/**
 * `default` — the escape hatch for a non-standard module (§5.2). Gets `kotlin-project` + `eazyportal-core-test` (from
 * the base template) and nothing else; it opts into any other convention by bare id in its own build script.
 */
internal class DefaultProjectConfigurer(
    eazyProjectExtension: EazyProjectExtension,
    project: Project,
) : GradleProjectConfigurer(eazyProjectExtension, project) {

    override fun configurePlugins() {
        applyPlugin(KOTLIN_PROJECT_CONVENTION)
    }

    override fun configureDependencies() {
        // none — only eazyportal-core-test (added by the base template) + the BOM.
    }

}
