package org.eazyportal.plugin.common.integration.test.dsl.then

import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl
import org.eazyportal.plugin.common.integration.test.dsl.then.ThenContext

@IntegrationTestDsl
abstract class Then<out C : ThenContext>(
    private val context: C,
)
