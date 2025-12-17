package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import java.io.File
import java.nio.file.Files

abstract class BaseScmProjectTestCase : ScmProjectTestCase {

    final override fun runTestCase(block: TestScenario<ScmProjectGiven, ScmProjectWhen, ScmProjectThen>.() -> Unit) {
        val workingDir = Files.createTempDirectory("ep-")
            .toFile()

        try {
            crateTestScenario(workingDir).block()
        } finally {
            workingDir.deleteRecursively()
        }
    }

    protected abstract fun crateTestScenario(workingDir: File): TestScenario<ScmProjectGiven, ScmProjectWhen, ScmProjectThen>

}
