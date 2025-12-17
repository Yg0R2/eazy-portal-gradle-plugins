package org.eazyportal.plugin.common.integration.test.dsl.given

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
open class Given<out C: GivenContext>(
    private val context: C,
) {

//    fun withScenarioConfiguration(block: C.() -> Unit) {
//        block(context)
//    }

}
