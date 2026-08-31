package org.eazyportal.gradle.internal

import org.eazyportal.gradle.internal.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.eazyportal.gradle.internal.project.ExampleProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for `org.eazyportal.gradle.java-project-convention` (design §4.2, TOOLS-61 acceptance criteria):
 * a javac lint warning fails the build by default and passes with `-PsuppressAllErrors`.
 */
class JavaProjectConventionTest {

    @Test
    fun `a javac lint warning fails the build by default`(@TempDir projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.java-project-convention")
            .withJavaSource {
                JAVA_SOURCE_WITH_COMPILER_WARNING
            }.build()

        val output = runFailingGradleTask(projectDir, "compileJava", withPluginClasspath = true)

        assertThat(output)
            .contains("[cast] redundant cast")
            .contains("warnings found and -Werror specified")
    }

    @Test
    fun `a javac lint warning passes the build with -PsuppressAllErrors`(@TempDir projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.java-project-convention")
            .withJavaSource {
                JAVA_SOURCE_WITH_COMPILER_WARNING
            }.build()

        val output = runGradleTask(projectDir, "compileJava", "-PsuppressAllErrors", withPluginClasspath = true)

        assertThat(output).contains("BUILD SUCCESSFUL")
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
