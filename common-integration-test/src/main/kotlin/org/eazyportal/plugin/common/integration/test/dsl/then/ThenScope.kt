package org.eazyportal.plugin.common.integration.test.dsl.then

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
class ThenScope<C : ThenContext>(
    private val context: C,
) {

    fun thenValidate(block: C.() -> Unit) {
        context.block()
    }

}
