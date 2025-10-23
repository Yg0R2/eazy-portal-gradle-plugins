package org.eazyportal.plugin.gradle.portal.settings.model

import org.eazyportal.plugin.gradle.portal.common.model.ApplicationTypes
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface EazyPortalSettingsPluginExtension {

    @get:Input
    val applicationType: Property<ApplicationTypes>

    @get:Input
    val applyCoreDependencies: Property<Boolean>

    @get:Input
    val coreVersion: Property<String>

}
