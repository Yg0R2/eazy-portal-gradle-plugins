package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.gradle.release.testcase.dsl.SingleModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class SingleModuleGitFlowScmProjectTestCase : SingleModuleScmProjectTestCase {
    override val scmActions: TestScmActions<File> = TestGitActions(CommandLineExecutor())
    override val scmConfig: ScmConfig = ScmConfig.GIT_FLOW
}


open class OldSingleModuleGitFlowScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : BaseSingleModuleScmProjectTestCase(
    scmActions,
    ScmConfig.GIT_FLOW,
) {

    override fun initializeGradleProject(projectDir: File, projectName: String) {
        GradleProjectBuilder(projectDir)
            .withProjectName(projectName)
            .withEazyPortalReleasePlugin()
            .withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.GIT_FLOW
                }
                """.trimIndent()
            ).build()
    }

}
