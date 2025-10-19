package org.eazyportal.plugin.gradle.portal.common.model

import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

interface SettingsServiceParameters : BuildServiceParameters {

    val applicationType: Property<ApplicationTypes>

    val applyCoreDependencies: Property<Boolean>

    val coreVersion: Property<String>

}
