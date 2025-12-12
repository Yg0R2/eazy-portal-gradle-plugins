package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class OldMultiModuleGitFlowScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : OldBaseMultiModuleScmProjectTestCase(
    scmActions,
    ScmConfig.GIT_FLOW,
) {

    override fun initializeProject(gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit) {
        // Initialize root project
        GradleProjectBuilder(remoteProjectFile.getFile())
            .withEazyPortalReleasePlugin()
            .withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.GIT_FLOW
                }
                """.trimIndent()
            )//.apply { gradleProjectBuilderBlock() } // TODO: this would configure the submodules
            .build()

        scmActions.initializeRepository(remoteProjectFile)

        // Initialize submodules
        remoteSubmoduleProjectFiles.forEach {
            GradleProjectBuilder(it.getFile())
                .withEazyPortalReleasePlugin()
                .withExtraProjectConfig(
                    """
                    eazyRelease {
                        scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.GIT_FLOW
                    }
                    """.trimIndent()
                )//.apply { gradleProjectBuilderBlock() } // TODO: need to configure submodules
                .build()

            scmActions.initializeRepository(it)

            scmActions.addSubmodule(remoteProjectFile, it)

            // Create remote feature branch in submodule
            scmActions.execute(it, "branch", scmConfig.featureBranch)
        }

        scmActions.commit(remoteProjectFile, CHORE_ADD_SUBMODULES_COMMIT_MESSAGE)

        // Create remote feature branch in root project
        scmActions.execute(remoteProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(remoteProjectFile, projectFile)

        // Create local feature branch
        scmActions.checkout(projectFile, scmConfig.featureBranch)
        scmActions.checkout(projectFile, scmConfig.releaseBranch)

        submoduleProjectFiles.forEach {
            scmActions.checkout(it, scmConfig.featureBranch)
            scmActions.checkout(it, scmConfig.releaseBranch)
        }
    }

}
