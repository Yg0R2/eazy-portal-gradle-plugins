package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import java.io.File

object ProjectContextFactory {

    fun create(
        projectActionsFactory: ProjectActionsFactory<File>,
        projectFile: ProjectFile<File>,
        scmActions: ScmActions<File>,
    ): ProjectContext<File> =
        ProjectContext(
            root = ProjectContext.Pair(projectActionsFactory.create(projectFile), projectFile),
            sub = scmActions.getSubmodules(projectFile)
                .asSequence()
                .map(projectFile::resolve)
                .map { ProjectContext.Pair(projectActionsFactory.create(it), it) }
                .toSet()
        )

}