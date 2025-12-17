package org.eazyportal.plugin.common.integration.test.gradle.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestContext
import java.io.File

data class GradleProjectTestContext(
    val workingDir: File,
) : TestContext
