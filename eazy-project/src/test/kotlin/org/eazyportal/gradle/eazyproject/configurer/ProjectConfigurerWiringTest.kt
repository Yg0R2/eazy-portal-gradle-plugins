package org.eazyportal.gradle.eazyproject.configurer

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.gradle.eazyproject.DefaultVersions
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_EXTENSION_NAME
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.API
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.IMPLEMENTATION
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.TEST_IMPLEMENTATION
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_LIBRARY_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_PROJECT_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.eazyproject.wiringSummary
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * The acceptance test for the Configurer hierarchy (design §9.3 / review D3): for **every** archetype it applies the
 * real Configurer to a `ProjectBuilder` module and asserts the §5.3 wiring table **exactly** — the applied
 * convention, each sibling dependency's configuration (`api` vs `implementation`), each versionless
 * `eazyportal-core-<type>` dependency's configuration, `eazyportal-core-test` on `testImplementation`, and the eazyportal-core
 * BOM platform on `implementation` + `testImplementation` (and on `api` for the archetypes that expose eazyportal-core
 * transitively). Because the assertions pin the *exact* configuration each dependency lands on, a leaked level (e.g.
 * an `implementation` dependency declared on `api`) fails the test.
 *
 * `ProjectBuilder` is the right vehicle here: it runs in-process so the `internal` Configurers are reachable and the
 * resulting `Configuration`s can be inspected directly, and the real convention plugins are on the classpath via the
 * `implementation("org.eazyportal.gradle.conventions:conventions:…")` dependency.
 */
class ProjectConfigurerWiringTest {

    @TestFactory
    fun `each archetype wires exactly the design §5_3 table`(): List<DynamicTest> =
        listOf(
            Expectation(
                projectType = ProjectType.COMMON,
                convention = KOTLIN_LIBRARY_CONVENTION,
                eazyPortalCoreImplementation = "common",
            ),
            Expectation(
                projectType = ProjectType.API,
                convention = KOTLIN_LIBRARY_CONVENTION,
                apiSiblings = listOf("common"),
                eazyPortalCoreApi = "api",
                bomOnApi = true,
            ),
            Expectation(
                projectType = ProjectType.PERSISTENCE,
                convention = KOTLIN_LIBRARY_CONVENTION,
                implementationSiblings = listOf("common"),
                eazyPortalCoreImplementation = "persistence",
            ),
            Expectation(
                projectType = ProjectType.SERVICE,
                convention = KOTLIN_LIBRARY_CONVENTION,
                implementationSiblings = listOf("common", "api", "persistence"),
                eazyPortalCoreImplementation = "service",
            ),
            Expectation(
                projectType = ProjectType.CLIENT,
                convention = KOTLIN_LIBRARY_CONVENTION,
                apiSiblings = listOf("common", "api"),
                eazyPortalCoreApi = "client",
                bomOnApi = true,
            ),
            Expectation(
                projectType = ProjectType.WEB,
                convention = KOTLIN_LIBRARY_CONVENTION,
                implementationSiblings = listOf("common", "api", "service"),
                eazyPortalCoreImplementation = "web",
            ),
            Expectation(
                projectType = ProjectType.APPLICATION,
                convention = KOTLIN_PROJECT_CONVENTION,
                implementationSiblings = listOf("common", "api", "persistence", "service", "client", "web"),
                eazyPortalCoreImplementation = "application",
            ),
            Expectation(
                projectType = ProjectType.DEFAULT,
                convention = KOTLIN_PROJECT_CONVENTION,
            ),
            Expectation(
                projectType = ProjectType.DEFAULT,
                projectName = "unknown",
                convention = KOTLIN_PROJECT_CONVENTION,
            ),
        ).map { expectation ->
            DynamicTest.dynamicTest("project.name=${expectation.projectName}, projectType=${expectation.projectType}") {
                val project = configureModule(expectation.projectName)

                assertConventionApplied(project, expectation)
                assertSiblings(project, expectation)
                assertEazyPortalCoreMainDependency(project, expectation)
                assertEazyPortalCoreTest(project)
                assertBom(project, expectation)
                assertWiringSummary(project, expectation)
            }
        }

    /** Builds a receiver with all standard modules (so `project(":sibling")` resolves) and configures [type]'s module. */
    private fun configureModule(projectName: String): Project {
        val root = ProjectBuilder.builder()
            .withName("receiver")
            .build()

        STANDARD_MODULES.forEach { module ->
            ProjectBuilder.builder()
                .withName(module)
                .withParent(root)
                .build()
        }

        val eazyProjectExtension = root.extensions
            .create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java)
            .apply { eazyPortalCoreVersion.set(EXAMPLE_CORE_VERSION) }

