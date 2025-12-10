package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.gradle.testkit.runner.BuildResult

open class GradleProjectThen(
    private val scenarioContext: ScenarioContext,
) : Then(scenarioContext) {

    fun taskOutput(block: ListAssert<String>.() -> Unit) {
        block(
            assertThat(
                scenarioContext.actual<BuildResult>()
                    .output
                    .lines()
            )
        )
    }

}
