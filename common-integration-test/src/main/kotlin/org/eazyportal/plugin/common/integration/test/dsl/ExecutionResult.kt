package org.eazyportal.plugin.common.integration.test.dsl

class ExecutionResult {

    private var value: Any? = null

    fun <T> set(value: T) {
        this.value = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> actual(): T =
        value as T

}
