package org.eazyportal.plugin.gradle.portal.common

import org.eazyportal.plugin.gradle.portal.common.model.SettingsServiceParameters
import org.gradle.api.services.BuildService

abstract class EazyPortalSettingsSharedService : BuildService<SettingsServiceParameters> {

    companion object {
        const val EAZY_PORTAL_SETTINGS_SHARED_SERVICE_NAME = "eazyPortalSettingsSharedService"
    }

}
