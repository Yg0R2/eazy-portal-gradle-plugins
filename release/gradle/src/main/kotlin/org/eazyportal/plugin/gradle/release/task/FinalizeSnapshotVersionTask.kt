package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.release.core.action.FinalizeSnapshotVersionAction
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class FinalizeSnapshotVersionTask : ReleaseActionTask() {

    override fun doRunTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext,
        scmActions: ScmActions<File>,
        scmConfig: ScmConfig
    ) {
        FinalizeSnapshotVersionAction(projectContext, scmActions)
            .execute()
    }

}
