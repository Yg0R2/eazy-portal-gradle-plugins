package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
abstract class When<C : WhenContext>(
    private val context: C,
)
