package org.eazyportal.plugin.release.core.executor.exception

class CliExecutionException(
    override val message: String,
    override val cause: Throwable? = null,
): Exception(message)
