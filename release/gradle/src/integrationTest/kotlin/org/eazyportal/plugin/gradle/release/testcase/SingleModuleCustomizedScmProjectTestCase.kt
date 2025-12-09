package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

class SingleModuleCustomizedScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : BaseSingleModuleScmProjectTestCase(
    scmActions,
    ScmConfig(
        featureBranch = "dummy-feature-branch",
        releaseBranch = "dummy-release-branch",
        remote = "upstream"
    ),
) {

    override fun initializeProject(gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit) {
        GradleProjectBuilder(remoteProjectFile.getFile())
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
            ).apply { gradleProjectBuilderBlock() }
            .build()

        scmActions.initializeRepository(remoteProjectFile, scmConfig.releaseBranch)

        // Create remote branches
        scmActions.execute(remoteProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(remoteProjectFile, projectFile)

        scmActions.execute(projectFile, "remote", "rename", "origin", scmConfig.remote)

        // Create local feature branch
        scmActions.checkout(projectFile, scmConfig.featureBranch)
        scmActions.checkout(projectFile, scmConfig.releaseBranch)
    }

}
