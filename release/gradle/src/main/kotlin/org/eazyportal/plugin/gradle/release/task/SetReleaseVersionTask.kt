package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.release.core.action.SetReleaseVersionAction
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.release.core.version.VersionIncrementProvider
import java.io.File

open class SetReleaseVersionTask : ReleaseActionTask() {

    override fun doRunTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext,
        scmActions: ScmActions<File>,
        scmConfig: ScmConfig
    ) {
        SetReleaseVersionAction(
            projectContext,
            releaseActionContext,
            ReleaseVersionProvider(),
            scmActions,
            scmConfig,
            VersionIncrementProvider(),
        ).execute()
    }

}
