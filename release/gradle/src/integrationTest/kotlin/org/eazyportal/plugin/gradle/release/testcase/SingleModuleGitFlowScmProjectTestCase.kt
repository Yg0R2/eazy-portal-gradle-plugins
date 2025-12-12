package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class SingleModuleGitFlowScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : BaseSingleModuleScmProjectTestCase(
    scmActions,
    ScmConfig.GIT_FLOW,
) {

    override fun initializeProject() {
        GradleProjectBuilder(projectDir.remoteDir)
            .withEazyPortalReleasePlugin()
            .withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.GIT_FLOW
                }
                """.trimIndent()
            )

        scmActions.initializeRepository(projectDir.remoteProjectFile)

        // Create remote feature branch
        scmActions.execute(projectDir.remoteProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(projectDir.remoteProjectFile, projectDir.localProjectFile)

        // Create local feature branch
        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
    }

}
