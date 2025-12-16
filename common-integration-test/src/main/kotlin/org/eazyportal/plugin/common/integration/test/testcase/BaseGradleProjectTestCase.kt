package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.dsl.*
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

abstract class BaseGradleProjectTestCase :
    TestCase<GradleProjectGiven, GradleProjectWhen, GradleProjectThen> {

    @OptIn(ExperimentalPathApi::class)
    override fun runTestCase(
        block: TestScenario<GradleProjectGiven, GradleProjectWhen, GradleProjectThen>.() -> Unit,
    ) {
        val workingDir = Files.createTempDirectory("ep-")

        try {
            val context = GradleProjectContext(workingDir.toFile())

            TestScenario(
                givenFactory = { GradleProjectGiven(context, this::setUpProject) },
                whenFactory = { GradleProjectWhen(context) },
                thenFactory = { GradleProjectThen(context, it) },
            ).block()
        } finally {
            workingDir.deleteRecursively()
        }
    }

    private fun setUpProject(
        context: GradleProjectContext,
        finalizeProjectBlock: GradleProjectBuilder.() -> Unit,
    ) {
        GradleProjectBuilder(context.workingDir)
            .apply(finalizeProjectBlock)
            .build()
    }

}