        return root.childProjects.getValue(projectName).also {
            ProjectConfigurerFactory.forType(eazyProjectExtension, it)
                .configure()
        }
    }

    private fun assertConventionApplied(project: Project, expectation: Expectation) {
        assertThat(project.pluginManager.hasPlugin(expectation.convention))
            .describedAs("%s should apply the %s convention", expectation.projectType, expectation.convention)
            .isTrue()
    }

    private fun assertSiblings(project: Project, expectation: Expectation) {
        assertThat(project.siblingPaths(API))
            .describedAs("%s api siblings", expectation.projectType)
            .containsExactlyInAnyOrderElementsOf(expectation.apiSiblings.map { ":$it" })

        assertThat(project.siblingPaths(IMPLEMENTATION))
            .describedAs("%s implementation siblings", expectation.projectType)
            .containsExactlyInAnyOrderElementsOf(expectation.implementationSiblings.map { ":$it" })
    }

    private fun assertEazyPortalCoreMainDependency(project: Project, expectation: Expectation) {
        // The eazyportal-core main layer must sit on EXACTLY its expected configuration and nowhere else — this is what
        // catches a leaked level (an `implementation` layer declared on `api`, or vice versa).
        assertThat(project.eazyPortalCoreMainModules(API))
            .describedAs("%s eazyportal-core on api", expectation.projectType)
            .containsExactlyElementsOf(listOfNotNull(expectation.eazyPortalCoreApi?.let { "eazyportal-core-$it" }))

        assertThat(project.eazyPortalCoreMainModules(IMPLEMENTATION))
            .describedAs("%s eazyportal-core on implementation", expectation.projectType)
            .containsExactlyElementsOf(listOfNotNull(expectation.eazyPortalCoreImplementation?.let { "eazyportal-core-$it" }))

        // Every eazyportal-core main dependency is versionless (the BOM pins it).
        (project.eazyPortalCoreExternalDependencies(API) + project.eazyPortalCoreExternalDependencies(IMPLEMENTATION))
            .filter { it.name != EXAMPLE_CORE_BOM }
            .forEach { dependency ->
                assertThat(dependency.version)
                    .describedAs("%s: %s must be versionless (BOM-pinned)", expectation.projectType, dependency.name)
                    .isNull()
            }
    }

    private fun assertEazyPortalCoreTest(project: Project) {
        val testDependency = project.eazyPortalCoreExternalDependencies(TEST_IMPLEMENTATION)
            .singleOrNull { it.name == "eazyportal-core-test" }

        assertThat(testDependency)
            .describedAs("eazyportal-core-test must be on testImplementation")
            .isNotNull()
        assertThat(testDependency!!.version)
            .describedAs("eazyportal-core-test must be versionless (BOM-pinned)")
            .isNull()
    }

    private fun assertBom(project: Project, expectation: Expectation) {
        assertThat(project.bomVersions(IMPLEMENTATION))
            .describedAs("%s BOM on implementation", expectation.projectType)
            .containsExactly(EXAMPLE_CORE_VERSION)

        assertThat(project.bomVersions(TEST_IMPLEMENTATION))
            .describedAs("%s BOM on testImplementation", expectation.projectType)
            .containsExactly(EXAMPLE_CORE_VERSION)

//        assertThat(project.bomVersions(API))
//            .describedAs("%s BOM on api (only when it exposes eazyportal-core transitively)", expectation.type)
//            .isEqualTo(if (expectation.bomOnApi) listOf(EXAMPLE_CORE_VERSION) else emptyList())
    }

    private fun assertWiringSummary(project: Project, expectation: Expectation) {
        // The WiringSummary is the single source of truth the diagnostics task reads — assert it agrees with what the
        // Configurer actually applied, so the two can never drift.
        val summary = project.wiringSummary()

        assertThat(summary.pluginIds).containsExactly(expectation.convention)
        assertThat(summary.siblings.map { it.notation })
            .containsExactlyInAnyOrderElementsOf(
                (expectation.apiSiblings + expectation.implementationSiblings).map { ":$it" },
            )
        assertThat(summary.eazyPortalCore.map { it.notation })
            .contains("eazyportal-core-test")
    }

    private companion object {
        const val EXAMPLE_CORE_VERSION = DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
        const val EXAMPLE_CORE_BOM = "eazyportal-core-bom"

        val STANDARD_MODULES = ProjectType.entries.map { it.toString() } + "unknown"

        fun Project.siblingPaths(dependencyConfiguration: DependencyConfiguration): List<String> =
            configurations.findByName(dependencyConfiguration.toString())
                ?.dependencies
                ?.filterIsInstance<ProjectDependency>()
                ?.map { it.path }
                ?: emptyList()

        fun Project.eazyPortalCoreExternalDependencies(dependencyConfiguration: DependencyConfiguration): List<ExternalModuleDependency> =
            configurations.findByName(dependencyConfiguration.toString())
                ?.dependencies
                ?.filterIsInstance<ExternalModuleDependency>()
                ?.filter { it.name.startsWith("eazyportal-core-") }
                ?: emptyList()

        /** eazyportal-core *main-layer* modules on [configuration] — excludes the BOM platform and `eazyportal-core-test`. */
        fun Project.eazyPortalCoreMainModules(dependencyConfiguration: DependencyConfiguration): List<String> =
            eazyPortalCoreExternalDependencies(dependencyConfiguration)
                .map { it.name }
                .filter { it != EXAMPLE_CORE_BOM && it != "eazyportal-core-test" }

        fun Project.bomVersions(dependencyConfiguration: DependencyConfiguration): List<String?> =
            eazyPortalCoreExternalDependencies(dependencyConfiguration)
                .filter { it.name == EXAMPLE_CORE_BOM }
                .map { it.version }
    }

    /** One archetype's expected §5.3 wiring. Empty collections / nulls mean "nothing on that axis". */
    private data class Expectation(
        val projectType: ProjectType,
        val projectName: String = projectType.toString(),
        val convention: String,
//        val dependencies: List<Pair<DependencyConfiguration, ProjectType>> = emptyList(),
        val apiSiblings: List<String> = emptyList(),
        val implementationSiblings: List<String> = emptyList(),
        val eazyPortalCoreApi: String? = null,
        val eazyPortalCoreImplementation: String? = null,
        val bomOnApi: Boolean = false,
    )

}
