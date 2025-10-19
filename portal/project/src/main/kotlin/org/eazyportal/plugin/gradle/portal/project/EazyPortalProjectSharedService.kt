package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.gradle.portal.project.model.ProjectServiceParameters
import org.gradle.api.services.BuildService

abstract class EazyPortalProjectSharedService : BuildService<ProjectServiceParameters> {

    companion object {
        const val EAZY_PORTAL_PROJECT_SHARED_SERVICE_NAME = "eazyPortalProjectSharedService"
    }

}
