package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectGiven
import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectThen
import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectWhen
import org.eazyportal.plugin.common.integration.test.testcase.dsl.TestScenario
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseGradleProjectTestCase :
    TestCase<GradleProjectGiven, GradleProjectWhen, GradleProjectThen, BaseGradleProjectTestCase> {

    protected lateinit var projectDir: File

    private lateinit var workingDir: File

    @BeforeEach
    fun setUpWorkingDir(@TempDir tempDir: File) {
        workingDir = tempDir

        projectDir = workingDir.resolve(PROJECT_NAME)
    }

    override fun runTestCase(
        block: TestScenario<GradleProjectGiven, GradleProjectWhen, GradleProjectThen, BaseGradleProjectTestCase>.() -> Unit,
    ) {
        TestScenario(
            this,
            { GradleProjectGiven(projectDir) },
            { GradleProjectWhen(projectDir) },
            { GradleProjectThen(it) },
        ).block()
    }

}
