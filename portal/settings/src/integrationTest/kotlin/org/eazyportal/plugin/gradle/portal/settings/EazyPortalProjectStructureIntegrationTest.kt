package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.SUBPROJECT_NAMES
import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.eazyportal.plugin.common.integration.test.testcase.TestCaseBuilder.givenTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class EazyPortalProjectStructureIntegrationTest {

    @Test
    fun test_validateProjectStructure(@TempDir workingDir: File) {
        givenTestCase<BaseGradleProjectTestCase>(workingDir) {
            withEazyPortalSettingsPlugin()
            withSubprojectNames(*SUBPROJECT_NAMES)
            withExtraSettingsConfig(
                """
                eazyPortal {
                    applyCoreDependencies = false
                }
                """.trimIndent()
            )

            withExtraProjectConfig(
                """
                allprojects {
                    dependencies {
                        testImplementation(platform("org.junit:junit-bom:+"))
                
                        testImplementation("org.junit.jupiter:junit-jupiter")
                        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
                    }
                }
                """.trimIndent()
            )
        }.givenConfiguration {
            SUBPROJECT_NAMES.forEach {
                projectDir.copyIntoFromResources(
                    this@EazyPortalProjectStructureIntegrationTest::class.java.simpleName,
                    "$it/",
                )
            }
        }.whenGradleTaskSucceeds("build")
            .thenAssertTaskOutput {
                contains(
                    *listOf("", *SUBPROJECT_NAMES.map { ":$it" }.toTypedArray())
                        .map { "> Task $it:test" }
                        .toTypedArray()
                )
            }
    }

}
