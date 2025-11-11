package org.eazyportal.plugin.release.core.project.exception

class MultipleProjectVersionPropertyException(
    override val message: String,
) : ProjectVersionPropertyException(message)
