package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

data class MultiModuleScmProjectContext(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    val projectDir: ProjectDir,
    val submoduleProjectDirs: List<ProjectDir>,
) : ScmProjectContext {

    override val projectActionsMap: MutableMap<String, ProjectActions<out Any>> = mutableMapOf()

}
