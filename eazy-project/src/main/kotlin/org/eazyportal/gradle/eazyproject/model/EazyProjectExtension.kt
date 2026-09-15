package org.eazyportal.gradle.eazyproject.model

import org.gradle.api.provider.Property

/**
 * DSL extension for `eazy-project` (design §5.1), registered as `eazyProject { }`.
 * `«dummy-settings»` configures (never re-creates) this extension to propagate its own `eazyPortalCoreVersion` (design §6).
 */
abstract class EazyProjectExtension {

    /**
     * eazyportal-core-* version.
     * Populated by `dummy-settings`;
     * defaulted by [org.eazyportal.gradle.eazyproject.EazyProjectPlugin.apply] (the sole owner of the default — design §5.5);
     * read lazily as a [Property].
     */
    abstract val eazyPortalCoreVersion: Property<String>

}
