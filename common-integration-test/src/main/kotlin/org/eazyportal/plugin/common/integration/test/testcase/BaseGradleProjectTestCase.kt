package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.gradle.dsl.GradleProjectGiven
import org.eazyportal.plugin.common.integration.test.gradle.dsl.GradleProjectThen
import org.eazyportal.plugin.common.integration.test.gradle.dsl.GradleProjectWhen
import java.io.File
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

open class BaseGradleProjectTestCase :
    TestCase<GradleProjectGiven, GradleProjectWhen, GradleProjectThen> {

    @OptIn(ExperimentalPathApi::class)
    override fun runTestCase(
        block: TestScenario<GradleProjectGiven, GradleProjectWhen, GradleProjectThen>.() -> Unit,
    ) {
        val workingDir = Files.createTempDirectory("ep-")
            .toFile()

        try {
            TestScenario(
                givenFactory = { GradleProjectGiven(workingDir, this::setUpProject) },
                whenFactory = { GradleProjectWhen(workingDir) },
                thenFactory = { GradleProjectThen(it) },
            ).block()
        } finally {
            workingDir.deleteRecursively()
        }
    }

    private fun setUpProject(
        workingDir: File,
        finalizeProjectBlock: GradleProjectBuilder.() -> Unit,
    ) {
        GradleProjectBuilder(workingDir)
            .apply(finalizeProjectBlock)
            .build()
    }

}
