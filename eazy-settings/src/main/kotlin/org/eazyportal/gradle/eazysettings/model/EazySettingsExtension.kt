package org.eazyportal.gradle.eazysettings.model

import org.eazyportal.gradle.eazyproject.model.Framework
import org.gradle.api.provider.Property

/**
 * Settings DSL extension for `eazy-settings` (design §6.1), registered as `eazySettings { }`.
 * Left unset, `eazy-project`'s own [org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION]
 * convention applies (design §6.3/§6.4) — this class owns no default itself.
 */
abstract class EazySettingsExtension {

    /** Override for the `eazyportal-core-*` version fed to every subproject's `eazyProject { eazyPortalCoreVersion }`. */
    abstract val eazyPortalCoreVersion: Property<String>

    /** Override for the [Framework] fed to every subproject's `eazyProject { framework }`; selects the `eazyportal-{framework}-core-*` artifacts. */
    abstract val framework: Property<Framework>

}
