package org.eazyportal.plugin.release.core.scm.exception

class ScmActionException(
    override val cause: Throwable?,
) : Exception()
