package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.util.UUID

abstract class ScmProjectBaseIntegrationTest1(
    protected open val scmActions: TestScmActions<File>,
    protected open val scmConfig: ScmConfig,
) : BaseReleaseGradlePluginIntegrationTest1() {

    protected val remoteProjectFile: ProjectFile<File>
        get() = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME")
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

    protected lateinit var projectActionsMap: MutableMap<String, ProjectActions<File>>

    @BeforeEach
    fun setUpScmProjectBaseIntegrationTest() {
        projectActionsMap = mutableMapOf()
    }

    protected fun createAndCommitDummyFile(
        projectFile: ProjectFile<File>,
        commitMessage: String = CHORE_COMMIT_MESSAGE,
    ) {
        createDummyFile(projectFile)

        scmActions.commit(projectFile, commitMessage)
    }

    protected fun createDummyFile(projectFile: ProjectFile<File>) {
        projectFile.resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

    protected open fun initializeRepository(initProjectFile: ProjectFile<File>) {
        GradleProjectBuilder(
            projectDir = initProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()

        scmActions.initializeRepository(initProjectFile)
    }

    protected open fun setUpBeforeClone() {
        // Nothing to set up
    }

}

abstract class ScmProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : BaseReleaseGradlePluginIntegrationTest(), ScmProjectIntegrationTest {

    override val remoteProjectDir: File
        get() = workingDir.resolve("${ScmConstants.REMOTE}/$PROJECT_NAME")
            .also { it.mkdirs() }

    protected val projectActionsMap = mutableMapOf<String, ProjectActions<File>>()

    final override fun getProjectVersion(projectDir: File, branch: String?): Version {
        if (branch != null) {
            scmUtils.checkout(projectDir, branch)
        }

        return projectActionsMap.computeIfAbsent(projectDir.path) {
            GradleProjectActions(FileSystemProjectFile(projectDir))
        }.getVersion()
    }

    protected abstract fun setupRemoteBeforeClone()

}
