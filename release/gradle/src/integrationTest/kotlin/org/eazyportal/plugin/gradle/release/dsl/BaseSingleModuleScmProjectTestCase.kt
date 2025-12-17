package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

abstract class BaseSingleModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : BaseScmProjectTestCase() {

    override fun crateTestScenario(workingDir: File): TestScenario<ScmProjectGiven, ScmProjectWhen, ScmProjectThen> {
        val projectDir = ProjectDir(
            localDir = workingDir.resolve(PROJECT_NAME),
            remoteDir = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME"),
        )

        return TestScenario(
            givenFactory = {
                ScmProjectGiven(scmActions, scmConfig, projectDir) {
                    initScmProjectBlock(projectDir)
                }
            },
            whenFactory = { ScmProjectWhen(projectDir) },
            thenFactory = { ScmProjectThen(it) },
        )
    }

    protected fun initScmProjectBlock(projectDir: ProjectDir) {
        initializeGradleRootProject(projectDir.remoteDir)

        if (scmConfig.featureBranch != scmConfig.releaseBranch) {
            // Create remote branches
            scmActions.execute(projectDir.remoteProjectFile, "branch", scmConfig.featureBranch)
        }

        scmActions.clone(projectDir.remoteProjectFile, projectDir.localProjectFile)

        if (scmConfig.remote != "origin") {
            // Rename remote
            scmActions.execute(projectDir.localProjectFile, "remote", "rename", "origin", scmConfig.remote)
        }

        if (scmConfig.featureBranch != scmConfig.releaseBranch) {
            // Create local feature branch
            scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
            scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
        }
    }

    protected abstract fun initializeGradleRootProject(workingDir: File)

}
