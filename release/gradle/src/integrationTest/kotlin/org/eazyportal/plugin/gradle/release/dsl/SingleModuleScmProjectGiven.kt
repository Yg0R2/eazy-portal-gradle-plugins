package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File

class SingleModuleScmProjectGiven(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    val projectDir: ProjectDir,
    private val projectActionsMap: MutableMap<String, ProjectActions<File>>,
    private val initializeProjectBlock: (() -> Unit) -> Unit,
) : ScmProjectGiven(
    scmActions,
    scmConfig,
    initializeProjectBlock,
) {

    override fun setProjectVersion(version: Version) {
        projectActionsMap
            .computeIfAbsent(projectDir.localDir.absolutePath) {
                GradleProjectActions(projectDir.localProjectFile)
            }.setVersion(version)
    }

}
