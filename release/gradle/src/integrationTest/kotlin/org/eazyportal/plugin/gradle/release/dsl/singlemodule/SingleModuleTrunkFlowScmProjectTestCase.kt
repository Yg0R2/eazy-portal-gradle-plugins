package org.eazyportal.plugin.gradle.release.dsl.singlemodule

import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.gradle.portal.common.dsl.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestGitActions.Companion.TEST_GIT_ACTIONS
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class SingleModuleTrunkFlowScmProjectTestCase : BaseSingleModuleScmProjectTestCase(
    TEST_GIT_ACTIONS,
    ScmConfig.TRUNK_BASED_FLOW,
) {

    override fun initializeGradleRootProject(workingDir: File) {
        GradleProjectBuilder(workingDir)
            .withProjectName(PROJECT_NAME)
            .withEazyPortalReleasePlugin()
            .withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                }
                """.trimIndent()
            ).build()
    }

}
