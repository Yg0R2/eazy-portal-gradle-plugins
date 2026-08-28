package org.eazyportal.gradle.internal

import org.eazyportal.gradle.internal.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for `org.eazyportal.gradle.kotlin-project-convention` (design §4.3, TOOLS-61 acceptance criteria):
 * a Kotlin compiler warning fails by default and passes with `-PsuppressAllErrors`, and applying the plugin
 * transitively applies `java-project-convention` (toolchain + JUnit platform).
 */
class KotlinProjectConventionTest {

    @Test
    fun `a kotlin compiler warning fails the build by default`(@TempDir projectDir: File) {
        writeSampleProject(projectDir, true)

        val output = runFailingGradleTask(projectDir, "compileKotlin", withPluginClasspath = true)

        assertThat(output).contains("is deprecated")
    }

    @Test
    fun `a kotlin compiler warning passes the build with -PsuppressAllErrors`(@TempDir projectDir: File) {
        writeSampleProject(projectDir, true)

        val output = runGradleTask(projectDir, "compileKotlin", "-PsuppressAllErrors", withPluginClasspath = true)

        assertThat(output).contains("BUILD SUCCESSFUL")
    }

    @Test
    fun `applying kotlin-project-convention transitively applies java-project-convention`(@TempDir projectDir: File) {
        writeSampleProject(projectDir, false)
        File(projectDir, "build.gradle.kts").appendText(
            """

            tasks.register("verifyConventions") {
                // Capture at configuration time (the doLast receiver is the Task, not the Project).
                val javaConventionApplied = project.plugins.hasPlugin("org.eazyportal.gradle.java-project-convention")
                val javaPluginApplied = project.plugins.hasPlugin("java")
                val toolchainVersion = project.extensions.getByType(JavaPluginExtension::class.java)
                    .toolchain.languageVersion.get().asInt()
                val junitPlatform = project.tasks.named("test", org.gradle.api.tasks.testing.Test::class.java).get()
                    .options is org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions
                doLast {
                    println("java-project-convention applied: ${'$'}javaConventionApplied")
                    println("java plugin applied: ${'$'}javaPluginApplied")
                    println("toolchain: ${'$'}toolchainVersion")
                    println("junit platform: ${'$'}junitPlatform")
                }
            }
            """.trimIndent(),
        )

        val output = runGradleTask(projectDir, "verifyConventions", withPluginClasspath = true)

        assertThat(output)
            .contains("java-project-convention applied: true")
            .contains("java plugin applied: true")
            .contains("toolchain: 25")
            .contains("junit platform: true")
    }

    /** A minimal Kotlin project; when [enableWarnings] is set, it emits an "unused variable" warning. */
    private fun writeSampleProject(projectDir: File, enableWarnings: Boolean) {
        File(projectDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "sample"
            """.trimIndent(),
        )
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("org.eazyportal.gradle.kotlin-project-convention")
            }

            repositories {
                mavenCentral()   // resolve kotlin-stdlib for compilation
            }
            """.trimIndent(),
        )
        if (enableWarnings) {
            val sourceDir = File(projectDir, "src/main/kotlin/sample").apply { mkdirs() }
            File(sourceDir, "Sample.kt").writeText(
                """
                package sample

                @Deprecated("emit a compiler warning")
                fun deprecated() {}

                fun sample() {
                    deprecated()
                }
                """.trimIndent(),
            )
        }
    }

}
