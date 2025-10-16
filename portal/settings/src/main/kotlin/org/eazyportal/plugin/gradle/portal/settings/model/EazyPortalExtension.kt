package org.eazyportal.plugin.gradle.portal.settings.model

import org.eazyportal.plugin.gradle.portal.common.model.ApplicationTypes
import org.gradle.api.provider.Property

interface EazyPortalExtension {

    val applicationType: Property<ApplicationTypes>

    val applyCoreDependencies: Property<Boolean>

    val coreVersion: Property<String>

}
