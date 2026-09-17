package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_EXTENSION_NAME
import org.eazyportal.gradle.eazyproject.model.EazyProjectExtension
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
            eazyPortalCoreVersion.set("9.9.9")
        }

        assertThat(extension.eazyPortalCoreVersion.get())
            .isEqualTo("9.9.9")
    }

}
