package org.eazyportal.plugin.gradle.release.dsl.multimodule

import org.eazyportal.plugin.gradle.portal.common.dsl.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestGitActions.Companion.TEST_GIT_ACTIONS
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class MultiModuleCustomizedScmProjectTestCase : BaseMultiModuleScmProjectTestCase(
    TEST_GIT_ACTIONS,
    ScmConfig(
        featureBranch = "dummy-feature-branch",
        releaseBranch = "dummy-release-branch",
        remote = "upstream"
    ),
) {

    override fun initializeGradleRootProject(workingDir: File) {
        GradleProjectBuilder(workingDir)
            .withEazyPortalReleasePlugin()
            .withExtraProjectConfig(
                """
                eazyRelease {
                    conventionalCommitTypes = listOf(
                        org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType(
                            listOf("dummy"),
                             org.eazyportal.plugin.release.core.version.model.VersionIncrement.MAJOR,
                        ),
                        org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType(
                            listOf("fix"),
                             org.eazyportal.plugin.release.core.version.model.VersionIncrement.PATCH,
                        ),
                    )
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig(
                        featureBranch = "${scmConfig.featureBranch}",
                        releaseBranch = "${scmConfig.releaseBranch}",
                        remote = "upstream"
                    )
                }
                """.trimIndent()
            ).build()
    }

}
