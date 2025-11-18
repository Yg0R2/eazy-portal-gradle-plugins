package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FinalizeSnapshotVersionAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val scmActions: ScmActions<T>,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Finalizing snapshot version...")

        val snapshotVersion = projectContext.root.projectActions.getVersion()

        projectContext.all.reversed().forEach { (projectActions, projectFile) ->
            val csmFilesToCommit = projectActions.scmFilesToCommit()

            scmActions.add(projectFile, *csmFilesToCommit)
            scmActions.commit(projectFile, "New SNAPSHOT version: $snapshotVersion")
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(FinalizeSnapshotVersionAction::class.java)
    }

}
