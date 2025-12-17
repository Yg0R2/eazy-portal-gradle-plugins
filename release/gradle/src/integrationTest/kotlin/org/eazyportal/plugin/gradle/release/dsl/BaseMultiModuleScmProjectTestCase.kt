package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.SUBMODULE_NAMES
import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.gradle.portal.common.dsl.GradleProjectBuilder
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

abstract class BaseMultiModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : BaseScmProjectTestCase() {

    override fun crateTestScenario(workingDir: File): TestScenario<ScmProjectGiven, ScmProjectWhen, ScmProjectThen> {
        val projectDir = ProjectDir(
            localDir = workingDir.resolve(PROJECT_NAME),
            remoteDir = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME"),
        )
        val submoduleProjectDirs = SUBMODULE_NAMES.map {
            ProjectDir(
                localDir = projectDir.localDir.resolve(it),
                remoteDir = projectDir.remoteDir.resolve(it),
            )
        }

        return TestScenario(
            givenFactory = {
                ScmProjectGiven(scmActions, scmConfig, projectDir) {
                    initScmProjectBlock(projectDir, submoduleProjectDirs)
                }
            },
            whenFactory = { ScmProjectWhen(projectDir) },
            thenFactory = { ScmProjectThen(it) },
        )
    }

    protected fun initScmProjectBlock(
        projectDir: ProjectDir,
        submoduleProjectDirs: List<ProjectDir>,
    ) {
        projectDir.remoteDir
            .also(File::mkdirs)
            .run(::initializeGradleRootProject)

        scmActions.initializeRepository(projectDir.remoteProjectFile)

        submoduleProjectDirs.forEach { submoduleProjectDir ->
            submoduleProjectDir.remoteDir
                .also(File::mkdirs)
                .run {
                    GradleProjectBuilder(this)
                        .withProjectName(name)
                        .build()
                }

            scmActions.initializeRepository(submoduleProjectDir.remoteProjectFile, scmConfig.releaseBranch)

            scmActions.addSubmodule(projectDir.remoteProjectFile, submoduleProjectDir.remoteProjectFile)

            if (scmConfig.featureBranch != scmConfig.releaseBranch) {
                // Create remote feature branch in submodule
                scmActions.execute(submoduleProjectDir.remoteProjectFile, "branch", scmConfig.featureBranch)
            }
        }

        scmActions.commit(projectDir.remoteProjectFile, CHORE_ADD_SUBMODULES_COMMIT_MESSAGE)

        if (scmConfig.featureBranch != scmConfig.releaseBranch) {
            // Create remote feature branch in root project
            scmActions.execute(projectDir.remoteProjectFile, "branch", scmConfig.featureBranch)
        }

        scmActions.clone(projectDir.remoteProjectFile, projectDir.localProjectFile)

        if (scmConfig.remote != "origin") {
            // Rename remote
            scmActions.execute(projectDir.localProjectFile, "remote", "rename", "origin", scmConfig.remote)

            submoduleProjectDirs.forEach {
                scmActions.execute(it.localProjectFile, "remote", "rename", "origin", scmConfig.remote)
            }
        }

        if (scmConfig.featureBranch != scmConfig.releaseBranch) {
            // Create local feature branch
            scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
            scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

            submoduleProjectDirs.forEach {
                scmActions.checkout(it.localProjectFile, scmConfig.featureBranch)
                scmActions.checkout(it.localProjectFile, scmConfig.releaseBranch)
            }
        }
    }

    protected abstract fun initializeGradleRootProject(workingDir: File)

}
