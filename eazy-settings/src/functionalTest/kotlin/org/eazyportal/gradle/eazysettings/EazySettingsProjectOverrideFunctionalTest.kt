package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_EXTENSION_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.repository.ExampleRepositoryBuilder.Companion.exampleRepository
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_GROUP_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_VERSION
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.createCoreArtifactId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for a subproject's own `eazyProject { }` block overriding the values fed by `eazy-settings`:
 * real Gradle builds driven through TestKit's plugin-under-test classpath.
 * The subprojects are named outside the standard archetype set (design §5.2), so they fall back to the `default` archetype and stay hermetic.
 */
class EazySettingsProjectOverrideFunctionalTest {

    @Test
    fun `a subproject's own eazyProject override wins over the eazySettings one, without affecting its siblings`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras-a", "extras-b") {
            settings {
                script {
                    """
                    $EAZY_SETTINGS_EXTENSION_NAME {
                        eazyPortalCoreVersion = "$SETTINGS_CORE_VERSION"
                    }
                    """.trimIndent()
                }
            }

            subproject("extras-a") {
                script {
                    generateEazyProjectString(SUBPROJECT_CORE_VERSION)
                }
            }
        }

        val gradleRunner = gradleRunner(projectDir)

        val overriddenOutput = gradleRunner.runGradleTask(":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")
        val inheritedOutput = gradleRunner.runGradleTask(":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(overriddenOutput)
            .contains("eazyPortalCoreVersion  : $SUBPROJECT_CORE_VERSION   (source: DSL override)")
        assertThat(inheritedOutput)
            .contains("eazyPortalCoreVersion  : $SETTINGS_CORE_VERSION   (source: DSL override)")
            .doesNotContain(SUBPROJECT_CORE_VERSION)
    }

    @Test
    fun `a subproject's own eazyProject override wins over eazy-project's own default`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            subproject("extras") {
                script {
                    generateEazyProjectString(SUBPROJECT_CORE_VERSION)
                }
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output)
            .contains("eazyPortalCoreVersion  : $SUBPROJECT_CORE_VERSION   (source: DSL override)")
    }

    @Test
    fun `a subproject's own eazyProject override is fed into its eazyportal-core BOM`(@TempDir workingDir: File) {
        val exampleRepositoryDir = exampleRepository(workingDir, SUBPROJECT_CORE_VERSION) {
            eazyportalArtifacts()
        }

        val projectDir = buildExampleProject(workingDir, "common") {
            settings {
                script {
                    """
                    dependencyResolutionManagement {
                        repositories {
                            exclusiveContent {
                                forRepository {
                                    maven { url = uri("${exampleRepositoryDir.toURI()}") }
                                }

                                filter {
                                    includeGroup("$CORE_GROUP_ID")
                                }
                            }
                        }
                    }
                    """.trimIndent()
                }
            }

            subproject("common") {
                script {
                    generateEazyProjectString(SUBPROJECT_CORE_VERSION)
                }
            }
        }

        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        val output = gradleRunner(projectDir) {
            gradleUserHome = gradleHomeDir

            stubGitHubCredentials()
        }.runGradleTask(":common:dependencies", "--configuration", "compileClasspath")

        assertThat(output).contains("$CORE_GROUP_ID:${createCoreArtifactId("common")}:$SUBPROJECT_CORE_VERSION")
    }

    private fun buildExampleProject(
        workingDir: File,
        vararg subprojectNames: String,
        builder: ExampleProjectBuilder.() -> Unit = { },
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)
            }

            subprojectNames.forEach {
                subproject(it)
            }

            builder()
        }

    private fun generateEazyProjectString(eazyPortalCoreVersion: String): String =
        """
        $EAZY_PROJECT_EXTENSION_NAME {
            eazyPortalCoreVersion = "$eazyPortalCoreVersion"
        }
        """.trimIndent()

    private companion object {
        const val SETTINGS_CORE_VERSION = "0.0.1"
        const val SUBPROJECT_CORE_VERSION = "0.0.2"
    }

}
