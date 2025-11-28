package org.eazyportal.plugin.gradle.release.asd

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File
import java.util.UUID

abstract class BaseProjectTestCase(
    protected open val workingDir: File,
) {

    abstract fun initializeProject()

}

abstract class BaseScmProjectTestCase(
    open val scmConfig: ScmConfig,
    open val scmActions: TestScmActions<File>,
    override val workingDir: File,
) : BaseProjectTestCase(workingDir) {

    val projectFile: ProjectFile<File>
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

    val remoteProjectFile: ProjectFile<File>
        get() = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME")
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

    fun createAndCommitDummyFile(
        projectFile: ProjectFile<File>,
        commitMessage: String = CHORE_COMMIT_MESSAGE,
    ) {
        createDummyFile(projectFile)

        scmActions.commit(projectFile, commitMessage)
    }

    fun createDummyFile(projectFile: ProjectFile<File>) {
        projectFile.resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

}

abstract class BaseSingleModuleScmProjectTestCase(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
    override val workingDir: File,
) : BaseScmProjectTestCase(scmConfig, scmActions, workingDir) {

}

class SingleModuleGitFlowScmProjectTestCase(
    override val workingDir: File,
) : BaseSingleModuleScmProjectTestCase(
    ScmConfig.GIT_FLOW,
    TestGitActions(CommandLineExecutor()),
    workingDir,
) {

    override fun initializeProject() {
        GradleProjectBuilder(
            projectDir = remoteProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).withExtraProjectConfig(
            """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.GIT_FLOW
                }
                """.trimIndent()
        ).build()

        scmActions.initializeRepository(remoteProjectFile)

        // Create remote feature branch
        scmActions.execute(remoteProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(remoteProjectFile, projectFile)
    }

}

class SingleModuleTrunkFlowScmProjectTestCase(
    override val workingDir: File,
) : BaseSingleModuleScmProjectTestCase(
    ScmConfig.TRUNK_BASED_FLOW,
    TestGitActions(CommandLineExecutor()),
    workingDir,
) {

    override fun initializeProject() {
        GradleProjectBuilder(
            projectDir = remoteProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).withExtraProjectConfig(
            """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                }
                """.trimIndent()
        ).build()

        scmActions.initializeRepository(remoteProjectFile)

        scmActions.clone(remoteProjectFile, projectFile)
    }


}

class SingleModuleCustomFlowScmProjectTestCase(
    override val workingDir: File,
) : BaseSingleModuleScmProjectTestCase(
    ScmConfig(
        featureBranch = "dummy-feature-branch",
        releaseBranch = "dummy-release-branch",
        remote = "upstream"
    ),
    TestGitActions(CommandLineExecutor()),
    workingDir,
) {

    override fun initializeProject() {
        GradleProjectBuilder(
            projectDir = remoteProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).withExtraProjectConfig(
            """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig(
                        featureBranch = "dummy-feature-branch",
                        releaseBranch = "dummy-release-branch",
                        remote = "upstream"
                    )
                }
                """.trimIndent()
        ).build()

        scmActions.initializeRepository(remoteProjectFile)

        // Create remote branches
        scmActions.execute(remoteProjectFile, "branch", scmConfig.featureBranch)
        scmActions.execute(remoteProjectFile, "branch", scmConfig.releaseBranch)

        scmActions.clone(remoteProjectFile, projectFile)

        scmActions.execute(projectFile,"remote", "rename", "origin", scmConfig.remote)
    }

}

abstract class BaseMultiModuleScmProjectTestCase(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
    override val workingDir: File,
) : BaseScmProjectTestCase(scmConfig, scmActions, workingDir) {

    protected val submoduleProjectFile: ProjectFile<File>
        get() = projectFile.resolve(SUBMODULE_NAME)
            .also { it.getFile().mkdirs() }

    protected val remoteSubmoduleProjectFile: ProjectFile<File>
        get() = workingDir.resolve("${scmConfig.remote}/$SUBMODULE_NAME")
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

}

abstract class BaseMultiModuleGitFlowScmProjectTestCase(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
    override val workingDir: File,
) : BaseMultiModuleScmProjectTestCase(scmConfig, scmActions, workingDir) {

}

abstract class BaseMultiModuleTrunkFlowScmProjectTestCase(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
    override val workingDir: File,
) : BaseMultiModuleScmProjectTestCase(scmConfig, scmActions, workingDir) {

}

abstract class BaseMultiModuleCustomFlowScmProjectTestCase(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
    override val workingDir: File,
) : BaseMultiModuleScmProjectTestCase(scmConfig, scmActions, workingDir) {

}
