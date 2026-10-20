package org.eazyportal.gradle.eazyproject.model

import org.gradle.api.provider.Property

/**
 * DSL extension for `eazy-project` (design §5.1), registered as `eazyProject { }`.
 * `eazy-settings` configures (never re-creates) this extension to propagate its own `eazyPortalCoreVersion` (design §6).
 */
abstract class EazyProjectExtension {

    /**
     * eazyportal-core-* version.
     * Populated by `eazy-settings`;
     * defaulted by [org.eazyportal.gradle.eazyproject.EazyProjectPlugin.apply] (the sole owner of the default — design §5.5);
     * read lazily as a [Property].
     */
    abstract val eazyPortalCoreVersion: Property<String>

    /**
     * Framework flavour of eazyportal-core (selects `eazyportal-{framework}-core-bom` and the framework-specific modules).
     * Populated by `eazy-settings`;
     * defaulted by [org.eazyportal.gradle.eazyproject.EazyProjectPlugin.apply];
     * read lazily as a [Property].
     */
    abstract val framework: Property<Framework>

    /**
     * Escape hatch: `false` skips every eazyportal-core dependency — the `eazyportal-{framework}-core-bom` import, the archetype's `eazyportal-core-*` and `eazyportal-core-test` —
     * while the convention plugin and the sibling wiring stay in place.
     * Populated by `eazy-settings`;
     * defaulted (`true`) by [org.eazyportal.gradle.eazyproject.EazyProjectPlugin.apply];
     * read lazily as a [Property] — only final once the module's own build script ran.
     */
    abstract val applyEazyPortalCoreDependencies: Property<Boolean>

}
