package org.eazyportal.plugin.gradle.portal.common

import org.eazyportal.plugin.common.CommonTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {

    protected lateinit var workingDir: File
    protected lateinit var projectDir: File

    @BeforeAll
    fun setUpBaseIntegrationTest(
        @TempDir tempDir: File
    ) {
        workingDir = tempDir

        projectDir = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }
    }

    protected fun File.initializeGradleProject(
        vararg subProjectNames: String,
    ) {
        createGradleRunner(this, "init", "--dsl", "kotlin")
            .build()

        copyIntoFromResources(this@BaseIntegrationTest::class.java.simpleName, "build.gradle.kts")
        copyIntoFromResources(this@BaseIntegrationTest::class.java.simpleName, "settings.gradle.kts")

        subProjectNames.forEach {
            Files.createDirectories(resolve(it).toPath())
        }
    }

}
