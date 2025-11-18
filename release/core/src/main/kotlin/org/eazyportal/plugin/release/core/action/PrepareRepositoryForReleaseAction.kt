package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PrepareRepositoryForReleaseAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Preparing repository for release...")

        projectContext.all.forEach { (_, projectFile) ->
            releaseActionContext.scmActions.clean(projectFile)
        }

        releaseActionContext.scmActions.fetch(projectContext.root.projectFile, releaseActionContext.scmConfig.remote)

        projectContext.all.forEach { (_, projectFile) ->
            checkoutToFeatureBranch(projectFile)
        }
    }

    private fun checkoutToFeatureBranch(projectFile: ProjectFile<T>) {
        if (releaseActionContext.scmConfig.releaseBranch != releaseActionContext.scmConfig.featureBranch) {
            releaseActionContext.scmActions.checkout(projectFile, releaseActionContext.scmConfig.featureBranch)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(PrepareRepositoryForReleaseAction::class.java)
    }

}
