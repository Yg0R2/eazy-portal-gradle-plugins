package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.dsl.ExecutionResult
import org.eazyportal.plugin.common.integration.test.dsl.then.ThenContext
import org.gradle.testkit.runner.BuildResult

class GradleProjectThenContext : ThenContext {

    override lateinit var executionResult: ExecutionResult

    fun taskOutput(block: ListAssert<String>.() -> Unit) {
        block(
            assertThat(
                executionResult.actual<BuildResult>()
                    .output
                    .lines()
            )
        )
    }

}
