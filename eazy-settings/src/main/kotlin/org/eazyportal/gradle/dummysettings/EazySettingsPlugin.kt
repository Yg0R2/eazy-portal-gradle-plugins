package org.eazyportal.gradle.dummysettings

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.dummysettings.model.EazySettingsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/**
 * Receiver entry point (design §6.3): creates the [EazySettingsExtension] and, once the settings file has fully evaluated,
 * auto-applies [EazyProjectPlugin] to every subproject (never the root),
 * feeding the resolved `eazyPortalCoreVersion` override when the DSL sets one — else `eazy-project`'s own convention default stands.
 *
 * `beforeProject` is registered from *within* `settingsEvaluated`, not directly in [apply] (design §6.5):
 * `gradle.lifecycle.beforeProject` isolates its captured state at REGISTRATION time.
 * Registering it directly in [apply] would snapshot the DSL value before the receiver's `eazySettings { }` block (which runs later) has had a
 * chance to set it, silently losing the override. Resolving to a plain `String?` first — instead of capturing the
 * live `Property` — also keeps the captured state isolatable under the configuration cache.
 */
class EazySettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        val extension = settings.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java)

        // TODO: after lifecycle API stabilizes, validate if this is still needed
        settings.gradle.settingsEvaluated {
            settings.gradle.lifecycle.beforeProject { project ->
                project.takeIf { project != project.rootProject }
                    ?.configureEazyProject(extension)
            }
        }
    }

    private fun Project.configureEazyProject(
        eazySettingsExtension: EazySettingsExtension,
    ) {
        pluginManager.apply(EazyProjectPlugin::class.java)

        extensions.configure(EazyProjectExtension::class.java) {
            it.eazyPortalCoreVersion.set(eazySettingsExtension.eazyPortalCoreVersion.orNull)
        }
    }

    companion object {
        const val EAZY_SETTINGS_EXTENSION_NAME = "eazySettings"
    }

}
