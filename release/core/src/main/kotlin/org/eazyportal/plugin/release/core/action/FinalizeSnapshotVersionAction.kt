package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FinalizeSnapshotVersionAction<T : Any>(
    private val projectActionsFactory: ProjectActionsFactory<T>,
    private val projectFile: ProjectFile<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
) : ReleaseAction<T>(
    projectFile,
    releaseActionContext,
) {

    override fun execute() {
        LOGGER.info("Finalizing snapshot version...")

        val snapshotVersion = projectActionsFactory.create(projectFile)
            .getVersion()

        allProjectFiles.reversed().forEach {
            val csmFilesToCommit = projectActionsFactory.create(it)
                .scmFilesToCommit()

            scmActions.add(it, *csmFilesToCommit)
            scmActions.commit(it, "New SNAPSHOT version: $snapshotVersion")
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(FinalizeSnapshotVersionAction::class.java)
    }

}
