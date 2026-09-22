package org.eazyportal.gradle.eazyproject.configurer

import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.WiredDependency
import org.eazyportal.gradle.eazyproject.WiringSummary
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.TEST_IMPLEMENTATION
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.IMPLEMENTATION
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.eazyproject.recordWiringSummary
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Shared template + helpers for every Gradle based archetype's [ProjectConfigurer] (design §5.4).
 *
 * [configure] is **final**: the order is fixed and identical for all archetypes —
 * 1. apply the matching convention ([configurePlugins]),
 * 2. import the eazyportal-core BOM so every eazyportal-core-* dependency resolves to one aligned version,
 * 3. wire sibling + eazyportal-core dependencies ([configureDependencies]),
 * 4. add `eazyportal-core-test` on `testImplementation` (every module gets it, §5.3).
 *
 * Subclasses fill only the archetype-specific blanks and wire dependencies through [applySiblingProject] / [applyEazyPortalCore],
 * which both add the dependency **and** record it into the [org.eazyportal.gradle.eazyproject.WiringSummary] —
 * so the summary can never drift from what was actually applied.
 * All coordinates are versionless (the BOM pins them) and added lazily via [Provider]s where the version is involved,
 * keeping apply-time configuration-cache friendly (§5.6).
 */
internal abstract class GradleProjectConfigurer(
    protected val eazyProjectExtension: EazyProjectExtension,
    protected val project: Project,
) : ProjectConfigurer {

    private val siblingProjectDependencies = mutableListOf<WiredDependency>()
    private val eazyPortalCoreDependencies = mutableListOf<WiredDependency>()
    private val appliedPlugins = mutableListOf<String>()

    final override fun configure() {
        configurePlugins()

        importEazyPortalCoreBom()
        configureDependencies()
        applyEazyPortalCore(TEST_IMPLEMENTATION, "test")

        project.recordWiringSummary(
            WiringSummary(appliedPlugins.toList(), siblingProjectDependencies.toList(), eazyPortalCoreDependencies.toList()),
        )
    }

    /** Applies the archetype's convention — implementations call [applyPlugin] with the plugin id. */
    protected abstract fun configurePlugins()

    /** Wires the archetype's sibling + eazyportal-core dependencies via [applySiblingProject] / [applyEazyPortalCore]. */
    protected abstract fun configureDependencies()

    /** Adds the versionless `eazyportal-core-$projectType` dependency on [configuration] and records it. */
    protected fun applyEazyPortalCore(
        dependencyConfiguration: DependencyConfiguration,
        projectType: ProjectType,
    ) {
        applyEazyPortalCore(dependencyConfiguration, projectType.toString())
    }

    private fun applyEazyPortalCore(
        dependencyConfiguration: DependencyConfiguration,
        projectName: String,
    ) {
        project.dependencies.add(dependencyConfiguration.toString(), createEazyPortalCoreNotation(projectName))

        eazyPortalCoreDependencies += WiredDependency(dependencyConfiguration, "eazyportal-core-$projectName")
    }

    /** Applies [pluginId] and records it as the module's plugin in the [WiringSummary]. */
    protected fun applyPlugin(pluginId: String) {
        project.pluginManager.apply(pluginId)

        appliedPlugins += pluginId
    }

    /** Adds a sibling project dependency (`:$projectType`) on [dependencyConfiguration] and records it. */
    protected fun applySiblingProject(
        dependencyConfiguration: DependencyConfiguration,
        projectType: ProjectType,
    ) {
        project.dependencies.add(dependencyConfiguration.toString(), project.project(":$projectType"))

        siblingProjectDependencies += WiredDependency(dependencyConfiguration, ":$projectType")
    }

    /** eazyportal-core notation WITHOUT a version — the BOM (see [importEazyPortalCoreBom]) pins it. */
    private fun createEazyPortalCoreNotation(projectName: String): String =
        "org.eazyportal.core:eazyportal-core-$projectName"

    /** Imports the eazyportal-core BOM/platform so every eazyportal-core-* dependency resolves to one aligned version. */
    private fun importEazyPortalCoreBom() {
        with(project.dependencies) {
            // lazy: version set later (§6.3)
            val eazyPortalCoreBomNotation = eazyProjectExtension.eazyPortalCoreVersion.map {
                platform("${createEazyPortalCoreNotation("bom")}:$it")
            }

            addProvider(IMPLEMENTATION.toString(), eazyPortalCoreBomNotation)
            addProvider(TEST_IMPLEMENTATION.toString(), eazyPortalCoreBomNotation)
            // TODO: validate if this is needed
//            if (exposesEazyPortalCoreViaApi) {
//                // api/client expose eazyportal-core transitively — propagate the BOM constraints to their consumers too.
//                addProvider(API.toString(), eazyPortalCoreBomNotation)
//            }
        }
    }

}
