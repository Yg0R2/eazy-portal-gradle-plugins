package org.eazyportal.plugin.common.integration.test.dsl

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl
import org.eazyportal.plugin.common.integration.test.dsl.given.GivenContext
import org.eazyportal.plugin.common.integration.test.dsl.then.ThenContext
import org.eazyportal.plugin.common.integration.test.dsl.`when`.WhenContext
import java.io.File
import java.nio.file.Files

interface TestCase<G : GivenContext, W : WhenContext, T : ThenContext> {

    @IntegrationTestDsl
    fun runTest(block: TestScenario<G, W, T>.() -> Unit) {
        val workingDir = Files.createTempDirectory("ep-").toFile()

        try {
            TestScenario(
                initGivenContext(workingDir),
                initWhenContext(workingDir),
                initThenContext(),
            ).block()
        } finally {
            workingDir.deleteRecursively()
        }
    }

    fun initGivenContext(workingDir: File): G

    fun initWhenContext(workingDir: File): W

    fun initThenContext(): T

}
