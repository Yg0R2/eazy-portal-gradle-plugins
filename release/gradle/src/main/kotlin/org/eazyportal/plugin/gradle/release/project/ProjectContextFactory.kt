package org.eazyportal.plugin.gradle.release.project

import org.eazyportal.plugin.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import java.io.File

object ProjectContextFactory {

    fun create(
        projectActionsFactory: ProjectActionsFactory<File>,
        projectFile: ProjectFile<File>,
        scmActions: Lazy<ScmActions<File>>,
    ): ProjectContext<File> =
        object : ProjectContext<File>() {

            override val root: ProjectFileToProjectActionsPair<File> by lazy {
                ProjectFileToProjectActionsPair(projectActionsFactory.create(projectFile), projectFile)
            }

            override val sub: Set<ProjectFileToProjectActionsPair<File>> by lazy {
                scmActions.value.getSubmodules(projectFile)
                    .asSequence()
                    .map(projectFile::resolve)
                    .map { ProjectFileToProjectActionsPair(projectActionsFactory.create(it), it) }
                    .toSet()
            }

        }

}
