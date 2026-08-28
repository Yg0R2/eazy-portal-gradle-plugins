package org.eazyportal.gradle.internal

import org.eazyportal.gradle.internal.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
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
        writeSampleProject(projectDir)

        val output = runFailingGradleTask(projectDir, "compileJava", withPluginClasspath = true)

        assertThat(output)
            .contains("[cast] redundant cast")
            .contains("warnings found and -Werror specified")
    }

    @Test
    fun `a javac lint warning passes the build with -PsuppressAllErrors`(@TempDir projectDir: File) {
        writeSampleProject(projectDir)

        val output = runGradleTask(projectDir, "compileJava", "-PsuppressAllErrors", withPluginClasspath = true)

        assertThat(output).contains("BUILD SUCCESSFUL")
    }

    /** A minimal Java project that emits a `-Xlint:cast` warning (redundant cast). */
    private fun writeSampleProject(projectDir: File) {
        File(projectDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "sample"
            """.trimIndent(),
        )
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("org.eazyportal.gradle.java-project-convention")
            }
            """.trimIndent(),
        )
        val sourceDir = File(projectDir, "src/main/java/sample").apply { mkdirs() }
        File(sourceDir, "Sample.java").writeText(
            """
            package sample;

            public class Sample {
                public String value() {
                    return (String) "redundant cast triggers -Xlint:cast";
                }
            }
            """.trimIndent(),
        )
    }

}
