package org.eazyportal.plugin.gradle.release.core.project.exception

class MultipleProjectVersionPropertyException(
    override val message: String,
) : ProjectVersionPropertyException(message)
