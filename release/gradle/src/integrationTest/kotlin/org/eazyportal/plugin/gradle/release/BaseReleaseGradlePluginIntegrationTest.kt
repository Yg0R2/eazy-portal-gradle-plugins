package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

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
