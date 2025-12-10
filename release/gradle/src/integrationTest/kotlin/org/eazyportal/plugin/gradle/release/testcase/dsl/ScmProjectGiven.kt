package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectGiven
import java.io.File

class ScmProjectGiven(
    private val projectDir: File,
) : GradleProjectGiven(projectDir) {

}
