package org.eazyportal.plugin.gradle.release.dsl.multimodule

import org.eazyportal.plugin.gradle.release.dsl.BaseScmProjectGiven
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

class MultiModuleScmProjectGiven(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    val projectDir: ProjectDir,
    val submoduleProjectDirs: List<ProjectDir>,
    projectActionsMap: MutableMap<String, ProjectActions<File>>,
    initScmProjectBlock: () -> Unit,
) : BaseScmProjectGiven(
    scmActions,
    scmConfig,
    projectActionsMap,
    initScmProjectBlock,
)
