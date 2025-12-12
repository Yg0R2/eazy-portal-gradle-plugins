package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class MultiModuleCustomizedScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : BaseMultiModuleScmProjectTestCase(
    scmActions,
    ScmConfig(
        featureBranch = "dummy-feature-branch",
        releaseBranch = "dummy-release-branch",
        remote = "upstream"
    ),
) {

    override fun initializeGradleProject(projectDir: File, projectName: String) {
        GradleProjectBuilder(projectDir)
            .withProjectName(projectName)
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
