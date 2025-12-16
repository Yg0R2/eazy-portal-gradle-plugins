package org.eazyportal.plugin.common.integration.test.dsl.`when`

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
class WhenScope<C : WhenContext>(
    private val context: C,
) {

    fun <R> whenExecute(block: C.() -> R): R =
        context.block()

}
