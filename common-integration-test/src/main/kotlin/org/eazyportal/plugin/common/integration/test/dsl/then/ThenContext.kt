package org.eazyportal.plugin.common.integration.test.dsl.then

import org.eazyportal.plugin.common.integration.test.dsl.ExecutionResult
import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl

@IntegrationTestDsl
interface ThenContext {

    val executionResult: ExecutionResult

}
