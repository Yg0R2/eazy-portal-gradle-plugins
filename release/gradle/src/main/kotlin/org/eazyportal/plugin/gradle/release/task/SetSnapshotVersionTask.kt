package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.release.core.action.SetSnapshotVersionAction
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.SnapshotVersionProvider
import java.io.File

open class SetSnapshotVersionTask : ReleaseActionTask() {

    override fun doRunTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext,
        scmActions: ScmActions<File>,
        scmConfig: ScmConfig
    ) {
        SetSnapshotVersionAction(projectContext, scmActions, scmConfig, SnapshotVersionProvider())
            .execute()
    }

}
