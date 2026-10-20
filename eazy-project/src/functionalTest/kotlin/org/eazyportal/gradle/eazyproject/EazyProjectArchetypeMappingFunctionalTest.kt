package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_PLUGIN_ID
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.Companion.TEST_CONFIGURATIONS
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_TEST_PROJECT_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.createCoreArtifactId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

/**
 * Black-box coverage for the module name -> archetype -> convention mapping of `org.eazyportal.gradle.eazy-project` (design §5.5/§5.7, TOOLS-67 acceptance criteria):
 * real Gradle builds driven through TestKit's plugin-under-test classpath — which,
 * because `eazy-project` depends on `conventions` (the LINCHPIN edge, §5.5),
 * also carries every `org.eazyportal.gradle.*-convention` plugin —
 * proving the cascade actually takes effect rather than just asserting `hasPlugin(...)` in-process
 * (already covered by [org.eazyportal.gradle.eazyproject.configurer.ProjectConfigurerWiringTest] and [org.eazyportal.gradle.eazyproject.configurer.EazyProjectPluginTest]).
 */
class EazyProjectArchetypeMappingFunctionalTest {

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
        // `application` depends on every other layer sibling that exists, except `client` (ApplicationProjectConfigurer),
        // so those siblings must exist as projects — the dependency is a `project.project(":x")` reference,
        // not a real build of them.
        val siblings =
            if (subprojectName == "application") {
                listOf("common", "api", "persistence", "service", "web")
            } else {
                emptyList()
            }

        val projectDir = exampleProject(workingDir) {
            subproject(subprojectName) {
                plugins(EAZY_PROJECT_PLUGIN_ID)
            }

            siblings.forEach {
                subproject(it)
            }
        }

        val output = gradleRunner(projectDir)
            .runGradleTask(":$subprojectName:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        val expectedArchetype = ProjectType.fromProjectName(subprojectName).toString()
        val expectedSiblings = siblings.joinToString(", ", "[", "]") {
            ":$it (implementation)"
        }
        val expectedExampleCoreDependency = subprojectName
            .takeIf { expectedArchetype == it }
            ?.let { "${createCoreArtifactId(subprojectName, Framework.SPRING_BOOT)} (implementation), " }
            .orEmpty()
        val expectedTestDependencies = TEST_CONFIGURATIONS.joinToString(", ") {
            "$CORE_TEST_PROJECT_ARTIFACT_ID ($it)"
        }
        assertThat(output).containsSubsequence(
            "> Task :$subprojectName:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "eazy-project diagnostics — :$subprojectName",
            "  archetype              : $expectedArchetype",
            "  convention             : [$expectedConvention]",
            "  siblings               : $expectedSiblings",
            "  eazyportal-core        : [${expectedExampleCoreDependency}$expectedTestDependencies]   (versions via the eazyportal-core BOM)",
            "  eazyPortalCoreVersion  : $CORE_DEFAULT_VERSION   (source: default (DefaultVersions))",
            "  framework              : ${Framework.SPRING_BOOT}",
        )
    }

}
