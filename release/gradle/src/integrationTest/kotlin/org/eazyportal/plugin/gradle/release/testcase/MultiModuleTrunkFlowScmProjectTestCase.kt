package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class MultiModuleTrunkFlowScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : OldBaseMultiModuleScmProjectTestCase(
    scmActions,
    ScmConfig.TRUNK_BASED_FLOW,
) {

    override fun initializeGradleProject(projectDir: File, projectName: String) {
        GradleProjectBuilder(projectDir)
            .withProjectName(projectName)
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
