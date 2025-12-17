package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File
import java.util.UUID
import kotlin.io.path.pathString

abstract class BaseScmProjectGiven(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    private val projectActionsMap: MutableMap<String, ProjectActions<File>>,
    private val initScmProjectBlock: () -> Unit,
) : ScmProjectGiven {

    override fun setProjectVersion(projectFile: ProjectFile<File>, version: Version) {
        projectActionsMap.computeIfAbsent(projectFile.getPath().pathString) {
            GradleProjectActions(projectFile)
        }.setVersion(version)
    }

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
