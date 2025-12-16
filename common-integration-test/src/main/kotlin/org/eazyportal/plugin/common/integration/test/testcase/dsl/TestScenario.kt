package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
class TestScenario<G : Given<*>, W : When<*>, T : Then<*>>(
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

}
