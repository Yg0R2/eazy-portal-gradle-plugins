package org.eazyportal.plugin.common.integration.test.dsl

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl
import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import org.eazyportal.plugin.common.integration.test.dsl.then.Then
import org.eazyportal.plugin.common.integration.test.dsl.`when`.When
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest

@IntegrationTestDsl
interface TestCase<G : Given, W : When, T : Then> {

    fun runTestCase(block: TestScenario<G, W, T>.() -> Unit)

    fun runDynamicTestCase(
        displayName: String,
        block: TestScenario<G, W, T>.() -> Unit,
    ): DynamicTest =
        dynamicTest(displayName) {
            runTestCase(block)
        }

    fun <A> runDynamicTestCase(
        arguments: Iterable<A>,
        displayNameFactory: (A) -> String,
        block: TestScenario<G, W, T>.(A) -> Unit,
    ): List<DynamicTest> =
        arguments.map { argument ->
            dynamicTest(displayNameFactory(argument)) {
                runTestCase {
                    block(argument)
                }
            }
        }

}
