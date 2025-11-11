package org.eazyportal.plugin.release.core.action.model

import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.scm.model.ScmConfig

data class ReleaseActionContext<T : Any>(
    val conventionalCommitTypesProvider: () -> List<ConventionalCommitType>,
    val isForceReleaseProvider: () -> Boolean,
    val scmActionsProvider: () -> ScmActions<T>,
    val scmConfigProvider: () -> ScmConfig,
)
