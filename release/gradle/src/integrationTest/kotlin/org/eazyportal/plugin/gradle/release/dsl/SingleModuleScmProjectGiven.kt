package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.version.model.Version

class SingleModuleScmProjectGiven(
    private val context: SingleModuleScmProjectTestContext,
    private val initializeProjectBlock: (SingleModuleScmProjectTestContext, SingleModuleScmProjectTestContext.() -> Unit) -> Unit,
) : ScmProjectGiven<SingleModuleScmProjectTestContext>(
    context,
    initializeProjectBlock,
) {

    val projectDir: ProjectDir = context.projectDir

    override fun setProjectVersion(version: Version) {
        context.projectActionsMap
            .computeIfAbsent(context.projectDir.localDir.absolutePath) {
                GradleProjectActions(context.projectDir.localProjectFile)
            }.setVersion(version)
    }

}
