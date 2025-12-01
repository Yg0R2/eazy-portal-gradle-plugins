package org.eazyportal.plugin.gradle.release.asd

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import java.util.UUID
import kotlin.io.path.absolutePathString
import kotlin.reflect.KClass

abstract class BaseProjectTestCase(
    protected open val workingDir: File,
) {

    abstract fun initializeProject()

    companion object {
        @JvmStatic
        fun testCases(): List<KClass<out BaseScmProjectTestCase>> =
            listOf(
                SingleModuleGitFlowScmProjectTestCase::class,
                MultiModuleGitFlowScmProjectTestCase::class,
                SingleModuleTrunkFlowScmProjectTestCase::class,
                MultiModuleTrunkFlowScmProjectTestCase::class,
                SingleModuleCustomizedProjectTestCase::class,
                MultiModuleCustomizedProjectTestCase::class,
            )
    }

}

abstract class BaseScmProjectTestCase(
    open val scmActions: TestScmActions<File>,
    open val scmConfig: ScmConfig,
    override val workingDir: File,
) : BaseProjectTestCase(workingDir) {

    protected val projectActionsMap: MutableMap<String, ProjectActions<File>> = mutableMapOf()

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

        scmActions.add(projectFile, ".")
        scmActions.commit(projectFile, commitMessage)
    }

    fun createDummyFile(projectFile: ProjectFile<File>) {
        projectFile.resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

    fun getProjectVersion(projectFile: ProjectFile<File>): Version =
        projectActionsMap.computeIfAbsent(projectFile.getPath().absolutePathString()) {
            GradleProjectActions(projectFile)
        }.getVersion()

}

abstract class BaseSingleModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    override val workingDir: File,
) : BaseScmProjectTestCase(scmActions, scmConfig, workingDir) {

}

class SingleModuleGitFlowScmProjectTestCase(
    override val workingDir: File,
) : BaseSingleModuleScmProjectTestCase(
    TestGitActions(CommandLineExecutor()),
    ScmConfig.GIT_FLOW,
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

        // Create local feature branch
        scmActions.checkout(projectFile, scmConfig.featureBranch)
        scmActions.checkout(projectFile, scmConfig.releaseBranch)
    }

}

