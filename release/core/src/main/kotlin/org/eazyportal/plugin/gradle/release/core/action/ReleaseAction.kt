package org.eazyportal.plugin.gradle.release.core.action

import org.eazyportal.plugin.gradle.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile
import org.eazyportal.plugin.gradle.release.core.scm.ScmActions
import org.eazyportal.plugin.gradle.release.core.scm.model.ScmConfig

abstract class ReleaseAction<T : Any>(
    private val projectFile: ProjectFile<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
) {

    protected val allProjectFiles: Set<ProjectFile<T>> by lazy {
        setOf(projectFile) + subModuleProjectFiles
    }

    protected val subModuleProjectFiles: Set<ProjectFile<T>> by lazy {
        scmActions.getSubmodules(projectFile)
            .map(projectFile::resolve)
            .toSet()
    }

    protected val scmActions: ScmActions<T> by lazy {
        releaseActionContext.scmActionsProvider()
    }
    protected val scmConfig: ScmConfig by lazy {
        releaseActionContext.scmConfigProvider()
    }

    abstract fun execute()

}
