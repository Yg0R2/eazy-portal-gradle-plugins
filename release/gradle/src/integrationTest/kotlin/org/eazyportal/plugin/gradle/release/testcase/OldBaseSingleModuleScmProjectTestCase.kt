package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.gradle.release.testcase.dsl.*
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import java.nio.file.Files

abstract class BaseSingleModuleScmProjectTestCase : SingleModuleScmProjectTestCase {

    override fun runTestCase(
        block: TestScenario<SingleModuleScmProjectGiven, ScmProjectWhen, ScmProjectThen>.() -> Unit,
    ) {
        val workingDir = Files.createTempDirectory("ep-")
            .toFile()

        try {
            val context = SingleModuleScmProjectContext(
                scmActions = scmActions,
                scmConfig = scmConfig,
                projectDir = ProjectDir(
                    localDir = workingDir.resolve(PROJECT_NAME),
                    remoteDir = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME")
                        .also(File::mkdirs),// TODO: remove this
                ),
            )

            TestScenario(
                givenFactory = { SingleModuleScmProjectGiven(context, this::setUp) },
                whenFactory = { ScmProjectWhen(context) },
                thenFactory = { ScmProjectThen(context, it) },
            ).block()
        } finally {
            workingDir.deleteRecursively()
        }
    }

    // TODO: implement this here
    protected abstract fun setUp(
        context: SingleModuleScmProjectContext,
        finalizeScmBlock: (SingleModuleScmProjectContext) -> Unit,
    )

}

abstract class OldBaseSingleModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
) : BaseScmProjectTestCase(), SingleModuleScmProjectTestCase {

    final override fun initializeScmProject(workingDir: File) {
        initializeGradleProject(projectDir.remoteDir, PROJECT_NAME)

        scmActions.initializeRepository(projectDir.remoteProjectFile, scmConfig.releaseBranch)

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

    final override fun setProjectVersion(version: Version) {
        projectActionsMap.computeIfAbsent(projectDir.localDir.absolutePath) {
            GradleProjectActions(FileSystemProjectFile(projectDir.localDir))
        }.setVersion(version)
    }

}
