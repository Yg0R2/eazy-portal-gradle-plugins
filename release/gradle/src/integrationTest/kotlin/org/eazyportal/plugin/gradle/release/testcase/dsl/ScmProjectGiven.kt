package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.GradleProjectGiven
import java.io.File

class ScmProjectGiven(
    private val projectDir: File,
    private val submoduleDirs: List<File>,
) : GradleProjectGiven(projectDir) {


    override fun build() {
        // TODO: initialize project and submodule
        super.build()
    }

}
