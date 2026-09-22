package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * End-to-end coverage for the REAL `foojay-resolver-convention` wiring in `org.eazyportal.gradle.eazy-settings` (design §6.3/§9.4 extension):
 * this test drives an actual network call to `api.foojay.io`,
 * proving the plugin's real toolchain auto-provisioning path works,
 * not just the underlying Gradle mechanism it relies on.
 *
 * Requires network access to `api.foojay.io` — deliberately not hermetic
 * (design decision: every build in this project already depends on network access at build or test time, either for dependencies or JDK downloads).
 */
class EazySettingsPluginIntegrationTest {

    @Test
    fun `a toolchain vendor with no real match fails cleanly after a real foojay lookup`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            script {
                """
                java {
                    toolchain {
                        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(25))
                        vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.matching("eazyportal-nonexistent-vendor-tools-70"))
                    }
                }
                """.trimIndent()
            }
        }

        val output = gradleRunner(projectDir).runFailingGradleTask("compileJava")

        assertThat(output)
            .contains("Cannot find a Java installation")
            .contains("No matching toolchain could be found in the configured toolchain download repositories")
    }

    @Test
    fun `a toolchain vendor with current java version`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            script {
                """
                java {
                    toolchain {
                        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(25))
                    }
                }
                """.trimIndent()
            }
        }

        val output = gradleRunner(projectDir) {
            stubEnvironment()
        }.runGradleTask("compileJava")

        assertThat(output)
            .contains("> Task :compileJava")
    }

    private fun buildExampleProject(
        workingDir: File,
        rootBuilder: ExampleProjectBuilder.RootProjectBuilder.() -> Unit = {},
    ): File =
         exampleProject(workingDir) {
            settings {
                plugins("org.eazyportal.gradle.eazy-settings")
            }

            rootProject {
                plugins("java")

                rootBuilder()

                javaSource()
            }
        }

}
