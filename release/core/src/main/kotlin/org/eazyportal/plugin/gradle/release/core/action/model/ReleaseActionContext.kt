package org.eazyportal.plugin.gradle.release.core.action.model

import org.eazyportal.plugin.gradle.release.core.scm.ScmActions
import org.eazyportal.plugin.gradle.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.gradle.release.core.scm.model.ScmConfig

data class ReleaseActionContext<T: Any>(
    val conventionalCommitTypes: () -> List<ConventionalCommitType>,
    val isForceRelease: () -> Boolean,
    val scmActions: () -> ScmActions<T>,
    val scmConfig: () -> ScmConfig,
)
