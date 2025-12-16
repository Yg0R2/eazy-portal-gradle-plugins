package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.builder.TestCaseBuilder
import org.eazyportal.plugin.common.integration.test.testcase.dsl.Given
import org.eazyportal.plugin.common.integration.test.testcase.dsl.TestScenario
import org.eazyportal.plugin.common.integration.test.testcase.dsl.Then
import org.eazyportal.plugin.common.integration.test.testcase.dsl.When
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest

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

interface OldTestCase<P : OldTestCase<P>> {

    fun givenTestCase(
        initProjectBlock: GradleProjectBuilder.() -> Unit = {},
    ): TestCaseBuilder.Given<P, *, *>

    fun initializeProject(
        gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit,
    )

}
