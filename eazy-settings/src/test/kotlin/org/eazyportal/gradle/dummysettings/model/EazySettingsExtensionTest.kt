package org.eazyportal.gradle.dummysettings.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.eazyportal.gradle.dummysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.gradle.api.internal.provider.MissingValueException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * [EazySettingsExtension]'s own DSL surface (design §6.1).
 * `EazySettingsPlugin` never defaults this property itself — an unset value simply leaves `eazy-project`'s own convention default in place —
 * so there is no "default applies" case to cover here; that behavior is covered by `EazySettingsPluginFunctionalTest`.
 *
 * A [org.gradle.api.Project] stands in for the real `Settings` extension container — the extension is a plain `ExtensionAware`-hosted DSL surface,
 * indifferent to which container hosts it (the same trick `eazy-project`'s own extension test uses).
 */
class EazySettingsExtensionTest {

    @Test
    fun `eazyPortalCoreVersion retrieval should fail since no value is set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java)

        assertThatThrownBy { extension.eazyPortalCoreVersion.get() }
            .isInstanceOf(MissingValueException::class.java)
            .hasMessage("Cannot query the value of extension 'eazySettings' property 'eazyPortalCoreVersion' because it has no value available.")
    }

    @Test
    fun `eazyPortalCoreVersion can be set`() {
        val project = ProjectBuilder.builder().build()

        val extension = project.extensions.create(EAZY_SETTINGS_EXTENSION_NAME, EazySettingsExtension::class.java).apply {
            eazyPortalCoreVersion.set("9.9.9")
        }

        assertThat(extension.eazyPortalCoreVersion.get())
            .isEqualTo("9.9.9")
    }

}
