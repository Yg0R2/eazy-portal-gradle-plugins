package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File
import java.util.UUID

abstract class BaseScmProjectGiven(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    private val projectActionsMap: MutableMap<String, ProjectActions<File>>,
    private val initScmProjectBlock: () -> Unit,
) : ScmProjectGiven {

    override fun withScmProject() {
        initScmProjectBlock()
    }

    override fun createDummyFile(projectFile: ProjectFile<File>) {
        projectFile.resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

    override fun createAndCommitDummyFile(
        projectFile: ProjectFile<File>,
        commitMessage: String,
    ) {
        createDummyFile(projectFile)

        scmActions.add(projectFile, DUMMY_FILE_NAME)
        scmActions.commit(projectFile, commitMessage)
    }

}
