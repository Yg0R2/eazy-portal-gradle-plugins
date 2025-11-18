package org.eazyportal.plugin.release.core.action.model

import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.scm.model.ScmConfig

abstract class ReleaseActionContext<T : Any> {

    abstract val conventionalCommitTypes: List<ConventionalCommitType>

    abstract val isForceRelease: Boolean

    abstract val scmActions: ScmActions<T>

    abstract val scmConfig: ScmConfig

}
