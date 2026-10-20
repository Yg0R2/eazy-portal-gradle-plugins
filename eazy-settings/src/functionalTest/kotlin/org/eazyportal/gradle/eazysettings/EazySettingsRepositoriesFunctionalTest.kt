package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.repository.ExampleRepositoryBuilder.Companion.exampleRepository
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_GROUP_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.createCoreArtifactId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for the centralized dependency repositories and their credentials (design §6.5):
 * real Gradle builds driven through TestKit's plugin-under-test classpath.
 */
class EazySettingsRepositoriesFunctionalTest {

    @Test
    fun `a module declaring its own repositories fails the build (FAIL_ON_PROJECT_REPOS)`(@TempDir workingDir: File) {
        val projectDir = exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)
            }

            subproject("extras") {
                useMavenCentral()
            }
        }

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("help")

        assertThat(output).contains("Build was configured to prefer settings repositories over project repositories")
    }

    @Test
    fun `the hermetic stub resolves eazyportal-core with no credentials — the real GitHub repo is registered but never hit`(
        @TempDir workingDir: File,
    ) {
        val exampleRepositoryDir = exampleRepository(workingDir, CORE_DEFAULT_VERSION) {
            eazyportalArtifacts()
        }

        val projectDir = exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)

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

            subproject("common")
        }

        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        val output = gradleRunner(projectDir) {
            gradleUserHome = gradleHomeDir

            stubGitHubCredentials()
        }.runGradleTask(":common:dependencies", "--configuration", "compileClasspath")

        assertThat(output).contains("$CORE_GROUP_ID:${createCoreArtifactId("common")}:$CORE_DEFAULT_VERSION")
    }

    @Test
    fun `forcing resolution from the real GitHub repo without credentials fails with a clear error, never silent anonymous`(
        @TempDir workingDir: File,
    ) {
        val projectDir = exampleProject(workingDir) {
            settings {
                plugins(EAZY_SETTINGS_PLUGIN_ID)
            }

            subproject("common")
        }

        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        val output = gradleRunner(projectDir) {
            gradleUserHome = gradleHomeDir
        }.runFailingGradleTask(":common:dependencies", "--configuration", "compileClasspath")

        assertThat(output)
            .contains("The following Gradle properties are missing for 'GitHubPackages' credentials")
            .doesNotContain("BUILD SUCCESSFUL")
    }

}
