package org.eazyportal.plugin.release.core

import org.eazyportal.plugin.common.GradleTestFixtures
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseReleaseCoreIntegrationTest {

    protected lateinit var projectFile: ProjectFile<File>
    protected lateinit var workingDir: File

    @BeforeEach
    fun setUpProjectFile(@TempDir tempDir: File) {
        projectFile = tempDir
            .resolve(GradleTestFixtures.PROJECT_NAME)
            .also { it.mkdir() }
            .let(::FileSystemProjectFile)

        workingDir = tempDir
    }

}