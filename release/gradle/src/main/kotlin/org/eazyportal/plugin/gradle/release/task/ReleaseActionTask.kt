package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.gradle.release.ProjectContextFactory
import org.eazyportal.plugin.gradle.release.ReleaseActionContextFactory
import org.eazyportal.plugin.gradle.release.ScmConfigFactory
import org.eazyportal.plugin.gradle.release.extension.getOrElse
import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.gradle.release.project.GradleProjectActionsFactory
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.GitActions
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ReleaseActionTask : DefaultTask() {

    @get:Input
    protected val extension: EazyReleasePluginExtension =
        project.extensions.getByType(EazyReleasePluginExtension::class.java)

    @get:Internal
    protected val projectFile: ProjectFile<File> =
        FileSystemProjectFile(project.projectDir)

    @get:Internal
    protected val providerFactory: ProviderFactory =
        project.providers

    @Internal
    final override fun getGroup(): String =
        RELEASE_TASKS_GROUP

    final override fun setGroup(group: String?) {
        throw UnsupportedOperationException("Not allowed to set the group of an $RELEASE_TASKS_GROUP task.")
    }

    @TaskAction
    open fun runTask() {
        logger.info("Running '$name' task...")

        runCatching {
            val releaseActionContext = ReleaseActionContextFactory.create(extension, providerFactory)

            val projectActionsFactory = extension.projectActionsFactory
                .getOrElse { GradleProjectActionsFactory() }

            val scmActions = extension.scmActions
                .getOrElse { GitActions(CommandLineExecutor()) }

            val scmConfig = ScmConfigFactory.create(extension, providerFactory)

            val projectContext = ProjectContextFactory.create(projectActionsFactory, projectFile, scmActions)

            doRunTask(projectContext, releaseActionContext, scmActions, scmConfig)
        }.onFailure {
            logger.error("Failed to run '$name' task", it)
        }.onSuccess {
            logger.info("Finished '$name' task.")
        }.getOrThrow()
    }

    protected abstract fun doRunTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext,
        scmActions: ScmActions<File>,
        scmConfig: ScmConfig,
    )

    companion object {
        const val RELEASE_TASKS_GROUP = "eazy-release"
    }

}
