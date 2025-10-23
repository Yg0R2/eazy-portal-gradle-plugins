package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.gradle.release.core.action.SetReleaseVersionAction
import org.eazyportal.plugin.gradle.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.gradle.release.core.project.FileSystemProjectFile
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class SetReleaseVersionTask @Inject constructor(
    private val setReleaseVersionAction: SetReleaseVersionAction<File>,
) : EazyReleaseBaseTask() {

    @get:Input
    abstract val releaseActionContext: Property<ReleaseActionContext<File>>

    @TaskAction
    fun run() {
        logger.quiet("Setting release version...")

        setReleaseVersionAction.execute(
            FileSystemProjectFile(project.projectDir),
            releaseActionContext.get()
        )
    }

}
