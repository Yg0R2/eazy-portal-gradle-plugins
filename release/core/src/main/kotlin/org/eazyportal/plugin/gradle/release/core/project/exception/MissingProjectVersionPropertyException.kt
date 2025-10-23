package org.eazyportal.plugin.gradle.release.core.project.exception

class MissingProjectVersionPropertyException(
    override val message: String,
): ProjectVersionPropertyException(message)