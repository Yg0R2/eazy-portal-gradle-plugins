package org.eazyportal.plugin.gradle.release.dsl.singlemodule

import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.gradle.portal.common.dsl.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class SingleModuleGitFlowScmProjectTestCase : BaseSingleModuleScmProjectTestCase(
    TestGitActions(CommandLineExecutor()),
    ScmConfig.GIT_FLOW,
) {

    override fun initializeGradleRootProject(workingDir: File) {
        GradleProjectBuilder(workingDir)
            .withProjectName(PROJECT_NAME)
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
