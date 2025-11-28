package org.eazyportal.plugin.gradle.portal.common.extension

import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.common.exception.MissingProjectException
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes.entries
import org.gradle.api.Project

fun Project.findSubProject(
    projectType: ProjectTypes
): Project? =
    rootProject.allprojects
        .firstOrNull { it.isTypeOf(projectType) }

fun Project.getSubProject(
    projectType: ProjectTypes,
): Project =
    rootProject.allprojects
        .firstOrNull { it.isTypeOf(projectType) }
        ?: throw MissingProjectException("Required project does not exist: ${projectType.name}")

fun Project.getType(): ProjectTypes =
    if (this == rootProject) {
        ProjectTypes.ROOT
    } else {
        entries.filter { it != ProjectTypes.ROOT }
            .first { name.endsWith(it.suffix) } // TODO: fix unknown type
    }.also { logger.info("[ProjectExtensions.getType] - project $name type is $it") }

fun Project.isTypeOf(projectType: ProjectTypes): Boolean =
    (getType() == projectType)
        .also { logger.info("[ProjectExtensions.isTypeOf] - project $name is type of $projectType: $it") }
