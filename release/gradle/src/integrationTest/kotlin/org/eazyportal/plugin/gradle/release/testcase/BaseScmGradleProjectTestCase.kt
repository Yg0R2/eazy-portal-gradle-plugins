package org.eazyportal.plugin.gradle.release.testcase

import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import java.io.File

open class BaseScmGradleProjectTestCase(
    override val workingDir: File
) : BaseGradleProjectTestCase(workingDir) {

}
