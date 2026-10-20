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
 * TOOLS-73 (design §7.4/§7.5, §9.5): `eazyportal-core-test` — added to every module on every test tier — resolves versionless from the `eazyportal-core` BOM.
 * Hermetic/offline like [SampleReceiverPublishingIntegrationTest]: `eazyportal-core-*` resolves from a stub repository built fresh into the @TempDir,
 * with an isolated `GRADLE_USER_HOME` and stubbed, never-used GitHub Packages credentials.
 */
class SampleReceiverCoreTestBomIntegrationTest {

    @Test
    fun `eazyportal-core-test resolves versionless from the BOM on every test tier and on the test fixtures`(
        @TempDir workingDir: File,
    ) {
        val repositoryDir = exampleRepository(workingDir, CORE_DEFAULT_VERSION) {
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
                                    maven { url = uri("${repositoryDir.toURI()}") }
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

        val gradleHomeDir = File(workingDir, "gradle-home").also { it.mkdirs() }

        listOf(
            "testCompileClasspath",
            "functionalTestCompileClasspath",
            "integrationTestCompileClasspath",
            "testFixturesCompileClasspath",
        ).forEach { classpath ->
            val output = gradleRunner(projectDir) {
                gradleUserHome = gradleHomeDir

                stubGitHubCredentials()
            }.runGradleTask(
                ":common:dependencyInsight",
                "--configuration",
                classpath,
                "--dependency",
                createCoreArtifactId("test"),
            )

            assertThat(output)
                .describedAs("eazyportal-core-test on $classpath")
                .contains("$CORE_GROUP_ID:${createCoreArtifactId("test")}:$CORE_DEFAULT_VERSION")
                .doesNotContain("FAILED")
        }
    }

}
