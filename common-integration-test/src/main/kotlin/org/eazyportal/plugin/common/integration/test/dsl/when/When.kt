package org.eazyportal.plugin.common.integration.test.dsl.`when`

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl
import org.eazyportal.plugin.common.integration.test.dsl.`when`.WhenContext

@IntegrationTestDsl
open class When<out C : WhenContext>(
    private val context: C,
)
