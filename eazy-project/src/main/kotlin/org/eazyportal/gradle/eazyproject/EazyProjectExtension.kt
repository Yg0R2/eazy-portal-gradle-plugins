package org.eazyportal.gradle.eazyproject

import org.gradle.api.provider.Property

/**
 * DSL extension for `eazy-project` (design §5.1), registered as `eazyProject { }`.
 * `«dummy-settings»` configures (never re-creates) this extension to propagate its own `eazyPortalCoreVersion` (design §6).
 */
abstract class EazyProjectExtension {

    /**
     * eazy-portal-core-* version.
     * Populated by `dummy-settings`; read lazily as a [Property].
     */
    abstract val eazyPortalCoreVersion: Property<String>

    init {
        // Set here (rather than in EazyProjectPlugin.apply(), as design §5.5 shows) so the default is
        // unit-testable without the plugin, which lands in TOOLS-67. TOOLS-67: revisit — either drop the
        // plugin's own redundant `.convention(...)` call, or remove this init block, so there's one owner.
        eazyPortalCoreVersion.convention(DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION)
    }

}
