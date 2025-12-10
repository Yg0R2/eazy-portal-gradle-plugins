package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.TestCase

@Suppress("UNCHECKED_CAST")
class TestScenario<G : Given, W : When, T : Then, C: TestCase<G, W, T, *>>(
    private val testCase: C,
    private val givenFactory: () -> G,
    private val whenFactory: () -> W,
    private val thenFactory: (ScenarioContext) -> T,
) {

    private val scenarioContext = ScenarioContext()

    fun givenTestCase(block: G.() -> Unit) {
        givenFactory()
            .apply { block() }
            .build()
    }

    fun andGivenSetUp(block: C.() -> Unit) {
        block(testCase)
    }

    fun <T> whenExecute(block: W.(ScenarioContext) -> T) {
        whenFactory()
            .block(scenarioContext)
            .also(scenarioContext::set)
    }

    fun thenValidate(block: T.() -> Unit) {
        thenFactory(scenarioContext).block()
    }

}
