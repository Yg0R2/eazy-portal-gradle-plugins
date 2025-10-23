package org.eazyportal.plugin.gradle.release.core.project.exception

class InvalidProjectTypeException(
    override val message: String,
): ProjectException(message)
