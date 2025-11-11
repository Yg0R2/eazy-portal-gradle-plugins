package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.version.SnapshotVersionProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SetSnapshotVersionAction<T : Any>(
    private val projectActionsFactory: ProjectActionsFactory<T>,
    private val projectFile: ProjectFile<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
    private val snapshotVersionProvider: SnapshotVersionProvider,
) : ReleaseAction<T>(
    projectFile,
    releaseActionContext,
) {

    override fun execute() {
        LOGGER.info("Setting snapshot version...")

        val snapshotVersion = projectActionsFactory.create(projectFile)
            .getVersion()
            .let(snapshotVersionProvider::provide)

        allProjectFiles.asSequence().forEach {
            checkoutToFeatureBranch(it)

            projectActionsFactory.create(it)
                .setVersion(snapshotVersion)
        }

        LOGGER.info("Snapshot version set to: $snapshotVersion")
    }

    private fun checkoutToFeatureBranch(projectFile: ProjectFile<T>) {
        if (scmConfig.releaseBranch != scmConfig.featureBranch) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            scmActions.mergeNoCommit(projectFile, scmConfig.releaseBranch)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SetSnapshotVersionAction::class.java)
    }

}
