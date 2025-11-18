package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UpdateScmAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val scmActions: ScmActions<T>,
    private val scmConfig: ScmConfig,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Update SCM...")

        projectContext.all.forEach { (_, projectFile) ->
            scmActions.push(projectFile, scmConfig.remote, scmConfig.releaseBranch, scmConfig.featureBranch)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(UpdateScmAction::class.java)
    }

}
