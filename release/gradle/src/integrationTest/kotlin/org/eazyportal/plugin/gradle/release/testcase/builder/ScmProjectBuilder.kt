package org.eazyportal.plugin.gradle.release.testcase.builder

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.dsl.annotation.IntegrationTestDsl
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

@IntegrationTestDsl
class ScmProjectBuilder(
    private val workingDir: File,
) {

    private val submodules = mutableMapOf<String, ScmProjectBuilder>()

    fun build(
        scmActions: TestScmActions<*>,
        gradleProjectBuilder: GradleProjectBuilder,
    ) {

    }

    fun withSubmodule(
        submoduleName: String,
        submoduleInitBlock: ScmProjectBuilder.() -> Unit = {},
    ): ScmProjectBuilder =
        apply {
            submodules[submoduleName] = ScmProjectBuilder(
                workingDir.resolve("$PROJECT_NAME/$submoduleName")
            ).apply {
                submoduleInitBlock(this)
            }

        }

    fun withSubmodules(vararg submoduleNames: String): ScmProjectBuilder =
        apply {
            submoduleNames.forEach {
                withSubmodule(it)
            }
        }

}
