package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestCase
import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectWhenContext
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

interface SingleModuleScmProjectTestCase :
    ScmProjectTestCase<SingleModuleScmProjectGivenContext> {

    val scmActions: TestScmActions<File>
    val scmConfig: ScmConfig

    override fun initGivenContext(workingDir: File): SingleModuleScmProjectGivenContext =
        SingleModuleScmProjectGivenContext(projectDir, scmActions, scmConfig)

}

interface OldSingleModuleScmProjectTestCase : OldScmProjectTestCase {

    var projectDir: ProjectDir

}
