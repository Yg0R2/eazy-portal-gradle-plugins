package org.eazyportal.plugin.common.integration.test.testcase.dsl

@Suppress("UNCHECKED_CAST")
class TestScenario<G : Given, W : When, T : Then>(
    private val givenFactory: () -> G,
    private val whenFactory: () -> W,
    private val thenFactory: () -> T,
) {

    fun givenTestCase(block: G.() -> Unit) {
        givenFactory()
            .apply { block() }
            .build()
    }

    fun whenExecute(block: W.() -> Unit) {
        whenFactory().block()
    }

    fun thenValidate(block: T.() -> Unit) {
        thenFactory().block()
    }

}