class SingleModuleTrunkFlowScmProjectTestCase(
    override val workingDir: File,
) : BaseSingleModuleScmProjectTestCase(
    TestGitActions(CommandLineExecutor()),
    ScmConfig.TRUNK_BASED_FLOW,
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

class SingleModuleCustomizedProjectTestCase(
    override val workingDir: File,
) : BaseSingleModuleScmProjectTestCase(
    TestGitActions(CommandLineExecutor()),
    ScmConfig(
        featureBranch = "dummy-feature-branch",
        releaseBranch = "dummy-release-branch",
        remote = "upstream"
    ),
    workingDir,
) {

    override fun initializeProject() {
        GradleProjectBuilder(
            projectDir = remoteProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).withExtraProjectConfig(
            """
                eazyRelease {
                    conventionalCommitTypes = listOf(
                        org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType(
                            listOf("dummy"),
                             org.eazyportal.plugin.release.core.version.model.VersionIncrement.MAJOR,
                         )
                    )
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig(
                        featureBranch = "${scmConfig.featureBranch}",
                        releaseBranch = "${scmConfig.releaseBranch}",
                        remote = "upstream"
                    )
                }
                """.trimIndent()
        ).build()

        scmActions.initializeRepository(remoteProjectFile, scmConfig.releaseBranch)

        // Create remote branches
        scmActions.execute(remoteProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(remoteProjectFile, projectFile)

        scmActions.execute(projectFile,"remote", "rename", "origin", scmConfig.remote)

        // Create local feature branch
        scmActions.checkout(projectFile, scmConfig.featureBranch)
        scmActions.checkout(projectFile, scmConfig.releaseBranch)
    }

}

abstract class BaseMultiModuleScmProjectTestCase(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    override val workingDir: File,
) : BaseScmProjectTestCase(scmActions, scmConfig, workingDir) {

    val submoduleProjectFile: ProjectFile<File>
        get() = projectFile.resolve(SUBMODULE_NAME)
            .also { it.getFile().mkdirs() }

    val remoteSubmoduleProjectFile: ProjectFile<File>
        get() = workingDir.resolve("${scmConfig.remote}/$SUBMODULE_NAME")
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

}

class MultiModuleGitFlowScmProjectTestCase(
    override val workingDir: File,
) : BaseMultiModuleScmProjectTestCase(
    TestGitActions(CommandLineExecutor()),
    ScmConfig.GIT_FLOW,
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

        GradleProjectBuilder(
            projectDir = remoteSubmoduleProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).withExtraProjectConfig(
            """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.GIT_FLOW
                }
                """.trimIndent()
        ).build()

        scmActions.initializeRepository(remoteSubmoduleProjectFile)

        scmActions.addSubmodule(remoteProjectFile, remoteSubmoduleProjectFile)
        scmActions.commit(remoteProjectFile, "chore: add $SUBMODULE_NAME submodule")

        // Create remote feature branch
        scmActions.execute(remoteProjectFile, "branch", scmConfig.featureBranch)
        scmActions.execute(remoteSubmoduleProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(remoteProjectFile, projectFile)

        // Create local feature branch
        scmActions.checkout(projectFile, scmConfig.featureBranch)
        scmActions.checkout(projectFile, scmConfig.releaseBranch)

        scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
        scmActions.checkout(submoduleProjectFile, scmConfig.releaseBranch)
    }

}

class MultiModuleTrunkFlowScmProjectTestCase(
    override val workingDir: File,
) : BaseMultiModuleScmProjectTestCase(
    TestGitActions(CommandLineExecutor()),
    ScmConfig.TRUNK_BASED_FLOW,
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

        GradleProjectBuilder(
            projectDir = remoteSubmoduleProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).withExtraProjectConfig(
            """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                }
                """.trimIndent()
        ).build()

        scmActions.initializeRepository(remoteSubmoduleProjectFile)

        scmActions.addSubmodule(remoteProjectFile, remoteSubmoduleProjectFile)
        scmActions.commit(remoteProjectFile, "chore: add $SUBMODULE_NAME submodule")

        scmActions.clone(remoteProjectFile, projectFile)

        // Create local branch
//        scmActions.checkout(submoduleProjectFile, scmConfig.releaseBranch)
    }

}

class MultiModuleCustomizedProjectTestCase(
    override val workingDir: File,
) : BaseMultiModuleScmProjectTestCase(
    TestGitActions(CommandLineExecutor()),
    ScmConfig(
        featureBranch = "dummy-feature-branch",
        releaseBranch = "dummy-release-branch",
        remote = "upstream"
    ),
    workingDir,
) {

    override fun initializeProject() {
        GradleProjectBuilder(
            projectDir = remoteProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).withExtraProjectConfig(
            """
                eazyRelease {
                    conventionalCommitTypes = listOf(
                        org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType(
                            listOf("dummy"),
                             org.eazyportal.plugin.release.core.version.model.VersionIncrement.MAJOR,
                         )
                    )
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig(
                        featureBranch = "${scmConfig.featureBranch}",
                        releaseBranch = "${scmConfig.releaseBranch}",
                        remote = "upstream"
                    )
                }
                """.trimIndent()
        ).build()

        scmActions.initializeRepository(remoteProjectFile, scmConfig.releaseBranch)

        GradleProjectBuilder(
            projectDir = remoteSubmoduleProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).withExtraProjectConfig(
            """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig(
                        featureBranch = "${scmConfig.featureBranch}",
                        releaseBranch = "${scmConfig.releaseBranch}",
                        remote = "upstream"
                    )
                }
                """.trimIndent()
        ).build()

        scmActions.initializeRepository(remoteSubmoduleProjectFile, scmConfig.releaseBranch)

        scmActions.addSubmodule(remoteProjectFile, remoteSubmoduleProjectFile)
        scmActions.commit(remoteProjectFile, "chore: add $SUBMODULE_NAME submodule")

        // Create remote feature branch
        scmActions.execute(remoteProjectFile, "branch", scmConfig.featureBranch)
        scmActions.execute(remoteSubmoduleProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(remoteProjectFile, projectFile)

        scmActions.execute(projectFile,"remote", "rename", "origin", scmConfig.remote)
        scmActions.execute(submoduleProjectFile,"remote", "rename", "origin", scmConfig.remote)

        // Create local feature branch
        scmActions.checkout(projectFile, scmConfig.featureBranch)
        scmActions.checkout(projectFile, scmConfig.releaseBranch)

        scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
        scmActions.checkout(submoduleProjectFile, scmConfig.releaseBranch)
    }

}
