package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
abstract class Then<C : ThenContext>(
    private val context: C,
)
