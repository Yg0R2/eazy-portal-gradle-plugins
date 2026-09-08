package org.eazyportal.gradle.internal

import org.eazyportal.gradle.internal.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.eazyportal.gradle.internal.project.ExampleProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for `org.eazyportal.gradle.kotlin-project-convention` (design §4.3, TOOLS-61 acceptance criteria):
 * a Kotlin compiler warning fails by default and passes with `-PsuppressAllErrors`, and applying the plugin
 * transitively applies `java-project-convention` (toolchain + JUnit platform).
 */
class KotlinProjectConventionIntegrationTest {

    @Test
    fun `a kotlin compiler warning fails the build by default`(@TempDir projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.kotlin-project-convention")
            .withMavenCentral()   // resolve kotlin-stdlib for compilation
            .withKotlinSource {
                KOTLIN_SOURCE_WITH_COMPILER_WARNING
            }.build()

        val output = runFailingGradleTask(projectDir, "compileKotlin", withPluginClasspath = true)

        assertThat(output).contains("is deprecated")
    }

    @Test
    fun `a kotlin compiler warning passes the build with -PsuppressAllErrors`(@TempDir projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.kotlin-project-convention")
            .withMavenCentral()   // resolve kotlin-stdlib for compilation
            .withKotlinSource {
                KOTLIN_SOURCE_WITH_COMPILER_WARNING
            }.build()

        runGradleTask(projectDir, "compileKotlin", "-PsuppressAllErrors", withPluginClasspath = true)
    }

    @Test
    fun `applying kotlin-project-convention transitively applies java-project-convention`(@TempDir projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.kotlin-project-convention")
            .withMavenCentral()   // resolve kotlin-stdlib for compilation
            .withKotlinSource()
            .withBuildScript {
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
                """.trimIndent()
            }.build()

        val output = runGradleTask(projectDir, "verifyConventions", withPluginClasspath = true)

        assertThat(output)
            .contains("java-project-convention applied: true")
            .contains("java plugin applied: true")
            .contains("toolchain: 25")
            .contains("junit platform: true")
    }

    companion object {
        /** A minimal Kotlin code that emits a deprecation warning. */
        private val KOTLIN_SOURCE_WITH_COMPILER_WARNING =
            """
            package org.eazyportal.example

            @Deprecated("emit a compiler warning")
            fun deprecatedGreetings() {}

            fun greetings() {
                deprecatedGreetings()
            }
            """.trimIndent()
    }

}
