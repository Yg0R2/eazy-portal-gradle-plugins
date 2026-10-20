package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for `org.eazyportal.gradle.eazy-project` being subprojects-only (design §5.5):
 * a real Gradle build driven through TestKit's plugin-under-test classpath.
 */
class EazyProjectRootProjectFunctionalTest {

    @Test
    fun `applying to the root project throws`(@TempDir workingDir: File) {
        val projectDir = exampleProject(workingDir) {
            rootProject {
                plugins(EAZY_PROJECT_PLUGIN_ID)
            }
        }

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("help")

        assertThat(output)
            .contains("$EAZY_PROJECT_PLUGIN_ID plugin must not be applied to the root project — it is only for subprojects.")
    }

}
