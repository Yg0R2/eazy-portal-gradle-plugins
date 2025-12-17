package org.eazyportal.plugin.gradle.release.dsl.multimodule

import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.gradle.release.dsl.BaseScmProjectThen
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

class MultiModuleScmProjectThen(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    val projectDir: ProjectDir,
    val submoduleProjectDirs: List<ProjectDir>,
    projectActionsMap: MutableMap<String, ProjectActions<File>>,
    executionResult: TestScenario.ExecutionResult,
) : BaseScmProjectThen(
    scmActions,
    scmConfig,
    projectActionsMap,
    executionResult,
)
