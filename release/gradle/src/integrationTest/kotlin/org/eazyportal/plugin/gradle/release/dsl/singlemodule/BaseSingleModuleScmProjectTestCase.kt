package org.eazyportal.plugin.gradle.release.dsl.singlemodule

import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.gradle.release.dsl.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.ScmProjectWhen
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

abstract class BaseSingleModuleScmProjectTestCase(
    final override val scmActions: TestScmActions<File>,
    final override val scmConfig: ScmConfig,
) : BaseScmProjectTestCase<SingleModuleScmProjectGiven, SingleModuleScmProjectThen>() {

    final override fun crateTestScenario(workingDir: File): TestScenario<SingleModuleScmProjectGiven, ScmProjectWhen, SingleModuleScmProjectThen> {
        val projectDir = ProjectDir(
            localDir = workingDir.resolve(PROJECT_NAME),
            remoteDir = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME"),
        )

        val projectActionsMap = mutableMapOf<String, ProjectActions<File>>()

        return TestScenario(
            givenFactory = {
                SingleModuleScmProjectGiven(scmActions, scmConfig, projectDir, projectActionsMap) {
                    initScmProjectBlock(projectDir)
                }
            },
            whenFactory = { ScmProjectWhen(projectDir) },
            thenFactory = {
                SingleModuleScmProjectThen(scmActions, scmConfig, projectDir, projectActionsMap, it)
            },
        )
    }

    protected fun initScmProjectBlock(projectDir: ProjectDir) {
        projectDir.remoteDir
            .also(File::mkdirs)
            .run(::initializeGradleRootProject)

        scmActions.initializeRepository(projectDir.remoteProjectFile, scmConfig.releaseBranch)

        if (scmConfig.releaseBranch != "main") {
            // Rename release branch in submodule
            scmActions.execute(projectDir.remoteProjectFile, "branch", "-m", scmConfig.releaseBranch)
        }

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
