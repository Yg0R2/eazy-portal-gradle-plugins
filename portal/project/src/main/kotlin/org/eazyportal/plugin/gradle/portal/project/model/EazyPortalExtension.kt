package org.eazyportal.plugin.gradle.portal.project.model

import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty

interface EazyPortalExtension {

    val projectTypeMap: MapProperty<Project, ProjectTypes>

}
