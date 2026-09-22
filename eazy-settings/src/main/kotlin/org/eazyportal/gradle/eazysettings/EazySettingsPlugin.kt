package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazysettings.model.EazySettingsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode
import java.net.URI

/**
 * Receiver entry point (design §6.3): creates the [EazySettingsExtension], centralizes dependency repositories (design §6.5 — a deliberate supply-chain control, not tidiness) and,
 * once the settings file has fully evaluated, auto-applies [EazyProjectPlugin] to every subproject (never the root),
 * feeding the resolved `eazyPortalCoreVersion` override when the DSL sets one — else `eazy-project`'s own convention default stands.
 *
 * `beforeProject` is registered from *within* `settingsEvaluated`, not directly in [apply] (design §6.5):
 * `gradle.lifecycle.beforeProject` isolates its captured state at REGISTRATION time.
 * Registering it directly in [apply] would snapshot the DSL value before the receiver's `eazySettings { }` block (which runs later) has had a chance to set it, silently losing the override.
 * Resolving to a plain `String?` first — instead of capturing the live `Property` — also keeps the captured state isolatable under the configuration cache.
 */
class EazySettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        val extension = settings.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java)

        settings.configureDependencyResolutionManagement()

        // TODO: after lifecycle API stabilizes, validate if this is still needed
        settings.gradle.settingsEvaluated {
            settings.gradle.lifecycle.beforeProject { project ->
                project.takeIf { project != project.rootProject }
                    ?.configureEazyProject(extension)
            }
        }
    }

    /**
     * Dependency repositories for regular deps like `eazyportal-core-*` (design §6.3/§6.5) — NOT plugin resolution that stays the receiver's hand-written `pluginManagement` bootstrap (README Quickstart).
     * [RepositoriesMode.FAIL_ON_PROJECT_REPOS] is a deliberate supply-chain security control:
     * a module declaring its own `repositories { }` fails the build outright, so every repo an artifact can come from is centralized here and vetted, never ad-hoc per module.
     * `mavenLocal()` is intentionally omitted — add it per-repo only when testing an `eazyportal-core` SNAPSHOT, never as a standing resolution source.
     */
    private fun Settings.configureDependencyResolutionManagement() {
        dependencyResolutionManagement { drm ->
            drm.repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

            drm.repositories { repositories ->
                repositories.mavenCentral()

                repositories.maven { repo ->
                    // SAME identity ("GitHubPackages") as the receiver's pluginManagement repo → one shared credential pair.
                    repo.name = "GitHubPackages"
                    repo.url = URI("https://maven.pkg.github.com/EazyPortal")
                    repo.credentials(PasswordCredentials::class.java) // lazy: only required when eazyportal-core is actually fetched
                }
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
