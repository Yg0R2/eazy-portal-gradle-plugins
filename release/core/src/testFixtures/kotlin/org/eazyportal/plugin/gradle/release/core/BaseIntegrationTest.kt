package org.eazyportal.plugin.gradle.release.core

import org.eazyportal.plugin.gradle.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseIntegrationTest {

    protected lateinit var projectFile: ProjectFile<File>
    protected lateinit var workingDir: File

    @BeforeEach
    fun setUpProjectFile(@TempDir tempDir: File) {
        projectFile = tempDir
            .resolve(PROJECT_NAME)
            .also { it.mkdir() }
            .let(::FileSystemProjectFile)

        workingDir = tempDir
    }

    companion object {
        const val PROJECT_NAME = "dummy-project"
    }

}
