package org.eazyportal.plugin.gradle.release.olddsl

import org.eazyportal.plugin.common.ScmTestFixtures
import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import java.util.UUID

abstract class ScmProjectGiven(
    open val scmActions: TestScmActions<File>,
    open val scmConfig: ScmConfig,
    private val initializeProjectBlock: (() -> Unit) -> Unit,
) : Given {

    protected val projectActionsMap = mutableMapOf<String, ProjectActions<File>>()

    fun createDummyFile(projectFile: ProjectFile<File>) {
        projectFile.resolve(ScmTestFixtures.DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

    fun createAndCommitDummyFile(
        projectFile: ProjectFile<File>,
        commitMessage: String = ScmTestFixtures.CHORE_COMMIT_MESSAGE,
    ) {
        createDummyFile(projectFile)

        scmActions.add(projectFile, ScmTestFixtures.DUMMY_FILE_NAME)
        scmActions.commit(projectFile, commitMessage)
    }

    abstract fun setProjectVersion(
        projectFile: ProjectFile<File>,
        version: Version,
    )

    fun withScmProject(finalizeScmBlock: () -> Unit = {}) {
        initializeProjectBlock(finalizeScmBlock)
    }

}
