package org.eazyportal.plugin.common.integration.test.dsl

import org.eazyportal.plugin.common.integration.test.dsl.given.GivenContext
import org.eazyportal.plugin.common.integration.test.dsl.then.ThenContext
import org.eazyportal.plugin.common.integration.test.dsl.`when`.WhenContext

interface TestContext : GivenContext, WhenContext, ThenContext
