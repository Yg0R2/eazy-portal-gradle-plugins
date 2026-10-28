package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * The single source of truth for what a [org.eazyportal.gradle.eazyproject.configurer.GradleProjectConfigurer] wired into a module (design §5.7):
 * the applied convention plus every sibling and eazyportal-core dependency, each with the configuration it landed on.
 * Built by the Configurer *as it wires* (never recomputed), so the `eazyDiagnostics` task (TOOLS-67) can report the wiring without duplicating —
 *   and therefore never drifting from — the logic that produced it.
 */
internal data class WiringSummary(
    val pluginIds: List<String>,
    val siblings: List<WiredDependency>,
    val eazyPortalCore: List<WiredDependency>,
) {

    fun siblingsString(): String =
        siblings.toGroupedString()

    fun eazyPortalCoreString(): String =
        eazyPortalCore.toGroupedString()

    /**
     * Renders [this] with each notation listed once, followed by every configuration it landed on, in wiring order —
     * e.g. `[eazyportal-spring-boot-core-bom (implementation, testImplementation), eazyportal-core-common (implementation)]`.
     */
    private fun List<WiredDependency>.toGroupedString(): String =
        groupBy(WiredDependency::notation, WiredDependency::configuration)
            .entries
            .joinToString(", ", "[", "]") { (notation, configurations) ->
                "$notation (${configurations.joinToString(", ")})"
            }

}

/**
 * A single dependency the Configurer added, paired with the configuration it was added to.
 * The notation is resolved on access, since a framework-specific one is only known once `eazy-settings` has applied its DSL.
 */
internal data class WiredDependency(
    val configuration: DependencyConfiguration,
    private val notationProvider: Provider<String>,
) {

    val notation: String
        get() = notationProvider.get()

    override fun toString(): String = "$notation ($configuration)"

}

private const val WIRING_SUMMARY_KEY = "org.eazyportal.gradle.eazyproject.wiringSummary"

/** Records the [summary] on [this] project so [wiringSummary] (read by the diagnostics task) can retrieve it. */
internal fun Project.recordWiringSummary(summary: WiringSummary) {
    extensions.extraProperties.set(WIRING_SUMMARY_KEY, summary)
}

/**
 * The [WiringSummary] recorded by the Configurer that ran on [this] project.
 *
 * @throws IllegalStateException if no Configurer has run yet (the summary is recorded at the end of
 *   [org.eazyportal.gradle.eazyproject.configurer.GradleProjectConfigurer.configure]).
 */
internal fun Project.wiringSummary(): WiringSummary {
    val recorded = extensions.extraProperties.takeIf { it.has(WIRING_SUMMARY_KEY) }?.get(WIRING_SUMMARY_KEY)

    return recorded as? WiringSummary
        ?: error("No WiringSummary recorded for $path — was a ProjectConfigurer applied to this module?")
}
