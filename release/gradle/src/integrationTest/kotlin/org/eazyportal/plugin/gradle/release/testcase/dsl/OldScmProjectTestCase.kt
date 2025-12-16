package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.integration.test.dsl.TestCase
import org.eazyportal.plugin.common.integration.test.testcase.OldTestCase
import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectWhenContext
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import java.util.UUID

interface ScmProjectTestCase<G : ScmProjectGivenContext> :
    TestCase<G, GradleProjectWhenContext, ScmProjectThenContext> {

    override fun initWhenContext(workingDir: File): GradleProjectWhenContext =
        GradleProjectWhenContext()


    override fun initThenContext(): ScmProjectThenContext {
        TODO("Not yet implemented")
    }

}

interface OldScmProjectTestCase : OldTestCase<ScmProjectGiven, ScmProjectWhen, ScmProjectThen> {

    val scmActions: TestScmActions<File>
    val scmConfig: ScmConfig

    fun createDummyFile(projectFile: ProjectFile<File>) {
        projectFile.resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

    fun createAndCommitDummyFile(
        projectFile: ProjectFile<File>,
        commitMessage: String = CHORE_COMMIT_MESSAGE,
    ) {
        createDummyFile(projectFile)

        scmActions.add(projectFile, DUMMY_FILE_NAME)
        scmActions.commit(projectFile, commitMessage)
    }

    fun setProjectVersion(version: Version)

}
