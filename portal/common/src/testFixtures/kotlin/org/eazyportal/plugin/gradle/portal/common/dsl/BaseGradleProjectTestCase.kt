package org.eazyportal.plugin.gradle.portal.common.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestCase
import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import java.nio.file.Files

open class BaseGradleProjectTestCase :
    TestCase<GradleProjectGiven, GradleProjectWhen, GradleProjectThen> {

    override fun runTestCase(
        block: TestScenario<GradleProjectGiven, GradleProjectWhen, GradleProjectThen>.() -> Unit,
    ) {
        val workingDir = Files.createTempDirectory("ep-")
            .toFile()

        try {
            TestScenario(
                givenFactory = { GradleProjectGiven(workingDir) },
                whenFactory = { GradleProjectWhen(workingDir) },
                thenFactory = { GradleProjectThen(it) },
            ).block()
        } finally {
            workingDir.deleteRecursively()
        }
    }

}
