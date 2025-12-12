package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import kotlin.io.path.absolutePathString

open class SingleModuleGitFlowScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
) : BaseSingleModuleScmProjectTestCase(
    scmActions,
    ScmConfig.GIT_FLOW,
) {

    override fun initializeGradleProjectBuilder(): GradleProjectBuilder =
        GradleProjectBuilder(projectDir.remoteDir)
            .withEazyPortalReleasePlugin()
            .withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.GIT_FLOW
                }
                """.trimIndent()
            )

    override fun initializeScm() {
        scmActions.initializeRepository(projectDir.remoteProjectFile)

        // Create remote feature branch
        scmActions.execute(projectDir.remoteProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(projectDir.remoteProjectFile, projectDir.localProjectFile)

        // Create local feature branch
        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
    }

    override fun getProjectVersion(projectFile: ProjectFile<File>): Version =
        projectActionsMap.computeIfAbsent(projectFile.getPath().absolutePathString()) {
            GradleProjectActions(projectFile)
        }.getVersion()

}
