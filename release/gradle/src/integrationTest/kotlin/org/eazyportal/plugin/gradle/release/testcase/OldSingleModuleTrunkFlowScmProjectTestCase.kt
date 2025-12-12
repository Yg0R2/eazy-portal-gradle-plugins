package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class OldSingleModuleTrunkFlowScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : OldBaseSingleModuleScmProjectTestCase(
    scmActions,
    ScmConfig.TRUNK_BASED_FLOW,
) {

    override fun initializeProject(gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit) {
        GradleProjectBuilder(remoteProjectFile.getFile())
            .withEazyPortalReleasePlugin()
            .withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                }
                """.trimIndent()
            ).apply { gradleProjectBuilderBlock() }
            .build()

        scmActions.initializeRepository(remoteProjectFile)

        scmActions.clone(remoteProjectFile, projectFile)
    }

}
