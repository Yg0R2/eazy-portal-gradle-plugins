package org.eazyportal.plugin.gradle.portal.settings

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.SUBPROJECT_NAMES
import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase
import org.eazyportal.plugin.common.integration.test.testcase.TestCaseBuilder.givenTestCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ProjectStructureIntegrationTest {

    @Test
    fun test_applyPlugin(@TempDir workingDir: File) {
        givenTestCase<BaseGradleProjectTestCase>(workingDir) {
            withEazyPortalSettingsPlugin()
            withSubprojectNames(*SUBPROJECT_NAMES)
            withExtraSettingsConfig(
                """
                eazyPortal {
                    applicationType = org.eazyportal.plugin.gradle.portal.common.model.ApplicationTypes.SPRING_BOOT
                }
                """.trimIndent()
            )
        }.whenGradleTaskSucceeds("projects")
            .thenAssertTaskOutput {
                val expectedSubprojects = SUBPROJECT_NAMES.withIndex()
                    .map { (index, subprojectName) ->
                        if (index < SUBPROJECT_NAMES.size - 1) {
                            "+--- Project ':$subprojectName'"
                        } else {
                            "\\--- Project ':$subprojectName'"
                        }
                    }.toTypedArray()

                contains(
                    "Root project '$PROJECT_NAME'",
                    *expectedSubprojects,
                )
            }
    }

}
