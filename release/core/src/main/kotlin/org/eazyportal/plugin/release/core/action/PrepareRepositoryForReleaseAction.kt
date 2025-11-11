package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PrepareRepositoryForReleaseAction<T : Any>(
    private val projectFile: ProjectFile<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
) : ReleaseAction<T>(
    projectFile,
    releaseActionContext,
) {

    override fun execute() {
        LOGGER.info("Preparing repository for release...")

        allProjectFiles.forEach {
            scmActions.clean(it)
        }

        scmActions.fetch(projectFile, scmConfig.remote)

        allProjectFiles.forEach {
            checkoutToFeatureBranch(it)
        }
    }

    private fun checkoutToFeatureBranch(projectFile: ProjectFile<T>) {
        if (scmConfig.releaseBranch != scmConfig.featureBranch) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(PrepareRepositoryForReleaseAction::class.java)
    }

}
