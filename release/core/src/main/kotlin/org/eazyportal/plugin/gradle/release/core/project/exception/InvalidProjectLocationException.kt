package org.eazyportal.plugin.gradle.release.core.project.exception

class InvalidProjectLocationException(
    override val message: String,
) : ProjectException(message)
