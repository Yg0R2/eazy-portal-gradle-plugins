package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
abstract class Given<C: GivenContext>(
    private val context: C,
) {

    fun withScenarioConfiguration(block: C.() -> Unit) {
        block(context)
    }

}
