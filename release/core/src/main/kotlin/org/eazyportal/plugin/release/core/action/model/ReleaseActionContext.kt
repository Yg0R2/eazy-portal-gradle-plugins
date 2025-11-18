package org.eazyportal.plugin.release.core.action.model

import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType

data class ReleaseActionContext(
    val conventionalCommitTypes: List<ConventionalCommitType>,
    val isForceRelease: Boolean,
)
