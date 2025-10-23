package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.gradle.portal.common.EazyPortalSettingsSharedService
import org.eazyportal.plugin.gradle.portal.common.EazyPortalSettingsSharedService.Companion.EAZY_PORTAL_SETTINGS_SHARED_SERVICE_NAME
import org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectPlugin
import org.eazyportal.plugin.gradle.portal.settings.model.EazyPortalSettingsPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class EazyPortalSettingsPlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val eazyPortalExtension = target.extensions.create("eazyPortal", EazyPortalSettingsPluginExtension::class.java)

        target.gradle.sharedServices.registerIfAbsent(
            EAZY_PORTAL_SETTINGS_SHARED_SERVICE_NAME,
            EazyPortalSettingsSharedService::class.java
        ) {
            parameters {
                applicationType.set(eazyPortalExtension.applicationType)
                applyCoreDependencies.set(eazyPortalExtension.applyCoreDependencies)
                coreVersion.set(eazyPortalExtension.coreVersion)
            }
        }

        target.gradle.allprojects {
            plugins.apply(EazyPortalProjectPlugin::class.java)
        }
    }

}
