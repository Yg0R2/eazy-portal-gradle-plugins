package org.eazyportal.plugin.gradle.release.core.project.exception

open class ProjectVersionPropertyException(
    override val message: String
) : ProjectException(message)
