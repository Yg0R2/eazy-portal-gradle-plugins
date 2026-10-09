package org.eazyportal.gradle.eazyproject.configurer

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_EXTENSION_NAME
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.API
import org.eazyportal.gradle.eazyproject.model.DependencyConfiguration.IMPLEMENTATION
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.eazyportal.gradle.eazyproject.wiringSummary
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * Which sibling modules a module needs (README: *Module structure and what each module gets*):
 * a *required* sibling must exist — its absence fails the configuration —
 * while an *optional* sibling is wired exactly when it is present and silently skipped otherwise.
 *
 * [ProjectConfigurerWiringTest] pins the full wiring (every sibling present); this class pins the presence rules
 * around it, for every archetype: only the required siblings, each optional sibling on its own, all siblings,
 * and each required sibling missing.
 */
class SiblingProjectPresenceTest {

    @TestFactory
    fun `a module wires exactly the siblings that are present`(): List<DynamicTest> =
        SPECS.flatMap { spec ->
            buildList {
                add(
                    dynamicTest("${spec.type}: only the required siblings are present") {
                        assertSiblingsWired(spec, present = spec.required)
                    },
                )

                spec.optional.forEach { optional ->
                    add(
                        dynamicTest("${spec.type}: the required siblings and only optional '${optional.name}' are present") {
                            assertSiblingsWired(spec, present = spec.required + optional)
                        },
                    )
                }

                add(
                    dynamicTest("${spec.type}: all siblings are present") {
                        assertSiblingsWired(spec, present = spec.required + spec.optional)
                    },
                )
            }
        }

    @TestFactory
    fun `a module fails when a required sibling is missing`(): List<DynamicTest> =
        SPECS.flatMap { spec ->
            spec.required.map { missing ->
                dynamicTest("${spec.type}: required '${missing.name}' is missing") {
                    // Everything else IS present, so the missing required sibling is the only possible cause.
                    val present = (spec.required - missing) + spec.optional

                    assertThatThrownBy { configureModule(spec.type, present.map { it.name }) }
                        .describedAs("%s without its required sibling '%s'", spec.type, missing.name)
                        .hasMessageContaining(":${missing.name}")
                }
            }
        }

    private fun assertSiblingsWired(spec: Spec, present: List<Sibling>) {
        val project = configureModule(spec.type, present.map { it.name })

        listOf(API, IMPLEMENTATION).forEach { configuration ->
            assertThat(project.siblingPaths(configuration))
                .describedAs("%s sibling projects on %s", spec.type, configuration)
                .containsExactlyInAnyOrderElementsOf(
                    present.filter { it.configuration == configuration }.map { ":${it.name}" },
                )
        }

        // The WiringSummary feeds `eazyDiagnostics`: it must agree with what was actually wired.
        assertThat(project.wiringSummary().siblings.map { it.toString() })
            .describedAs("%s wiring summary", spec.type)
            .containsExactlyInAnyOrderElementsOf(present.map { ":${it.name} (${it.configuration})" })
    }

    /** Builds a receiver that contains the module of [type] plus exactly [siblingNames], and configures that module. */
    private fun configureModule(type: ProjectType, siblingNames: List<String>): Project {
        val root = ProjectBuilder.builder()
            .withName("receiver")
            .build()

        (siblingNames + type.toString()).forEach { name ->
            ProjectBuilder.builder()
                .withName(name)
                .withParent(root)
                .build()
        }

        val eazyProjectExtension = root.extensions
            .create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java)

        return root.childProjects.getValue(type.toString()).also {
            ProjectConfigurerFactory.forType(eazyProjectExtension, it)
                .configure()
        }
    }

    private fun Project.siblingPaths(dependencyConfiguration: DependencyConfiguration): List<String> =
        configurations.getByName(dependencyConfiguration.toString())
            .dependencies
            .filterIsInstance<ProjectDependency>()
            .map { it.path }

    private data class Sibling(
        val name: String,
        val configuration: DependencyConfiguration,
    )

    /** One archetype's sibling contract. */
    private data class Spec(
        val type: ProjectType,
        val required: List<Sibling> = emptyList(),
        val optional: List<Sibling> = emptyList(),
    )

    companion object {
        private val SPECS = listOf(
            Spec(ProjectType.COMMON),
            Spec(
                ProjectType.API,
                optional = listOf(api(ProjectType.COMMON)),
            ),
            Spec(
                ProjectType.PERSISTENCE,
                optional = listOf(impl(ProjectType.COMMON)),
            ),
            Spec(
                ProjectType.SERVICE,
                optional = listOf(impl(ProjectType.COMMON), impl(ProjectType.API), impl(ProjectType.PERSISTENCE)),
            ),
            Spec(
                ProjectType.CLIENT,
                required = listOf(api(ProjectType.API)),
                optional = listOf(api(ProjectType.COMMON)),
            ),
            Spec(
                ProjectType.WEB,
                required = listOf(impl(ProjectType.API)),
                optional = listOf(impl(ProjectType.COMMON), impl(ProjectType.SERVICE)),
            ),
            Spec(
                ProjectType.APPLICATION,
                optional = listOf(
                    impl(ProjectType.COMMON),
                    impl(ProjectType.API),
                    impl(ProjectType.PERSISTENCE),
                    impl(ProjectType.SERVICE),
                    impl(ProjectType.WEB),
                ),
            ),
            Spec(ProjectType.DEFAULT),
        )

        private fun impl(type: ProjectType) = Sibling(type.toString(), IMPLEMENTATION)

        private fun api(type: ProjectType) = Sibling(type.toString(), API)
    }

}
