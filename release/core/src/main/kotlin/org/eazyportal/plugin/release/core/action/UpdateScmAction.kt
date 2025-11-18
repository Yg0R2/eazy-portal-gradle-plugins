package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UpdateScmAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Update SCM...")

        projectContext.all.forEach { (_, projectFile) ->
            releaseActionContext.scmActions.push(
                projectFile,
                releaseActionContext.scmConfig.remote,
                releaseActionContext.scmConfig.releaseBranch,
                releaseActionContext.scmConfig.featureBranch,
            )
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(UpdateScmAction::class.java)
    }

}
