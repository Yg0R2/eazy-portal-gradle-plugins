package org.eazyportal.plugin.gradle.release.core.action

import org.eazyportal.plugin.gradle.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile

interface ReleaseAction<T: Any> {

    fun execute(projectFile: ProjectFile<T>, releaseActionContext: ReleaseActionContext<T>)

}
