package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseReleaseGradlePluginIntegrationTest1 {

    protected lateinit var projectFile: ProjectFile<File>
    protected lateinit var workingDir: File

    @BeforeEach
    fun setUpBaseIntegrationTest(@TempDir tempDir: File) {
        workingDir = tempDir

        projectFile = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }
    }

}

abstract class BaseReleaseGradlePluginIntegrationTest {

    val projectDir: File
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

    protected lateinit var workingDir: File

    @BeforeEach
    fun setUpBaseIntegrationTest(@TempDir tempDir: File) {
        workingDir = tempDir
    }

}
