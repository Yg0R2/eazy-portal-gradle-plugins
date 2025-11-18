package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UpdateScmAction<T : Any>(
    projectFile: ProjectFile<T>,
    releaseActionContext: ReleaseActionContext<T>,
) : ReleaseAction<T>(
    projectFile,
    releaseActionContext,
) {

    override fun execute() {
        LOGGER.info("Update SCM...")

        allProjectFiles.forEach {
            scmActions.push(it, scmConfig.remote, scmConfig.releaseBranch, scmConfig.featureBranch)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(UpdateScmAction::class.java)
    }

}
