package org.eazyportal.gradle.eazyproject.assertion

import org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.Companion.TEST_CONFIGURATIONS
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.IMPLEMENTATION
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_BOM_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_TEST_PROJECT_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.createCoreArtifactId
import org.assertj.core.api.AbstractListAssert
import org.assertj.core.api.ObjectAssert

/** Assertion over the Gradle build output that contains the `eazyDiagnostics` block(s) of one or more modules. */
class DiagnosticsAssert(
    actual: List<String>,
) : AbstractListAssert<DiagnosticsAssert, List<String>, String, ObjectAssert<String>>(
    actual,
    DiagnosticsAssert::class.java,
) {

    /**
     * The output contains the exact `eazyDiagnostics` block of [projectName], line by line and in order.
     *
     * @param siblingProjects expected sibling projects as `name to configuration`, in wiring order.
     * @param eazyPortalCore the expected `eazyportal-core` section, or `null` when `applyEazyPortalCoreDependencies` is off.
     */
    fun hasDiagnostics(
        projectName: String,
        convention: String,
        siblingProjects: List<Pair<String, DependencyConfiguration>> = emptyList(),
        eazyPortalCore: EazyPortalCoreExpectation? = EazyPortalCoreExpectation(),
    ): DiagnosticsAssert {
        val projectType = ProjectType.fromProjectName(projectName)

        val siblingsText = siblingProjects.joinToString(", ", "[", "]") { (sibling, configuration) ->
            ":$sibling ($configuration)"
        }

        val eazyPortalCoreReport = buildList {
            if (eazyPortalCore == null) {
                add("  eazyportal-core : N/A   (disabled via DSL)")
            } else {
                val source = if (eazyPortalCore.version != CORE_DEFAULT_VERSION) {
                    "DSL override"
                } else {
                    "default (DefaultVersions)"
                }

                add("  eazyportal-core :")
                add("    version      : ${eazyPortalCore.version}   (source: $source)")
                add("    framework    : ${eazyPortalCore.framework}")
                add("    dependencies : ${eazyPortalCoreDependenciesText(projectType, eazyPortalCore)}")
            }
        }

        return containsSubsequence(
            "eazy-project diagnostics — :$projectName",
            "  projectType      : $projectType",
            "  conventions      : [$convention]",
            "  siblings         : $siblingsText",
            *eazyPortalCoreReport.toTypedArray()
        )
    }

    private fun eazyPortalCoreDependenciesText(projectType: ProjectType, eazyPortalCore: EazyPortalCoreExpectation): String =
        buildList {
            add("$CORE_BOM_ARTIFACT_ID (${(listOf(IMPLEMENTATION) + TEST_CONFIGURATIONS).joinToString(", ")})")

            if (projectType != ProjectType.DEFAULT) {
                add("${createCoreArtifactId(projectType, eazyPortalCore.framework)} (${eazyPortalCore.configuration})")
            }

            add("$CORE_TEST_PROJECT_ARTIFACT_ID (${TEST_CONFIGURATIONS.joinToString(", ")})")
        }.joinToString(", ", "[", "]")

    override fun toAssert(
        value: String,
        description: String,
    ): ObjectAssert<String> =
        ObjectAssert(value).`as`(description)

    override fun newAbstractIterableAssert(iterable: Iterable<String>): DiagnosticsAssert =
        DiagnosticsAssert(iterable.toList())

    /** What the `eazyportal-core` section of `eazyDiagnostics` is expected to report while `applyEazyPortalCoreDependencies` is on. */
    data class EazyPortalCoreExpectation(
        val version: String = CORE_DEFAULT_VERSION,
        val framework: Framework = Framework.SPRING_BOOT,
        val configuration: DependencyConfiguration = IMPLEMENTATION,
    )

    companion object {
        fun assertThat(output: String): DiagnosticsAssert =
            DiagnosticsAssert(output.lines())
    }

}
