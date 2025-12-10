package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.OldTestCase
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.gradle.release.testcase.builder.ScmProjectTestCaseBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*
import kotlin.io.path.absolutePathString

abstract class BaseScmProjectTestCase(
    open val scmActions: TestScmActions<File>,
    open val scmConfig: ScmConfig,
) : OldTestCase<BaseScmProjectTestCase> {

    protected val projectActionsMap: MutableMap<String, ProjectActions<File>> = mutableMapOf()

    protected lateinit var workingDir: File

    val projectFile: ProjectFile<File>
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

    val remoteProjectFile: ProjectFile<File>
        get() = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME")
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

    @BeforeEach
    fun setUpWorkingDir(@TempDir tempDir: File) {
        workingDir = tempDir
    }

    // TODO: move this to each impl
    override fun givenTestCase(
        initProjectBlock: GradleProjectBuilder.() -> Unit,
    ): ScmProjectTestCaseBuilder.ScmProjectGiven<BaseScmProjectTestCase, *> =
        ScmProjectTestCaseBuilder.ScmProjectGiven(this, initProjectBlock)

    fun createAndCommitDummyFile(
        projectFile: ProjectFile<File>,
        commitMessage: String = CHORE_COMMIT_MESSAGE,
    ) {
        createDummyFile(projectFile)

        scmActions.add(projectFile, DUMMY_FILE_NAME)
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

    abstract fun setProjectVersion(version: Version)

}
