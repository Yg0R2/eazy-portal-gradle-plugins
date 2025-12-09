package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.builder.GradleProjectTestCaseBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseGradleProjectTestCase : TestCase<BaseGradleProjectTestCase> {

    protected lateinit var workingDir: File

    val projectDir: File
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

    @BeforeEach
    fun setUpWorkingDir(@TempDir tempDir: File) {
        workingDir = tempDir
    }

    final override fun initializeProject(gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit) {
        GradleProjectBuilder(projectDir)
            .apply { gradleProjectBuilderBlock() }
            .build()
    }

    override fun givenTestCase(
        initProjectBlock: GradleProjectBuilder.() -> Unit,
    ): GradleProjectTestCaseBuilder.BaseProjectGiven<BaseGradleProjectTestCase> =
        GradleProjectTestCaseBuilder.BaseProjectGiven(this, initProjectBlock)

}
