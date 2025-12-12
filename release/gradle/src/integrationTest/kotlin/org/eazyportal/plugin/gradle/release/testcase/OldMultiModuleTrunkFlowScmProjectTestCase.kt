package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class OldMultiModuleTrunkFlowScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : OldBaseMultiModuleScmProjectTestCase(
    scmActions,
    ScmConfig.TRUNK_BASED_FLOW,
) {

    override fun initializeProject(gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit) {
        // Initialize root project
        GradleProjectBuilder(remoteProjectFile.getFile())
            .withEazyPortalReleasePlugin()
            .withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
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
                        scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                    }
                    """.trimIndent()
                )//.apply { gradleProjectBuilderBlock() } // TODO: need to configure submodules
                .build()

            scmActions.initializeRepository(it)

            scmActions.addSubmodule(remoteProjectFile, it)
        }

        scmActions.commit(remoteProjectFile, CHORE_ADD_SUBMODULES_COMMIT_MESSAGE)

        scmActions.clone(remoteProjectFile, projectFile)

        // After clone the HEAD is detached
        remoteSubmoduleProjectFiles.forEach {
            scmActions.checkout(it, scmConfig.releaseBranch)
        }
    }

}
