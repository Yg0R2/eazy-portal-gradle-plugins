package org.eazyportal.gradle.eazysettings.model

import org.eazyportal.gradle.eazyproject.model.Framework
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_VERSION
import org.gradle.api.internal.provider.MissingValueException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * [EazySettingsExtension]'s own DSL surface (design §6.1).
 * `EazySettingsPlugin` never defaults this property itself — an unset value simply leaves `eazy-project`'s own convention default in place —
 * so there is no "default applies" case to cover here; that behavior is covered by `EazySettingsExtensionFunctionalTest`.
 *
 * A [org.gradle.api.Project] stands in for the real `Settings` extension container — the extension is a plain `ExtensionAware`-hosted DSL surface,
 * indifferent to which container hosts it (the same trick `eazy-project`'s own extension test uses).
 */
class EazySettingsExtensionTest {

    @Test
    fun `config value retrieval should fail since no value is set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java)

        assertThatThrownBy { extension.eazyPortalCoreVersion.get() }
            .isInstanceOf(MissingValueException::class.java)
            .hasMessage("Cannot query the value of extension 'eazySettings' property 'eazyPortalCoreVersion' because it has no value available.")

        assertThatThrownBy { extension.framework.get() }
            .isInstanceOf(MissingValueException::class.java)
            .hasMessage("Cannot query the value of extension 'eazySettings' property 'framework' because it has no value available.")

        assertThatThrownBy { extension.applyEazyPortalCoreDependencies.get() }
            .isInstanceOf(MissingValueException::class.java)
            .hasMessage("Cannot query the value of extension 'eazySettings' property 'applyEazyPortalCoreDependencies' because it has no value available.")
    }

    @Test
    fun `applyEazyPortalCoreDependencies can be set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java).apply {
            applyEazyPortalCoreDependencies.set(false)
        }

        assertThat(extension.applyEazyPortalCoreDependencies.get())
            .isFalse()
    }

    @Test
    fun `eazyPortalCoreVersion can be set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java).apply {
            eazyPortalCoreVersion.set(CORE_VERSION)
        }

        assertThat(extension.eazyPortalCoreVersion.get())
            .isEqualTo(CORE_VERSION)
    }

    @Test
    fun `exclude is empty by default`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java)

        assertThat(extension.exclude.get())
            .isEmpty()
    }

    @Test
    fun `exclude can be set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java).apply {
            exclude.set(listOf("platform", "bom"))
        }

        assertThat(extension.exclude.get())
            .containsExactly("platform", "bom")
    }

    @Test
    fun `framework can be set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java).apply {
            framework.set(Framework.SPRING_BOOT)
        }

        assertThat(extension.framework.get())
            .isEqualTo(Framework.SPRING_BOOT)
    }

}
