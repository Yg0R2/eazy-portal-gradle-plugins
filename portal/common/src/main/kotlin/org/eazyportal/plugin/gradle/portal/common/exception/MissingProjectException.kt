package org.eazyportal.plugin.gradle.portal.common.exception

class MissingProjectException(
    override val message: String,
) : RuntimeException(message)
