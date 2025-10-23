package org.eazyportal.plugin.gradle.release.core.project

import org.eazyportal.plugin.gradle.release.core.project.exception.ProjectException

interface ProjectActionsFactory<T : Any> {

    @Throws(ProjectException::class)
    fun create(projectFile: ProjectFile<T>): ProjectActions<T>

}
