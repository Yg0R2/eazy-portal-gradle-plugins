package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.gradle.release.core.action.ReleaseAction
import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class ReleaseActionTask @Inject constructor(
    private val releaseAction: ReleaseAction<File>,
    private val taskName: String,
) : DefaultTask() {

    protected val extension: EazyReleasePluginExtension
        @Input get() = project.extensions.getByType(EazyReleasePluginExtension::class.java)

    @Internal
    final override fun getGroup(): String {
        return GROUP
    }

    final override fun setGroup(group: String?) {
        throw UnsupportedOperationException("Not allowed to set the group of an $GROUP task.")
    }

    @TaskAction
    open fun runTask() {
        logger.info("Running '$taskName' task...")

        runCatching {
            releaseAction.execute()
        }.onFailure {
            logger.error("Failed to run '$taskName' task", it)
        }.onSuccess {
            logger.info("Finished '$taskName' task.")
        }.getOrThrow()
    }

    companion object {
        const val GROUP = "eazy-release"
    }

}
