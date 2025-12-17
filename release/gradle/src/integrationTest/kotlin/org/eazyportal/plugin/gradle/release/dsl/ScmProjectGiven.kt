package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File

interface ScmProjectGiven : Given {

    val scmActions: TestScmActions<File>

    val scmConfig: ScmConfig

    //------------------------------------
    // Project
    //------------------------------------

    fun setProjectVersion(
        projectFile: ProjectFile<File>,
        version: Version,
    )

    fun withScmProject()

    //------------------------------------
    // SCM
    //------------------------------------

    fun createDummyFile(projectFile: ProjectFile<File>)

    fun createAndCommitDummyFile(
        projectFile: ProjectFile<File>,
        commitMessage: String = CHORE_COMMIT_MESSAGE,
    )

}
