package org.eazyportal.plugin.gradle.portal.common

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {

    protected lateinit var workingDir: File
    protected lateinit var projectDir: File

    @BeforeAll
    fun setUpBaseIntegrationTest(@TempDir tempDir: File) {
        workingDir = tempDir

        projectDir = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }
    }

}
