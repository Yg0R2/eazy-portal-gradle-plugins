package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.gradle.api.Project

/**
 * The single source of truth for what a [org.eazyportal.gradle.eazyproject.configurer.GradleProjectConfigurer] wired into a module (design §5.7):
 * the applied convention plus every sibling and eazy-portal-core dependency, each with the configuration it landed on.
 * Built by the Configurer *as it wires* (never recomputed), so the `eazyDiagnostics` task (TOOLS-67) can report the wiring without duplicating —
 *   and therefore never drifting from — the logic that produced it.
 */
internal data class WiringSummary(
    val pluginIds: List<String>,
    val siblings: List<WiredDependency>,
    val eazyPortalCore: List<WiredDependency>,
)

/** A single dependency the Configurer added, paired with the configuration it was added to. */
internal data class WiredDependency(
    val configuration: DependencyConfiguration,
    val notation: String,
) {
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
