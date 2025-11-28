package org.eazyportal.plugin.release.core

import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmActions

interface TestScmActions<T : Any> : ScmActions<T> {

    fun addSubmodule(
        projectFile: ProjectFile<T>,
        submoduleProjectFile: ProjectFile<T>,
    )

    fun clone(
        from: ProjectFile<T>,
        to: ProjectFile<T>,
    )

    fun execute(
        projectFile: ProjectFile<T>,
        vararg gitCommands: String,
    ): List<String>

    fun initializeRepository(
        projectFile: ProjectFile<T>,
        branchName: String = "main",
    )

    fun status(projectFile: ProjectFile<T>): List<String>

}
