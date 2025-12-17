package org.eazyportal.plugin.common.integration.test.dsl

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl
import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import org.eazyportal.plugin.common.integration.test.dsl.given.GivenContext
import org.eazyportal.plugin.common.integration.test.dsl.then.Then
import org.eazyportal.plugin.common.integration.test.dsl.then.ThenContext
import org.eazyportal.plugin.common.integration.test.dsl.`when`.When
import org.eazyportal.plugin.common.integration.test.dsl.`when`.WhenContext

@IntegrationTestDsl
class TestScenario<G : Given<GivenContext>, W : When<WhenContext>, T : Then<ThenContext>>(
    private val givenFactory: () -> G,
    private val whenFactory: () -> W,
    private val thenFactory: (ExecutionResult) -> T,
) {

    private val executionResult = ExecutionResult()

    fun givenTestCase(block: G.() -> Unit) {
        givenFactory().block()
    }

    fun whenExecute(block: W.() -> Any) {
        whenFactory()
            .block()
            .also(executionResult::set)
    }

    fun thenVerify(block: T.() -> Unit) {
        thenFactory(executionResult).block()
    }

    class ExecutionResult {

        private var value: Any? = null

        fun <T> set(value: T) {
            this.value = value
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> actual(): T =
            value as T

    }

}
