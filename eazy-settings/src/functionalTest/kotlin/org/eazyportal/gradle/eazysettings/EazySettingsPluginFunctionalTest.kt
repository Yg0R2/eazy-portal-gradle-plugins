package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_EXTENSION_NAME
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
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
 * Black-box coverage for `org.eazyportal.gradle.eazy-settings` (design §6.3/§6.5, TOOLS-68 acceptance criteria):
 * real Gradle builds, driven through TestKit's plugin-under-test classpath,
 * applying the plugin by bare id in a synthetic receiver's `settings.gradle.kts` `plugins { }` block —
 * the normal path a receiver repo uses.
 *
 * Subprojects are named outside the standard archetype set (design §5.2) on purpose:
 * they fall back to `ProjectType.DEFAULT` / `kotlin-project-convention`,
 * which needs no `eazyportal-core` repository to resolve —
 * keeping these tests hermetic while still exercising the auto-apply + version-resolution behavior under test here.
 */
class EazySettingsPluginFunctionalTest {

    @Test
    fun `every subproject gets eazy-project auto-applied, the root does not`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras-a", "extras-b")
        val gradleRunner = gradleRunner(projectDir)

        val output = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME"
        )

        assertThat(output)
            .contains("eazy-project diagnostics — :extras-a")
            .contains("eazy-project diagnostics — :extras-b")

        val rootOutput = gradleRunner.runFailingGradleTask(":$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(rootOutput).contains("not found in root project")
    }

    @Test
    fun `eazy-project's own default applies when the DSL override is absent`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras")

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output)
            .contains("eazyPortalCoreVersion  : $CORE_DEFAULT_VERSION   (source: default (DefaultVersions))")
            .contains("framework              : ${Framework.SPRING_BOOT}")
    }

    // TODO: refactor
    @Test
    fun `the eazySettings framework is fed to every subproject`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            settings {
                generateEazySettingsString()
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).contains("framework              : ${Framework.SPRING_BOOT}")
    }

    @Test
    fun `the eazySettings override wins over eazy-project's own default`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            settings {
                script {
                    generateEazySettingsString()
                }
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).contains("eazyPortalCoreVersion  : $CORE_VERSION   (source: DSL override)")
    }

    @Test
    fun `a subproject's own eazyProject override wins over the eazySettings one, without affecting its siblings`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras-a", "extras-b") {
            settings {
                script {
                    generateEazySettingsString()
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
            .contains("eazyPortalCoreVersion  : $CORE_VERSION   (source: DSL override)")
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

    @Test
    fun `CC double-run - override wins, the 2nd run reuses the CC entry, and zero CC problems occur`(@TempDir workingDir: File) {
        val gradleRunner = buildExampleProject(workingDir, "extras-a", "extras-b") {
            settings {
                script {
                    generateEazySettingsString()
                }
            }
        }.let(::gradleRunner)

        val firstRun = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )
        assertThat(firstRun)
            .contains("Configuration cache entry stored.")
            .contains("eazyPortalCoreVersion  : $CORE_VERSION   (source: DSL override)")
            .doesNotContain("problem was found", "problems were found")

        val secondRun = gradleRunner.runGradleTask(
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )
        assertThat(secondRun)
            .contains("Reusing configuration cache.")
            .contains("eazyPortalCoreVersion  : $CORE_VERSION   (source: DSL override)")
            .doesNotContain("problem was found", "problems were found")
    }

    @Test
    fun `an excluded subproject is left unconfigured, so it can apply java-platform, while the others still get eazy-project`(
        @TempDir workingDir: File,
    ) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            settings {
                script {
                    """
                    $EAZY_SETTINGS_EXTENSION_NAME {
                        exclude = listOf("platform")
                    }
                    """.trimIndent()
                }
            }

            subproject("platform") {
                plugins("java-platform")
            }
        }

        val gradleRunner = gradleRunner(projectDir)

        gradleRunner.runGradleTask(":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME").run {
            assertThat(this).contains("eazy-project diagnostics — :extras")
        }

        gradleRunner.runFailingGradleTask(":platform:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME").run {
            assertThat(this).contains("not found in project ':platform'")
        }
    }

    @Test
    fun `a subproject applying java-platform fails when it is not excluded`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            subproject("platform") {
                plugins("java-platform")
            }
        }

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("help")

        assertThat(output).contains("""The "java-platform" plugin cannot be applied together with the "java" (or "java-library") plugin.""")
    }

    @Test
    fun `a module declaring its own repositories fails the build (FAIL_ON_PROJECT_REPOS)`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
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
        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        val projectDir = buildExampleProject(workingDir, "common")

        val output = gradleRunner(projectDir) {
            gradleUserHome = gradleHomeDir
        }.runFailingGradleTask(":common:dependencies", "--configuration", "compileClasspath")

        assertThat(output)
            .contains("The following Gradle properties are missing for 'GitHubPackages' credentials")
            .doesNotContain("BUILD SUCCESSFUL")
    }

    /**
     * A synthetic receiver: an empty root applying `org.eazyportal.gradle.eazy-settings` via the settings `plugins { }`
     * block (the real receiver bootstrap, README Quickstart) + one subproject per [subprojectNames].
     */
    private fun buildExampleProject(
        workingDir: File,
        vararg subprojectNames: String,
        builder: ExampleProjectBuilder.() -> Unit =  { },
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins("org.eazyportal.gradle.eazy-settings")
            }

            subprojectNames.forEach {
                subproject(it)
            }

            builder()
        }

    private fun generateEazyProjectString(
        eazyPortalCoreVersionOverride: String,
    ): String =
        """
        $EAZY_PROJECT_EXTENSION_NAME {
            eazyPortalCoreVersion = "$eazyPortalCoreVersionOverride"
        }
        """.trimIndent()

    private fun generateEazySettingsString(
        eazyPortalCoreVersionOverride: String = CORE_VERSION,
        framework: Framework = Framework.SPRING_BOOT,
    ): String =
        """
        $EAZY_SETTINGS_EXTENSION_NAME {
            eazyPortalCoreVersion = "$eazyPortalCoreVersionOverride"
            framework = ${Framework::class.java.name}.${framework.name}
        }
        """.trimIndent()

    private companion object {
        const val SUBPROJECT_CORE_VERSION = "0.0.2"
    }

}
