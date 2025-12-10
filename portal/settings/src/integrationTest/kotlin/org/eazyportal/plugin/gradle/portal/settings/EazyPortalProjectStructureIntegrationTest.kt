package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.SUBPROJECT_NAMES
import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.junit.jupiter.api.Test

class EazyPortalProjectStructureIntegrationTest : BaseGradleProjectTestCase() {

    // TODO: Configuration with name 'testImplementation' not found.
    @Test
    fun test_validateProjectStructure() = runTestCase {
        givenTestCase {
            withEazyPortalSettingsPlugin()

            SUBPROJECT_NAMES.forEach {
                withSubproject(it) {
                    // TODO: java plugin should be applied to all Gradle subproject
                    withProjectPlugins("java")
                    withExtraProjectConfig(
                        """
                        dependencies {
                            testImplementation(platform("org.junit:junit-bom:+"))
                    
                            testImplementation("org.junit.jupiter:junit-jupiter")
                            testRuntimeOnly("org.junit.platform:junit-platform-launcher")
                        }
                        """.trimIndent()
                    )
                }
            }

            withExtraSettingsConfig(
                """
                eazyPortal {
                    applyCoreDependencies = false
                }
                """.trimIndent()
            )
        }
        andGivenSetUp {
            SUBPROJECT_NAMES.forEach {
                projectDir.copyIntoFromResources(
                    this@EazyPortalProjectStructureIntegrationTest::class.java.simpleName,
                    "$it/",
                )
            }
        }
        whenExecute {
            taskSucceeds("build")
        }
        thenValidate {
            taskOutput {
                containsAll(
                    // TODO: fix project setup:
                    //  - "> Task :test SKIPPED"
                    //  - "> Task :dummy-common:test NO-SOURCE"
                    //  - "> Task :dummy-ui:test NO-SOURCE"
                    listOf("", *SUBPROJECT_NAMES.map { ":$it" }.toTypedArray())
                        .map { "> Task $it:test" }
                )
            }
        }
    }

}
