package org.eazyportal.plugin.common.integration.test.testcase.dsl

import java.io.File

data class GradleProjectContext(
    val workingDir: File,
) : TestContext
