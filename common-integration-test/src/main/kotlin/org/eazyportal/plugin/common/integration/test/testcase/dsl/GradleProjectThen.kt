package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.gradle.testkit.runner.BuildResult

open class GradleProjectThen(
    private val executionResult: ExecutionResult,
) : Then(executionResult) {

    fun gradleTaskOutput(block: ListAssert<String>.() -> Unit) {
        block(
            assertThat(
                executionResult.actual<BuildResult>()
                    .output
                    .lines()
            )
        )
    }

}
