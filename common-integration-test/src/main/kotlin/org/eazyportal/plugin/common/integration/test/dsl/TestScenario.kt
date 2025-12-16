package org.eazyportal.plugin.common.integration.test.dsl

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl
import org.eazyportal.plugin.common.integration.test.dsl.given.GivenContext
import org.eazyportal.plugin.common.integration.test.dsl.given.GivenScope
import org.eazyportal.plugin.common.integration.test.dsl.then.ThenContext
import org.eazyportal.plugin.common.integration.test.dsl.then.ThenScope
import org.eazyportal.plugin.common.integration.test.dsl.`when`.WhenContext
import org.eazyportal.plugin.common.integration.test.dsl.`when`.WhenScope

@IntegrationTestDsl
class TestScenario<G : GivenContext, W : WhenContext, T : ThenContext>(
    private val givenContext: G,
    private val whenContext: W,
    private val thenContext: T,
) {

    fun givenTestCase(block: G.() -> Unit) =
        GivenScope(givenContext)
            .givenTestCase(block)

    fun whenExecute(block: W.() -> Unit) =
        WhenScope(whenContext)
            .whenExecute(block)
            .also(thenContext.executionResult::set)

    fun thenValidate(block: T.() -> Unit) =
        ThenScope(thenContext)
            .thenValidate(block)

}
