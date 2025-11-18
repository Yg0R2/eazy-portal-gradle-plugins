package org.eazyportal.plugin.gradle.release.model

import org.eazyportal.plugin.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.File

interface EazyReleasePluginExtension {

    @get:Input
    val conventionalCommitTypes: ListProperty<ConventionalCommitType>

//    @get:Input
//    val releaseBuildTasks: List<String> = listOf("build", "publish")

    @get:Input
    val projectActionsFactory: Property<ProjectActionsFactory<File>>

    @get:Input
    val scmActions: Property<ScmActions<File>>

    @get:Input
    val scmConfig: Property<ScmConfig>

}
