package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.gradle.portal.common.EazyPortalSharedService
import org.eazyportal.plugin.gradle.portal.common.EazyPortalSharedService.Companion.EAZY_PORTAL_SHARED_SERVICE_NAME
import org.eazyportal.plugin.gradle.portal.project.EazyPortalProjectPlugin
import org.eazyportal.plugin.gradle.portal.settings.model.EazyPortalExtension
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class EazyPortalSettingsPlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val eazyPortalExtension = target.extensions.create(
            "eazyPortal",
            EazyPortalExtension::class.java,
        )

        target.gradle.sharedServices.registerIfAbsent(
            EAZY_PORTAL_SHARED_SERVICE_NAME,
            EazyPortalSharedService::class.java
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
