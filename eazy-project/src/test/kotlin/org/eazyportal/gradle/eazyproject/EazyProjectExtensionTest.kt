package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_EXTENSION_NAME
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_VERSION
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.gradle.api.internal.provider.MissingValueException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * The default `eazyPortalCoreVersion` convention is owned by [EazyProjectPlugin] (design §5.5),
 * so it's covered by [EazyProjectPluginTest] instead of here — this class only covers the extension's own DSL surface.
 */
class EazyProjectExtensionTest {

    @Test
    fun `eazyPortalCoreVersion retrieval should fail since default value is not set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java)

        assertThatThrownBy { extension.eazyPortalCoreVersion.get() }
            .isInstanceOf(MissingValueException::class.java)
            .hasMessage("Cannot query the value of extension 'eazyProject' property 'eazyPortalCoreVersion' because it has no value available.")
    }

    @Test
    fun `eazyPortalCoreVersion can be overridden`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java).apply {
            // This is the same as what eazy-settings does in its plugin, design §6.
            eazyPortalCoreVersion.set(CORE_VERSION)
        }

        assertThat(extension.eazyPortalCoreVersion.get())
            .isEqualTo(CORE_VERSION)
    }

    @Test
    fun `framework retrieval should fail since default value is not set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java)

        assertThatThrownBy { extension.framework.get() }
            .isInstanceOf(MissingValueException::class.java)
            .hasMessage("Cannot query the value of extension 'eazyProject' property 'framework' because it has no value available.")
    }

    @Test
    fun `framework can be overridden`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java).apply {
            framework.set(Framework.SPRING_BOOT)
        }

        assertThat(extension.framework.get())
            .isEqualTo(Framework.SPRING_BOOT)
    }

    @Test
    fun `applyEazyPortalCoreDependencies retrieval should fail since default value is not set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java)

        assertThatThrownBy { extension.applyEazyPortalCoreDependencies.get() }
            .isInstanceOf(MissingValueException::class.java)
            .hasMessage("Cannot query the value of extension 'eazyProject' property 'applyEazyPortalCoreDependencies' because it has no value available.")
    }

    @Test
    fun `applyEazyPortalCoreDependencies can be overridden`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_PROJECT_EXTENSION_NAME, EazyProjectExtension::class.java).apply {
            applyEazyPortalCoreDependencies.set(false)
        }

        assertThat(extension.applyEazyPortalCoreDependencies.get())
            .isFalse()
    }

}
