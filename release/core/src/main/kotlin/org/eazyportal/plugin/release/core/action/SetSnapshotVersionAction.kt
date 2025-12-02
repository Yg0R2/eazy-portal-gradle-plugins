package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.SnapshotVersionProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SetSnapshotVersionAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val scmActions: ScmActions<T>,
    private val scmConfig: ScmConfig,
    private val snapshotVersionProvider: SnapshotVersionProvider,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Setting snapshot version...")

        val snapshotVersion = projectContext.root.projectActions.getVersion()
            .let(snapshotVersionProvider::provide)

        projectContext.all.asSequence().forEach { (projectActions, projectFile) ->
            if (scmActions.getCurrentBranch(projectFile) != scmConfig.featureBranch) {
                scmActions.checkout(projectFile, scmConfig.featureBranch)
            }

            if (scmConfig.releaseBranch != scmConfig.featureBranch) {
                scmActions.mergeNoCommit(projectFile, scmConfig.releaseBranch)
            }

            projectActions.setVersion(snapshotVersion)
        }

        LOGGER.info("Snapshot version set to: $snapshotVersion")
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SetSnapshotVersionAction::class.java)
    }

}
