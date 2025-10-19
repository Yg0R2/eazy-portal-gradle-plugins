package org.eazyportal.plugin.gradle.portal.project.model

import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.common.model.SettingsServiceParameters
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

interface ProjectServiceParameters : BuildServiceParameters {

    val projectTypeMap: MapProperty<String, ProjectTypes>

    val settingsParameters: Property<SettingsServiceParameters>

}
