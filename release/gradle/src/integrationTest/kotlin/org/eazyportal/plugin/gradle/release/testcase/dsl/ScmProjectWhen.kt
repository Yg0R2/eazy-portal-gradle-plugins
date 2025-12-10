package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectWhen
import java.io.File

class ScmProjectWhen(
    private val projectDir: File,
    private val submoduleDirs: List<File>,
) : GradleProjectWhen(projectDir) {

}
