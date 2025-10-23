package org.eazyportal.plugin.gradle.release.core.executor.exception

class CliExecutionException(
    override val message: String,
    override val cause: Throwable? = null,
): Exception(message)
