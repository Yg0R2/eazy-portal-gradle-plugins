package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FinalizeReleaseVersionAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val scmActions: ScmActions<T>,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Finalize release version...")

        val releaseVersion = projectContext.root.projectActions.getVersion()

        projectContext.all.reversed().forEach { (projectActions, projectFile) ->
            val csmFilesToCommit = projectActions.scmFilesToCommit()

            scmActions.add(projectFile, *csmFilesToCommit)
            scmActions.commit(projectFile, "Release version: $releaseVersion")

            scmActions.tag(projectFile, releaseVersion)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(FinalizeReleaseVersionAction::class.java)
    }

}
