package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for `org.eazyportal.gradle.java-project-convention` (design §4.2, TOOLS-61 acceptance criteria):
 * a javac lint warning fails the build by default and passes with `-PsuppressAllErrors`.
 */
class JavaProjectConventionIntegrationTest {

    @Test
    fun `a javac lint warning fails the build by default`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("compileJava")

        assertThat(output)
            .contains("[cast] redundant cast")
            .contains("warnings found and -Werror specified")
    }

    @Test
    fun `a javac lint warning passes the build with -PsuppressAllErrors`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        gradleRunner(projectDir)
            .runGradleTask("compileJava", "-PsuppressAllErrors")
    }

    private fun buildExampleProject(workingDir: File): File =
        exampleProject(workingDir) {
            rootProject {
                plugins("org.eazyportal.gradle.java-project-convention")

                javaSource {
                    JAVA_SOURCE_WITH_COMPILER_WARNING
                }
            }
        }

    companion object {
        /** A minimal Java code that emits a `-Xlint:cast` warning (redundant cast). */
        private val JAVA_SOURCE_WITH_COMPILER_WARNING =
            """
            package org.eazyportal.example;

            public class Example {
                public String greetings() {
                    return (String) "redundant cast triggers -Xlint:cast";
                }
            }
            """.trimIndent()
    }

}
