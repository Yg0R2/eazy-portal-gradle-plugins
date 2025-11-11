package org.eazyportal.plugin.release.core.project.exception

class MissingProjectVersionPropertyException(
    override val message: String,
): ProjectVersionPropertyException(message)