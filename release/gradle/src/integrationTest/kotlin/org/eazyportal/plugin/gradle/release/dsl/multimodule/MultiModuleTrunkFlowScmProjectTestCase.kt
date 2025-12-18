package org.eazyportal.plugin.gradle.release.dsl.multimodule

import org.eazyportal.plugin.gradle.portal.common.dsl.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestGitActions.Companion.TEST_GIT_ACTIONS
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class MultiModuleTrunkFlowScmProjectTestCase : BaseMultiModuleScmProjectTestCase(
    TEST_GIT_ACTIONS,
    ScmConfig.TRUNK_BASED_FLOW,
) {

    final override fun initializeGradleRootProject(workingDir: File) {
        GradleProjectBuilder(workingDir)
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
