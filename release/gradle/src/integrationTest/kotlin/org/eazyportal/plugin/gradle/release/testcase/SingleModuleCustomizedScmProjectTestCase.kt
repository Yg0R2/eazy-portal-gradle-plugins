package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

open class SingleModuleCustomizedScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : BaseSingleModuleScmProjectTestCase(
    scmActions,
    ScmConfig(
        featureBranch = "dummy-feature-branch",
        releaseBranch = "dummy-release-branch",
        remote = "upstream"
    ),
) {

    override fun initializeProject() {
        GradleProjectBuilder(projectDir.remoteDir)
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

        scmActions.initializeRepository(projectDir.remoteProjectFile, scmConfig.releaseBranch)

        // Create remote branches
        scmActions.execute(projectDir.remoteProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(projectDir.remoteProjectFile, projectDir.localProjectFile)

        // Rename remote
        scmActions.execute(projectDir.localProjectFile, "remote", "rename", "origin", scmConfig.remote)

        // Create local feature branch
        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
    }

}
