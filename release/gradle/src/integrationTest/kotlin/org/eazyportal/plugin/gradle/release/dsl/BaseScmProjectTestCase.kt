package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.junit.jupiter.api.DynamicTest
import java.io.File
import java.nio.file.Files

abstract class BaseScmProjectTestCase<G : ScmProjectGiven, T : ScmProjectThen> : ScmProjectTestCase<G, T> {

    final override fun runTestCase(block: TestScenario<G, ScmProjectWhen, T>.() -> Unit) {
        val workingDir = Files.createTempDirectory("ep-")
            .toFile()

        try {
            crateTestScenario(workingDir).block()
        } finally {
            workingDir.deleteRecursively()
        }
    }

    final override fun runDynamicTestCase(
        displayName: String,
        block: TestScenario<G, ScmProjectWhen, T>.() -> Unit
    ): DynamicTest =
        super.runDynamicTestCase(displayName, block)

    final override fun <A> runDynamicTestCase(
        arguments: Iterable<A>,
        displayNameFactory: (A) -> String,
        block: TestScenario<G, ScmProjectWhen, T>.(A) -> Unit
    ): List<DynamicTest> =
        super.runDynamicTestCase(arguments, displayNameFactory, block)

    protected abstract fun crateTestScenario(workingDir: File): TestScenario<G, ScmProjectWhen, T>

}
