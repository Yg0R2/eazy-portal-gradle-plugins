package org.eazyportal.plugin.release.core.project.model

import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile

data class ProjectContext<T : Any>(
    val root: Pair<T>,
    val sub: Set<Pair<T>>,
) {

    val all: Set<Pair<T>> by lazy {
        setOf(root) + sub
    }

    data class Pair<T : Any>(
        val projectActions: ProjectActions<T>,
        val projectFile: ProjectFile<T>,
    )

}
