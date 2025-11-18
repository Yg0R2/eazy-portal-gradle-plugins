package org.eazyportal.plugin.release.core.project.model

import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile

abstract class ProjectContext<T : Any> {

    open val all: Set<ProjectFileToProjectActionsPair<T>> by lazy {
        setOf(root) + sub
    }

    abstract val root: ProjectFileToProjectActionsPair<T>

    abstract val sub: Set<ProjectFileToProjectActionsPair<T>>

    data class ProjectFileToProjectActionsPair<T : Any>(
        val projectActions: ProjectActions<T>,
        val projectFile: ProjectFile<T>,
    )

}
