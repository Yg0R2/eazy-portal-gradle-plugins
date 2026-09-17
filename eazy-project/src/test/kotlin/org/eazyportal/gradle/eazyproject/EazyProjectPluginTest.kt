package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_LIBRARY_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyPortalConventionPluginNames.KOTLIN_PROJECT_CONVENTION
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * [EazyProjectPlugin] (design §5.5) end-to-end within a single `ProjectBuilder` tree — the root-project guard,
 * the extension's default,
 * the archetype → Configurer hand-off (already exhaustively covered per-archetype by [org.eazyportal.gradle.eazyproject.configurer.ProjectConfigurerWiringTest];
 * this class only proves the *plugin* wires that hand-off correctly),
 * and the `eazyDiagnostics` task registration.
 */
class EazyProjectPluginTest {

    @Test
    fun `applying to the root project throws`() {
        val root = ProjectBuilder.builder().withName("receiver").build()

        assertThatThrownBy { EazyProjectPlugin().apply(root) }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("must not be applied to the root project")
    }

    @Test
    fun `applying creates the eazyProject extension with the DefaultVersions eazyPortalCoreVersion convention`() {
        val common = buildReceiverSubproject("common")

        EazyProjectPlugin().apply(common)

        assertThat(common.extensions.getByType(EazyProjectExtension::class.java).eazyPortalCoreVersion.get())
            .isEqualTo(EXAMPLE_CORE_DEFAULT_VERSION)
    }

    @Test
    fun `applying to an 'api' module resolves ProjectType_API and runs the matching Configurer`() {
        val api = buildReceiverSubproject("api", siblings = listOf("common"))

        EazyProjectPlugin().apply(api)

        assertThat(api.pluginManager.hasPlugin(KOTLIN_LIBRARY_CONVENTION)).isTrue()
        assertThat(api.wiringSummary().pluginIds).containsExactly(KOTLIN_LIBRARY_CONVENTION)
    }

    @Test
    fun `applying to a non-standard module falls back to DEFAULT and applies kotlin-project-convention`() {
        val custom = buildReceiverSubproject("custom")

        EazyProjectPlugin().apply(custom)

        assertThat(ProjectType.fromProjectName(custom.name)).isEqualTo(ProjectType.DEFAULT)
        assertThat(custom.pluginManager.hasPlugin(KOTLIN_PROJECT_CONVENTION)).isTrue()
    }

    @Test
    fun `applying registers a eazyDiagnostics task in the help group that reads the recorded WiringSummary`() {
        val common = buildReceiverSubproject("common")

        EazyProjectPlugin().apply(common)

        val task = common.tasks.getByName(EAZY_PROJECT_DIAGNOSTICS_TASK_NAME)
        assertThat(task.group).isEqualTo("help")
        assertThat(task.description).isNotBlank()
    }

    /** A subproject named [name] under a fresh root, with [siblings] also present so `project.project(":x")` resolves. */
    private fun buildReceiverSubproject(name: String, siblings: List<String> = emptyList()): Project {
        val root = ProjectBuilder.builder().withName("receiver").build()

        (siblings + name).distinct().forEach {
            ProjectBuilder.builder().withName(it).withParent(root).build()
        }

        return root.childProjects.getValue(name)
    }

}
