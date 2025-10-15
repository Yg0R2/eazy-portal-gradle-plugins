package org.eazyportal.plugin.gradle.portal.common.model

import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

interface EazyPortalServiceParameters : BuildServiceParameters {

    val applicationType: Property<ApplicationTypes>

}
