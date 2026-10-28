package org.eazyportal.gradle.eazyproject.configurer

import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.WiredDependency
import org.eazyportal.gradle.eazyproject.WiringSummary
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.Companion.TEST_CONFIGURATIONS
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.IMPLEMENTATION
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.eazyproject.recordWiringSummary
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Provider

/**
 * Shared template + helpers for every Gradle based archetype's [ProjectConfigurer] (design §5.4).
 *
 * [configure] is **final**: the order is fixed and identical for all archetypes —
 * 1. apply the matching convention ([configurePlugins]),
 * 2. import the `eazyportal-{framework}-core-bom` so every eazyportal-core dependency resolves to one aligned version,
 * 3. wire sibling + eazyportal-core dependencies ([configureDependencies]),
 * 4. add `eazyportal-core-test` on every test configuration — `testImplementation`, `functionalTestImplementation`, `integrationTestImplementation` and `testFixturesImplementation` (every module gets it, §5.3).
 *
 * Subclasses fill only the archetype-specific blanks and wire dependencies through [applySiblingProject] / [applyEazyPortalCore],
 * which both add the dependency **and** record it into the [org.eazyportal.gradle.eazyproject.WiringSummary] —
 * so the summary can never drift from what was actually applied.
 * All coordinates are versionless (the BOM pins them) and added lazily via [Provider]s where the version is involved,
 * keeping apply-time configuration-cache friendly (§5.6).
 * Every eazyportal-core dependency (BOM included) is additionally gated by [EazyProjectExtension.applyEazyPortalCoreDependencies];
 * the convention and the sibling wiring are not.
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
        TEST_CONFIGURATIONS.forEach {
            applyEazyPortalCore(it, project.provider { createEazyPortalCoreArtifactId("test") })
        }

        project.recordWiringSummary(
            WiringSummary(appliedPlugins.toList(), siblingProjectDependencies.toList(), eazyPortalCoreDependencies.toList()),
        )
    }

    /** Applies the archetype's convention — implementations call [applyPlugin] with the plugin id. */
    protected abstract fun configurePlugins()

    /** Wires the archetype's sibling + eazyportal-core dependencies via [applySiblingProject] / [applyEazyPortalCore]. */
    protected abstract fun configureDependencies()

    /**
     * Adds the versionless eazyportal-core dependency of [projectType] on [dependencyConfiguration] and records it:
     * `eazyportal-{framework}-core-$projectType` for a [ProjectType.isFrameworkSpecific] type, else the framework-agnostic `eazyportal-core-$projectType`.
     */
    protected fun applyEazyPortalCore(
        dependencyConfiguration: DependencyConfiguration,
        projectType: ProjectType,
    ) {
        val artifactIdProvider = if (projectType.isFrameworkSpecific) {
            // Lazy: the framework is only known once `eazy-settings` has applied its DSL, after this configurer ran (§6.3).
            eazyProjectExtension.framework.map { createEazyPortalCoreArtifactId(projectType.toString(), it) }
        } else {
            project.provider { createEazyPortalCoreArtifactId(projectType.toString()) }
        }

        applyEazyPortalCore(dependencyConfiguration, artifactIdProvider)
    }

    /**
     * Adds the eazyportal-core [artifactIdProvider] to [dependencyConfiguration] and records it — the single path every eazyportal-core dependency (BOM included) takes.
     * The dependency is added lazily, and only while [EazyProjectExtension.applyEazyPortalCoreDependencies] holds:
     * the flag is final only once `eazy-settings` fed it and the module's own build script ran — both after this configurer (§6.3) —
     * so it is never read here, only when Gradle realizes the configuration's dependencies.
     * [createDependency] turns the versionless notation into the [Dependency] (a [platform][org.gradle.api.artifacts.dsl.DependencyHandler.platform] for the BOM).
     */
    private fun applyEazyPortalCore(
        dependencyConfiguration: DependencyConfiguration,
        artifactIdProvider: Provider<String>,
        createDependency: (notation: String) -> Dependency = project.dependencies::create,
    ) {
        val coreDependency = eazyProjectExtension.applyEazyPortalCoreDependencies.map { enabled ->
            if (enabled) {
                createDependency(createEazyPortalCoreNotation(artifactIdProvider.get()))
            } else {
                null
            }
        }

        project.configurations.named(dependencyConfiguration.toString()) {
            it.dependencies.addLater(coreDependency)
        }

        eazyPortalCoreDependencies += WiredDependency(dependencyConfiguration, coreDependency.map { it.name })
    }

    /** Applies [pluginId] and records it as the module's plugin in the [WiringSummary]. */
    protected fun applyPlugin(pluginId: String) {
        project.pluginManager.apply(pluginId)

        appliedPlugins += pluginId
    }

    /** If the sibling project exists, adds it as dependency (`:$projectType`) on [dependencyConfiguration] and records it. */
    protected fun applyOptionalSiblingProject(
        dependencyConfiguration: DependencyConfiguration,
        projectType: ProjectType,
    ) {
        if (project.findProject(":$projectType") != null) {
            applySiblingProject(dependencyConfiguration, projectType)
        }
    }

    /** Adds a sibling project dependency (`:$projectType`) on [dependencyConfiguration] and records it. */
    protected fun applySiblingProject(
        dependencyConfiguration: DependencyConfiguration,
        projectType: ProjectType,
    ) {
        project.dependencies.add(dependencyConfiguration.toString(), project.project(":$projectType"))

        siblingProjectDependencies += WiredDependency(dependencyConfiguration, project.provider { ":$projectType" })
    }

    /** eazyportal-core notation WITHOUT a version — the BOM (see [importEazyPortalCoreBom]) pins it. */
    private fun createEazyPortalCoreNotation(artifactId: String): String =
        "org.eazyportal.core:$artifactId"

    /** `eazyportal-[{framework}-]core-{projectName}` — the core artifact id. */
    private fun createEazyPortalCoreArtifactId(projectName: String, framework: Framework? = null): String =
        "eazyportal-${framework?.let { "$it-" } ?: ""}core-$projectName"

    /** Imports the `eazyportal-{framework}-core-bom` platform so every eazyportal-core dependency resolves to one aligned version. */
    private fun importEazyPortalCoreBom() {
        // lazy: version and framework are set later (§6.3)
        val bomArtifactIdProvider = eazyProjectExtension.framework.map { createEazyPortalCoreArtifactId("bom", it) }
        val createBomDependency = { notation: String ->
            project.dependencies.platform("$notation:${eazyProjectExtension.eazyPortalCoreVersion.get()}")
        }

        applyEazyPortalCore(IMPLEMENTATION, bomArtifactIdProvider, createBomDependency)

        TEST_CONFIGURATIONS.forEach {
            applyEazyPortalCore(it, bomArtifactIdProvider, createBomDependency)
        }

        // TODO: validate if this is needed
//        if (exposesEazyPortalCoreViaApi) {
//            // api/client expose eazyportal-core transitively — propagate the BOM constraints to their consumers too.
//            applyEazyPortalCore(API, bomArtifactIdProvider, createBomDependency)
//        }
    }

}
