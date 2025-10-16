package org.eazyportal.plugin.gradle.portal.project

import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.gradle.api.provider.Property

object CoreDependencyNotationFactory {

    fun createCoreDependencyNotation(projectType: ProjectTypes, coreVersion: Property<String>): String =
        "org.eazyportal.portal:core${projectType.suffix}:${coreVersion.getOrElse("+")}"

}
