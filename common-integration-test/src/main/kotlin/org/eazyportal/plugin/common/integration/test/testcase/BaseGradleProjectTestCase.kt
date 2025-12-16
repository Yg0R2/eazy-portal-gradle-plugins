package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectGiven
import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectThen
import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectWhen
import org.eazyportal.plugin.common.integration.test.testcase.dsl.TestScenario
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseGradleProjectTestCase :
    TestCase<GradleProjectGiven, GradleProjectWhen, GradleProjectThen> {

    lateinit var workingDir: File

    @BeforeEach
    fun setUpWorkingDir(@TempDir tempDir: File) {
        workingDir = tempDir
    }

    fun initializeGradleProjectBuilder(): GradleProjectBuilder =
        GradleProjectBuilder(workingDir)

    override fun runTestCase(
        block: TestScenario<GradleProjectGiven, GradleProjectWhen, GradleProjectThen>.() -> Unit,
    ) {
        TestScenario(
            { GradleProjectGiven(this) },
            { GradleProjectWhen(workingDir) },
            { GradleProjectThen(it) },
        ).block()
    }

}
