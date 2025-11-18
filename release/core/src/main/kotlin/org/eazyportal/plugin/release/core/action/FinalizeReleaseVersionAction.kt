package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FinalizeReleaseVersionAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Finalize release version...")

        val releaseVersion = projectContext.root.projectActions.getVersion()

        projectContext.all.reversed().forEach { (projectActions, projectFile) ->
            val csmFilesToCommit = projectActions.scmFilesToCommit()

            releaseActionContext.scmActions.add(projectFile, *csmFilesToCommit)
            releaseActionContext.scmActions.commit(projectFile, "Release version: $releaseVersion")

            releaseActionContext.scmActions.tag(projectFile, releaseVersion)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(FinalizeReleaseVersionAction::class.java)
    }

}
