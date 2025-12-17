package org.eazyportal.plugin.common.integration.test.gradle.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.common.integration.test.dsl.then.Then
import org.gradle.testkit.runner.BuildResult

class GradleProjectThen(
    context: GradleProjectTestContext,
    private val executionResult: TestScenario.ExecutionResult,
) : Then<GradleProjectTestContext>(context) {

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
