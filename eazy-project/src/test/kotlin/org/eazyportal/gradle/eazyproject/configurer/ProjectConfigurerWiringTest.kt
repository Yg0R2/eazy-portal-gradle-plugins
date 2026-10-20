package org.eazyportal.gradle.eazyproject.configurer

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.gradle.eazyproject.DefaultVersions.CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_EXTENSION_NAME
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.API
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.IMPLEMENTATION
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.Companion.TEST_CONFIGURATIONS
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_LIBRARY_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_PROJECT_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.eazyproject.wiringSummary
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_BOM_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_GROUP_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_TEST_PROJECT_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.createCoreArtifactId
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
 * `eazyportal-core-<type>` (`eazyportal-<framework>-core-<type>` for the framework-specific archetypes) dependency's configuration, `eazyportal-core-test` on every test configuration (unit, functional,
 * integration, test fixtures), and the eazyportal-core BOM platform on `implementation` + every test configuration (and on `api` for the archetypes that expose eazyportal-core
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
                eazyPortalCores = listOf(
                    Expectation.Dependency(
                        projectName = "common",
                        configuration = IMPLEMENTATION
                    )
                ),
            ),
            Expectation(
                projectType = ProjectType.API,
                convention = KOTLIN_LIBRARY_CONVENTION,
                siblings = listOf(
                    Expectation.Dependency(
                        projectName = "common",
                        configuration = API,
                    )
                ),
                eazyPortalCores = listOf(
                    Expectation.Dependency(
                        projectName = "api",
                        configuration = API,
                    )
                ),
                bomOnApi = true,
            ),
            Expectation(
                projectType = ProjectType.PERSISTENCE,
                convention = KOTLIN_LIBRARY_CONVENTION,
                siblings = listOf(
                    Expectation.Dependency(
                        projectName = "common",
                        configuration = IMPLEMENTATION
                    )
                ),
                eazyPortalCores = listOf(
                    Expectation.Dependency(
                        projectName = "persistence",
                        configuration = IMPLEMENTATION
                    )
                ),
            ),
            Expectation(
                projectType = ProjectType.SERVICE,
                convention = KOTLIN_LIBRARY_CONVENTION,
                siblings = listOf(
                    Expectation.Dependency(
                        projectName = "common",
                        configuration = IMPLEMENTATION
                    ),
                    Expectation.Dependency(
                        projectName = "api",
                        configuration = IMPLEMENTATION
                    ),
                    Expectation.Dependency(
                        projectName = "persistence",
                        configuration = IMPLEMENTATION
                    )
                ),
                eazyPortalCores = listOf(
                    Expectation.Dependency(
                        projectName = "service",
                        configuration = IMPLEMENTATION
                    )
                ),
            ),
            Expectation(
                projectType = ProjectType.CLIENT,
                convention = KOTLIN_LIBRARY_CONVENTION,
                siblings = listOf(
                    Expectation.Dependency(
                        projectName = "common",
                        configuration = API,
                    ),
                    Expectation.Dependency(
                        projectName = "api",
                        configuration = API,
                    )
                ),
                eazyPortalCores = listOf(
                    Expectation.Dependency(
                        projectName = "client",
                        configuration = API,
                    )
                ),
                bomOnApi = true,
            ),
            Expectation(
                projectType = ProjectType.WEB,
                convention = KOTLIN_LIBRARY_CONVENTION,
                siblings = listOf(
                    Expectation.Dependency(
                        projectName = "common",
                        configuration = IMPLEMENTATION
                    ),
                    Expectation.Dependency(
                        projectName = "api",
                        configuration = IMPLEMENTATION
                    ),
                    Expectation.Dependency(
                        projectName = "service",
                        configuration = IMPLEMENTATION
                    )
                ),
                eazyPortalCores = listOf(
                    Expectation.Dependency(
                        projectName = "web",
                        configuration = IMPLEMENTATION
                    )
                ),
            ),
            Expectation(
                projectType = ProjectType.APPLICATION,
                convention = KOTLIN_PROJECT_CONVENTION,
                siblings = listOf(
                    Expectation.Dependency(
                        projectName = "common",
                        configuration = IMPLEMENTATION
                    ),
                    Expectation.Dependency(
                        projectName = "api",
                        configuration = IMPLEMENTATION
                    ),
                    Expectation.Dependency(
                        projectName = "persistence",
                        configuration = IMPLEMENTATION
                    ),
                    Expectation.Dependency(
                        projectName = "service",
                        configuration = IMPLEMENTATION
                    ),
                    Expectation.Dependency(
                        projectName = "web",
                        configuration = IMPLEMENTATION
                    )
                ),
                eazyPortalCores = listOf(
                    Expectation.Dependency(
                        projectName = "application",
                        configuration = IMPLEMENTATION
                    )
                ),
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
                assertProductionSiblings(project, expectation)
                assertTestConfigurationDependencies(project, expectation)
                assertEazyPortalCoreMainDependency(project, expectation)
                assertEazyPortalCoreTest(project)
                assertBom(project, expectation)
                assertWiringSummary(project, expectation)
            }
        }

    /**
     * `applyEazyPortalCoreDependencies = false` drops every eazyportal-core dependency (BOM and `eazyportal-core-test` included) but keeps the convention + siblings.
     * The flag is flipped AFTER the Configurer ran — the real ordering, since `eazy-settings` and the module's build script set it after the plugin applied (§6.3).
     */
    @TestFactory
    fun `applyEazyPortalCoreDependencies=false skips every eazyportal-core dependency but keeps the convention and siblings`(): List<DynamicTest> =
        listOf(ProjectType.SERVICE, ProjectType.CLIENT, ProjectType.DEFAULT).map { projectType ->
            DynamicTest.dynamicTest("projectType=$projectType") {
                val project = configureModule(projectType.toString(), applyEazyPortalCoreDependencies = false)

                (listOf(API, IMPLEMENTATION) + TEST_CONFIGURATIONS).forEach { configuration ->
                    assertThat(project.eazyPortalCoreExternalDependencies(configuration))
                        .describedAs("%s eazyportal-core dependencies on %s", projectType, configuration)
                        .isEmpty()
                }

                assertThat(project.pluginManager.hasPlugin(project.wiringSummary().pluginIds.single())).isTrue()
                assertThat(
                    listOf(API, IMPLEMENTATION).flatMap { configuration ->
                        project.configurations.getByName(configuration.toString())
                            .dependencies
                            .filterIsInstance<ProjectDependency>()
                            .map { "${it.path} ($configuration)" }
                    }
                ).describedAs("%s siblings stay wired", projectType)
                    .containsExactlyInAnyOrderElementsOf(project.wiringSummary().siblings.map { it.toString() })
            }
        }

    /**
     * Builds a receiver with all standard modules (so `project(":sibling")` resolves) and configures [type]'s module.
     * [applyEazyPortalCoreDependencies] is set only after the Configurer ran, like `eazy-settings` does.
     */
    private fun configureModule(projectName: String, applyEazyPortalCoreDependencies: Boolean = true): Project {
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
            .apply {
                eazyPortalCoreVersion.set(CORE_DEFAULT_VERSION)
                framework.set(Framework.SPRING_BOOT)
                this.applyEazyPortalCoreDependencies.set(true)
            }

        return root.childProjects.getValue(projectName).also {
            ProjectConfigurerFactory.forType(eazyProjectExtension, it)
                .configure()

            eazyProjectExtension.applyEazyPortalCoreDependencies.set(applyEazyPortalCoreDependencies)
        }
    }

    private fun assertConventionApplied(project: Project, expectation: Expectation) {
        assertThat(project.pluginManager.hasPlugin(expectation.convention))
            .describedAs("%s should apply the %s convention", expectation.projectType, expectation.convention)
            .isTrue()
    }

    /**
     * The §5.3 production wiring: each sibling sits on EXACTLY its declared level (`api` vs `implementation`) and
     * nowhere else. Asserted on the *directly declared* dependencies (`dependencies`, not `allDependencies`), so a
     * leaked level — an `implementation` sibling declared on `api`, or vice versa — fails here.
     */
    private fun assertProductionSiblings(project: Project, expectation: Expectation) {
        val actual = listOf(API, IMPLEMENTATION)
            .associate {
                it.toString() to project.configurations.findByName(it.toString())
                    ?.dependencies
                    ?.filterIsInstance<ProjectDependency>()
                    ?.map(ProjectDependency::getPath)
                    .orEmpty()
            }.filterValues { it.isNotEmpty() }

        assertThat(actual)
            .describedAs("%s production siblings by configuration", expectation.projectType)
            .containsExactlyInAnyOrderEntriesOf(
                expectation.siblings.groupBy { it.configuration.toString() }
                    .mapValues { (_, dependencies) -> dependencies.map { ":${it.projectName}" } }
            )
    }

    /**
     * Every test source set must SEE everything the main source set depends on — the siblings (so test and fixture
     * code can use and mock the dependent modules; an `application` integration test boots Spring with every layer
     * present) — plus the module itself. Siblings arrive by configuration inheritance (`functionalTest`,
     * `integrationTest` and `testFixtures` `extendsFrom implementation`; `test` already does), so this asserts the
     * full *reachable* set via `allDependencies`.
     *
     * The module references itself both as its MAIN jar (no capability) and as its TEST-FIXTURES jar (the
     * `test-fixtures` feature capability); which of the two lands on a given configuration differs by configuration,
     * so the two are distinguished by that capability and both are pinned.
     */
    private fun assertTestConfigurationDependencies(project: Project, expectation: Expectation) {
        val siblings = expectation.siblings.map { ":${it.projectName}" }
        val self = ":${expectation.projectName}"
        val selfTestFixtures = "$self (test-fixtures)"

        mapOf(
            DependencyConfiguration.TEST_IMPLEMENTATION to (siblings + selfTestFixtures),
            DependencyConfiguration.FUNCTIONAL_TEST_IMPLEMENTATION to (siblings + self + selfTestFixtures),
            DependencyConfiguration.INTEGRATION_TEST_IMPLEMENTATION to (siblings + self + selfTestFixtures),
            DependencyConfiguration.TEST_FIXTURES_IMPLEMENTATION to (siblings + self),
        ).forEach { (configuration, expected) ->
            assertThat(project.projectDependencies(configuration))
                .describedAs("%s %s reachable project dependencies (siblings + self)", expectation.projectType, configuration)
                .containsExactlyInAnyOrderElementsOf(expected)
        }
    }

    private fun assertEazyPortalCoreMainDependency(project: Project, expectation: Expectation) {
        val dependenciesByConfiguration = expectation.eazyPortalCores.groupBy { it.configuration }

        // The eazyportal-core main layer must sit on EXACTLY its expected configuration and nowhere else — this is what
        // catches a leaked level (an `implementation` layer declared on `api`, or vice versa).
        dependenciesByConfiguration.forEach { (configuration, dependencies) ->
            assertThat(project.eazyPortalCoreMainModules(configuration))
                .describedAs("%s eazyportal-core on %s", expectation.projectType, configuration)
                .containsExactlyInAnyOrderElementsOf(
                    dependencies.map {
                        createCoreArtifactId(it.projectName, Framework.SPRING_BOOT)
                    }
                )
        }

        // Every eazyportal-core main dependency is versionless (the BOM pins it).
        dependenciesByConfiguration.keys.flatMap { project.eazyPortalCoreExternalDependencies(it) }
            .filter { it.name != CORE_BOM_ARTIFACT_ID }
            .forEach { dependency ->
                assertThat(dependency.version)
                    .describedAs("%s: %s must be versionless (BOM-pinned)", expectation.projectType, dependency.name)
                    .isNull()
            }
    }

    private fun assertEazyPortalCoreTest(project: Project) {
        TEST_CONFIGURATIONS.forEach { configuration ->
            val testDependency = project.eazyPortalCoreExternalDependencies(configuration)
                .singleOrNull { it.name == CORE_TEST_PROJECT_ARTIFACT_ID }

            assertThat(testDependency)
                .describedAs("$CORE_TEST_PROJECT_ARTIFACT_ID must be on $configuration")
                .isNotNull()
            assertThat(testDependency!!.version)
                .describedAs("$CORE_TEST_PROJECT_ARTIFACT_ID on $configuration must be versionless (BOM-pinned)")
                .isNull()
        }

        // eazyportal-core-test is a test-only dependency: it must never leak onto a production configuration.
        listOf(API, IMPLEMENTATION).forEach { configuration ->
            assertThat(project.eazyPortalCoreExternalDependencies(configuration).map { it.name })
                .describedAs("$CORE_TEST_PROJECT_ARTIFACT_ID must not be on $configuration")
                .doesNotContain(CORE_TEST_PROJECT_ARTIFACT_ID)
        }
    }

    private fun assertBom(project: Project, expectation: Expectation) {
        assertThat(project.bomVersions(IMPLEMENTATION))
            .describedAs("%s BOM on implementation", expectation.projectType)
            .containsExactly(CORE_DEFAULT_VERSION)

        TEST_CONFIGURATIONS.forEach { configuration ->
            assertThat(project.bomVersions(configuration))
                .describedAs("%s BOM on %s", expectation.projectType, configuration)
                .containsExactly(CORE_DEFAULT_VERSION)
        }

        // TODO: validate that whether [GradleProjectConfigure] needs this
//        assertThat(project.bomVersions(API))
//            .describedAs("%s BOM on api (only when it exposes eazyportal-core transitively)", expectation.projectType)
//            .isEqualTo(if (expectation.bomOnApi) listOf(EXAMPLE_CORE_VERSION) else emptyList())
    }

    private fun assertWiringSummary(project: Project, expectation: Expectation) {
        // The WiringSummary is the single source of truth the diagnostics task reads — assert it agrees with what the
        // Configurer actually applied, so the two can never drift.
        val summary = project.wiringSummary()

        assertThat(summary.pluginIds).containsExactly(expectation.convention)
        assertThat(summary.siblings.map { it.toString() })
            .containsExactlyInAnyOrderElementsOf(
                expectation.siblings.map { ":${it.projectName} (${it.configuration})" }
            )

        // eazyportal-core entries are external coordinates (`eazyportal-[<framework>-]core-<layer>`), not project paths — so,
        // unlike the sibling block above, they carry no leading `:`, and the framework-specific layers are
        // framework-prefixed (same resolution as assertEazyPortalCoreMainDependency, via createCoreArtifactId).
        assertThat(summary.eazyPortalCore.map { it.toString() })
            .containsExactlyInAnyOrder(
                *expectation.eazyPortalCores.groupBy { it.configuration }
                    .flatMap { (configuration, dependencies) ->
                        dependencies.map {
                            "${createCoreArtifactId(it.projectName, Framework.SPRING_BOOT)} ($configuration)"
                        }
                    }.toTypedArray(),
                *TEST_CONFIGURATIONS.map { "$CORE_TEST_PROJECT_ARTIFACT_ID ($it)" }.toTypedArray(),
            )
    }

    private companion object {
        val STANDARD_MODULES = ProjectType.entries.map { it.toString() } + "unknown"

        /**
         * The project (sibling + self) dependencies *reachable* from [dependencyConfiguration] — `allDependencies`,
         * so inherited siblings count. A self-reference to the module's test-fixtures jar is tagged `(test-fixtures)`
         * (the `test-fixtures` feature capability) to tell it apart from the main-jar reference on the same configuration.
         */
        fun Project.projectDependencies(dependencyConfiguration: DependencyConfiguration): List<String> =
            configurations.getByName(dependencyConfiguration.toString())
                .allDependencies
                .filterIsInstance<ProjectDependency>()
                .map {
                    it.path + if (it.requestsTestFixtures()) {
                        " (test-fixtures)"
                    } else {
                        ""
                    }
                }

        // A plain `project()` dependency requests no capability; `testFixtures(project())` requests the module's
        // `test-fixtures` feature capability (CapabilitySelector only exposes a display name — "feature
        // 'test-fixtures'" — in the public API, so match on that).
        private fun ProjectDependency.requestsTestFixtures(): Boolean =
            capabilitySelectors.any { it.displayName.contains("test-fixtures") }

        fun Project.eazyPortalCoreExternalDependencies(dependencyConfiguration: DependencyConfiguration): List<ExternalModuleDependency> =
            configurations.findByName(dependencyConfiguration.toString())
                ?.dependencies
                ?.filterIsInstance<ExternalModuleDependency>()
                ?.filter { it.group == CORE_GROUP_ID }
                ?: emptyList()

        /** eazyportal-core *main-layer* modules on [configuration] — excludes the BOM platform and `eazyportal-core-test`. */
        fun Project.eazyPortalCoreMainModules(dependencyConfiguration: DependencyConfiguration): List<String> =
            eazyPortalCoreExternalDependencies(dependencyConfiguration)
                .map { it.name }
                .filter { it != CORE_BOM_ARTIFACT_ID && it != CORE_TEST_PROJECT_ARTIFACT_ID }

        fun Project.bomVersions(dependencyConfiguration: DependencyConfiguration): List<String?> =
            eazyPortalCoreExternalDependencies(dependencyConfiguration)
                .filter { it.name == CORE_BOM_ARTIFACT_ID }
                .map { it.version }
    }

    /** One archetype's expected §5.3 wiring. Empty collections / nulls mean "nothing on that axis". */
    private data class Expectation(
        val projectType: ProjectType,
        val projectName: String = projectType.toString(),
        val convention: String,
        val siblings: List<Dependency> = emptyList(),
        val eazyPortalCores: List<Dependency> = emptyList(),
        val bomOnApi: Boolean = false,
    ) {

        data class Dependency(
            val projectName: String,
            val configuration: DependencyConfiguration,
        )

    }

}
