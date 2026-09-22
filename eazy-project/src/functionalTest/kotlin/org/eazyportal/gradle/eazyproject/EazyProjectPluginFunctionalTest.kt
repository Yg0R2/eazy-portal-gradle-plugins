package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.conventions.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.conventions.GradleUtils.runGradleTask
import org.eazyportal.gradle.eazyproject.DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.repository.ExampleRepositoryBuilder.Companion.exampleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

/**
 * Black-box coverage for `org.eazyportal.gradle.eazy-project` (design §5.5/§5.7, TOOLS-67 acceptance criteria):
 * real Gradle builds,
 * driven through TestKit's plugin-under-test classpath — which,
 * because `eazy-project` depends on `conventions` (the LINCHPIN edge, §5.5),
 * also carries every `org.eazyportal.gradle.*-convention` plugin —
 * proving the cascade actually takes effect rather than just asserting `hasPlugin(...)` in-process
 * (already covered by [org.eazyportal.gradle.eazyproject.configurer.ProjectConfigurerWiringTest] and [org.eazyportal.gradle.eazyproject.configurer.EazyProjectPluginTest]).
 */
class EazyProjectPluginFunctionalTest {

    @CsvSource(
        value = [
            "common,        org.eazyportal.gradle.kotlin-library-convention",
            "application,   org.eazyportal.gradle.kotlin-project-convention",
            "extras,        org.eazyportal.gradle.kotlin-project-convention",
        ]
    )
    @ParameterizedTest(name = "module ''{0}'' maps to {1}")
    fun `archetype to convention mapping is correct`(
        subprojectName: String,
        expectedConvention: String,
        @TempDir workingDir: File,
    ) {
        // `application` depends on every other layer sibling (ApplicationProjectConfigurer),
        // so those siblings must exist as projects — the dependency is a `project.project(":x")` reference,
        // not a real build of them.
        val siblings =
            if (subprojectName == "application") {
                listOf("common", "api", "persistence", "service", "client", "web")
            } else {
                emptyList()
            }

        val projectDir = buildExampleProject(workingDir, subprojectName, siblingProjectNames = siblings)

        val output = runGradleTask(projectDir, ":$subprojectName:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        val expectedArchetype = ProjectType.fromProjectName(subprojectName).toString()
        val expectedSiblings = siblings.joinToString(", ", "[", "]") {
            ":$it (implementation)"
        }
        val expectedExampleCoreDependency = subprojectName
            .takeIf { expectedArchetype == it }
            ?.let { "eazyportal-core-$subprojectName (implementation), " }
            .orEmpty()
        assertThat(output).containsSubsequence(
            "> Task :$subprojectName:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "eazy-project diagnostics — :$subprojectName",
            "  archetype              : $expectedArchetype",
            "  convention             : [$expectedConvention]",
            "  siblings               : $expectedSiblings",
            "  eazyportal-core        : [${expectedExampleCoreDependency}eazyportal-core-test (testImplementation)]   (versions via the eazyportal-core BOM)",
            "  eazyPortalCoreVersion  : $EXAMPLE_CORE_DEFAULT_VERSION   (source: default (DefaultVersions))",
        )
    }

    @Test
    fun `applying to the root project throws`(@TempDir workingDir: File) {
        val projectDir = exampleProject(workingDir) {
            rootProject {
                plugins("org.eazyportal.gradle.eazy-project")
            }
        }

        val output = runFailingGradleTask(projectDir, "help")

        assertThat(output)
            .contains("org.eazyportal.gradle.eazy-project plugin must not be applied to the root project — it is only for subprojects.")
    }

    @Test
    fun `the transitively-applied convention takes effect, not just its id`(@TempDir workingDir: File) {
        val exampleRepositoryDir = buildExampleRepository(workingDir)

        val projectDir = buildExampleProject(
            workingDir = workingDir,
            subprojectName = "common",
            exampleRepositoryDir = exampleRepositoryDir,
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

        val output = runFailingGradleTask(projectDir, ":common:compileKotlin")

        assertThat(output).contains("Visibility must be specified in explicit API mode.")
    }

    @Test
    fun `a default module applies kotlin-library-convention by bare id and publishes`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(
            workingDir = workingDir,
            subprojectName = "extras",
            extraPluginIds = listOf("org.eazyportal.gradle.kotlin-library-convention"),
        )

        runGradleTask(projectDir, ":extras:publishToMavenLocal", "-Pversion=1.0.0-SNAPSHOT")
    }

    /**
     * A receiver: an empty root + `moduleName`'s subproject applying `eazy-project` [+ `extraPluginIds`],
     * plus an empty subproject per [siblingProjectNames] — needed only so a `project.project(":x")` sibling reference
     * (e.g. `application`'s, which depends on every other layer) resolves; they are never built themselves.
     * The root always carries `allprojects { repositories { mavenCentral() [+ exampleRepositoryDir] } }` — needed once
     * real compilation/publishing is exercised. [exampleRepositoryDir], when given, is the `file://`-published sample
     * (see [buildExampleRepository]) so a real `compileClasspath` resolution of `org.eazyportal.core:eazyportal-core-*`
     * (added unconditionally by every [org.eazyportal.gradle.eazyproject.configurer.GradleProjectConfigurer]) succeeds —
     * those coordinates are design placeholders for a real internal registry, not anything mavenCentral() has.
     */
    private fun buildExampleProject(
        workingDir: File,
        subprojectName: String,
        extraPluginIds: List<String> = emptyList(),
        siblingProjectNames: List<String> = emptyList(),
        exampleRepositoryDir: File? = null,
        configureSubproject: ExampleProjectBuilder.SubprojectBuilder.() -> Unit = {},
    ): File =
        exampleProject(workingDir) {
            rootProject {
                script {
                    """
                    allprojects {
                        repositories {
                            mavenCentral()
                            ${exampleRepositoryDir?.let { "maven { url = uri(\"${it.toURI()}\") }" }.orEmpty()}
                        }
                    }
                    """.trimIndent()
                }
            }

            subproject(subprojectName) {
                plugins("org.eazyportal.gradle.eazy-project", *extraPluginIds.toTypedArray())

                configureSubproject()
            }

            siblingProjectNames.forEach {
                subproject(it)
            }
        }

    /**
     * A synthetic Maven2-layout repository, containing only the minimal artifacts needed to satisfy a Gradle `compileClasspath` resolution of
     * `org.eazyportal.core:eazyportal-core-{bom,common,test}:[EXAMPLE_CORE_DEFAULT_VERSION]` (design §7.4).
     */
    private fun buildExampleRepository(workingDir: File): File =
        exampleRepository(workingDir, EXAMPLE_CORE_DEFAULT_VERSION) {
            eazyportalArtifacts()
        }

}
