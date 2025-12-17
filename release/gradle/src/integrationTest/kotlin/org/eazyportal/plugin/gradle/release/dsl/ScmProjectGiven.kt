package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import java.util.UUID

abstract class ScmProjectGiven<out C : ScmProjectTestContext>(
    private val context: C,
    private val initializeProjectBlock: (C, C.() -> Unit) -> Unit,
) : Given<C>(context) {

    val scmActions: TestScmActions<File> = context.scmActions
    val scmConfig: ScmConfig = context.scmConfig

    fun createDummyFile(projectFile: ProjectFile<File>) {
        projectFile.resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

    fun createAndCommitDummyFile(
        projectFile: ProjectFile<File>,
        commitMessage: String = CHORE_COMMIT_MESSAGE,
    ) {
        createDummyFile(projectFile)

        context.scmActions.add(projectFile, DUMMY_FILE_NAME)
        context.scmActions.commit(projectFile, commitMessage)
    }

    abstract fun setProjectVersion(version: Version)

    fun withScmProject(finalizeScmBlock: C.() -> Unit = {}) {
        initializeProjectBlock(context, finalizeScmBlock)
    }

}
