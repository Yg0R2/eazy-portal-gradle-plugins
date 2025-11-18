package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.version.SnapshotVersionProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SetSnapshotVersionAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
    private val snapshotVersionProvider: SnapshotVersionProvider,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Setting snapshot version...")

        val snapshotVersion = projectContext.root.projectActions.getVersion()
            .let(snapshotVersionProvider::provide)

        projectContext.all.asSequence().forEach { (projectActions, projectFile) ->
            checkoutToFeatureBranch(projectFile)

            projectActions.setVersion(snapshotVersion)
        }

        LOGGER.info("Snapshot version set to: $snapshotVersion")
    }

    private fun checkoutToFeatureBranch(projectFile: ProjectFile<T>) {
        if (releaseActionContext.scmConfig.releaseBranch != releaseActionContext.scmConfig.featureBranch) {
            releaseActionContext.scmActions.checkout(projectFile, releaseActionContext.scmConfig.featureBranch)
// TODO: release-branch is master, feature-branch is current branch (where build started)
            releaseActionContext.scmActions.mergeNoCommit(projectFile, releaseActionContext.scmConfig.releaseBranch)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SetSnapshotVersionAction::class.java)
    }

}
