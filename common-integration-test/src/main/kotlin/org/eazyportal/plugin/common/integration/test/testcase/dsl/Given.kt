package org.eazyportal.plugin.common.integration.test.testcase.dsl

abstract class Given {

    fun withScenarioConfiguration(block: () -> Unit) {
        block()
    }

}
