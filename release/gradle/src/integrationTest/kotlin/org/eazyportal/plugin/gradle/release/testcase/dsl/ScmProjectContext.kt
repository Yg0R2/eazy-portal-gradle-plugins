package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.GivenContext
import org.eazyportal.plugin.common.integration.test.testcase.dsl.ThenContext
import org.eazyportal.plugin.common.integration.test.testcase.dsl.WhenContext
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

data class ScmProjectContext(
    val scmActions: TestScmActions<File>,
    val scmConfig: ScmConfig,
    val projectDir: ProjectDir,
    val projectActionsMap: MutableMap<String, ProjectActions<out Any>>,
) : GivenContext, WhenContext, ThenContext
