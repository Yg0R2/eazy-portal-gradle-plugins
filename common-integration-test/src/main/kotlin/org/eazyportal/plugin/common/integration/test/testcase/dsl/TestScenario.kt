package org.eazyportal.plugin.common.integration.test.testcase.dsl

@Suppress("UNCHECKED_CAST")
class TestScenario<G : Given, W : When, T : Then>(
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

    fun andGivenSetUp(block: () -> Unit) {
        block()
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
