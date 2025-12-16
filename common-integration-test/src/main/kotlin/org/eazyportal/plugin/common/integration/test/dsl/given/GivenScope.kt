package org.eazyportal.plugin.common.integration.test.dsl.given

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
class GivenScope<C : GivenContext>(
    private val context: C,
) {

    fun givenTestCase(block: C.() -> Unit) {
        context.block()
    }

}
