package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_PLUGIN_ID
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_SNAPSHOT_VERSION
import org.eazyportal.gradle.utils.repository.ExampleRepositoryBuilder.Companion.exampleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for the conventions `org.eazyportal.gradle.eazy-project` applies (design §5.5/§5.7):
 * real Gradle builds driven through TestKit's plugin-under-test classpath,
 * proving the transitively-applied convention actually takes effect — real compilation and publishing, not just plugin ids.
 */
class EazyProjectConventionFunctionalTest {

    @Test
    fun `the transitively-applied convention takes effect, not just its id`(@TempDir workingDir: File) {
        val exampleRepositoryDir = exampleRepository(workingDir, CORE_DEFAULT_VERSION) {
            eazyportalArtifacts()
        }

        val projectDir = buildExampleProject(
            workingDir = workingDir,
            subprojectName = "common",
            repositoryDir = exampleRepositoryDir,
        ) {
            kotlinSource {
                """
                package org.eazyportal.example

                // kotlin-library-convention (transitively applied by eazy-project) turns on explicitApi() —
                // an implicitly-public top-level declaration must fail to compile under it.
                fun greetings(): String = "Hello World!"
                """
            }
        }

        val output = gradleRunner(projectDir)
            .runFailingGradleTask(":common:compileKotlin")

        assertThat(output).contains("Visibility must be specified in explicit API mode.")
    }

    @Test
    fun `a default module applies kotlin-library-convention by bare id and publishes`(@TempDir workingDir: File) {
        val repositoryDir = exampleRepository(workingDir)

        val projectDir = buildExampleProject(
            workingDir = workingDir,
            subprojectName = "extras",
            extraPluginIds = listOf("org.eazyportal.gradle.kotlin-library-convention"),
        )

        gradleRunner(projectDir)
            .runGradleTask(
                ":extras:publishToMavenLocal",
                "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION",
                "-Dmaven.repo.local=${repositoryDir.absolutePath}"
            )
    }

    /**
     * A receiver: an empty root + `subprojectName`'s subproject applying `eazy-project` [+ `extraPluginIds`].
     * The root carries `allprojects { repositories { mavenCentral() [+ repositoryDir] } }` — needed because real compilation/publishing is exercised.
     * [repositoryDir], when given, is the `file://`-published sample
     * so a real `compileClasspath` resolution of `org.eazyportal.core:eazyportal-core-*`
     * (added unconditionally by every [org.eazyportal.gradle.eazyproject.configurer.GradleProjectConfigurer]) succeeds —
     * those coordinates are design placeholders for a real internal registry, not anything mavenCentral() has.
     */
    private fun buildExampleProject(
        workingDir: File,
        subprojectName: String,
        extraPluginIds: List<String> = emptyList(),
        repositoryDir: File? = null,
        configureSubproject: ExampleProjectBuilder.SubprojectBuilder.() -> Unit = {},
    ): File =
        exampleProject(workingDir) {
            rootProject {
                script {
                    """
                    allprojects {
                        repositories {
                            mavenCentral()
                            ${repositoryDir?.let { "maven { url = uri(\"${it.toURI()}\") }" }.orEmpty()}
                        }
                    }
                    """.trimIndent()
                }
            }

            subproject(subprojectName) {
                plugins(EAZY_PROJECT_PLUGIN_ID, *extraPluginIds.toTypedArray())

                configureSubproject()
            }
        }

}
