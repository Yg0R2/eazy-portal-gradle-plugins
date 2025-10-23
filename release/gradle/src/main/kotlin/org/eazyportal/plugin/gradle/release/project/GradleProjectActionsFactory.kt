package org.eazyportal.plugin.gradle.release.project

import org.eazyportal.plugin.gradle.release.core.project.ProjectActions
import org.eazyportal.plugin.gradle.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile
import org.eazyportal.plugin.gradle.release.core.project.exception.InvalidProjectTypeException
import java.io.File

class GradleProjectActionsFactory : ProjectActionsFactory<File> {

    override fun create(projectFile: ProjectFile<File>): ProjectActions<File> =
        if (GradleProjectActions.isGradleProject(projectFile)) {
            GradleProjectActions(projectFile)
        } else {
            throw InvalidProjectTypeException("Unable to identify the project type in: $projectFile")
        }

}
