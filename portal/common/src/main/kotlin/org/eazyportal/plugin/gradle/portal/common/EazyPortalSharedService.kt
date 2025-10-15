package org.eazyportal.plugin.gradle.portal.common

import org.eazyportal.plugin.gradle.portal.common.model.EazyPortalServiceParameters
import org.gradle.api.services.BuildService

abstract class EazyPortalSharedService : BuildService<EazyPortalServiceParameters> {

    companion object {
        const val EAZY_PORTAL_SHARED_SERVICE_NAME = "eazyPortalSharedService"
    }

}
