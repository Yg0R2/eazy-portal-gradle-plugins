package org.eazyportal.plugin.gradle.portal.common.extension

import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes
import org.eazyportal.plugin.gradle.portal.common.model.ProjectTypes.Companion.isTypeOf
import org.eazyportal.plugin.gradle.portal.common.exception.MissingProjectException
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
